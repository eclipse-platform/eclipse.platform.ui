package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIEventFilter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The Debug UI Plugin.
 *
 */
public class DebugUIPlugin extends AbstractUIPlugin implements ISelectionChangedListener,
															   IDebugEventListener, 
															   ISelectionListener, 
															   IDocumentListener, 
															   ILaunchListener,
															   IResourceChangeListener {
															   	
										   	
	/**
	 * The singleton debug plugin instance
	 */
	private static DebugUIPlugin fgDebugUIPlugin= null;
	
	/**
	 * A utility presentation used to obtain labels
	 */
	protected static IDebugModelPresentation fgPresentation = null;

	/**
	 * The selection providers for the debug UI
	 */
	protected List fSelectionProviders= new ArrayList(2);

	/**
	 * The desktop parts that contain the selections
	 */
	protected List fSelectionParts= new ArrayList(2);

	/**
	 * The list of selection listeners for the debug UI
	 */
	protected ListenerList fListeners= new ListenerList(2);

	/**
	 * The mappings of processes to their console documents.
	 */
	protected Map fConsoleDocuments= new HashMap(3);
	
	/**
	 * The process that is/can provide output to the console
	 * view.
	 */
	protected IProcess fCurrentProcess= null;

	/**
	 * Colors to be used in the debug ui
	 */
	protected ColorManager fColorManager= new ColorManager();

	/**
	 * The most recent launch
	 */
	protected LaunchHistoryElement fRecentLaunch = null;
	
	protected final static int MAX_HISTORY_SIZE= 5;
	/**
	 * The most recent debug launches
	 */
	protected Vector fDebugHistory = new Vector(MAX_HISTORY_SIZE);
	
	/**
	 * The most recent run launches
	 */
	protected Vector fRunHistory = new Vector(MAX_HISTORY_SIZE);
	
	/**
	 * Event filters for the debug UI
	 */
	protected ListenerList fEventFilters = new ListenerList(2);
	
	/**
	 * The visitor used to traverse resource deltas and keep the run & debug
	 * histories in synch with resource deletions.
	 */
	protected static ResourceDeletedVisitor fgDeletedVisitor;
	
	/**
	 * Context used to decide whether to show/switch to the 
	 * debug view / debug perspective on a launch or suspend event
	 */
	protected SwitchContext fSwitchContext= new SwitchContext();
	/**
	 * Visitor for handling resource deltas
	 */
	class ResourceDeletedVisitor implements IResourceDeltaVisitor {
		
		/**
		 * @see IResourceDeltaVisitor
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta == null) {
				return false;
			}
			if (delta.getKind() != IResourceDelta.REMOVED) {
				return true;
			}
			// check for deletions in launch history
			removeDeletedHistories();
			return false;
		}
	}

	/**
	 * Tracks the debugger page and perspective for
	 * the DebugUIPlugin
	 */
	class SwitchContext implements IPartListener, IPageListener {
		protected IWorkbenchWindow fWindow;
		protected IWorkbenchPage fPage;
		protected IPerspectiveDescriptor fPerspective;
		protected LaunchesView fDebuggerView;
		protected boolean fPageCreated;
		protected boolean fContextChanged= true;
		protected String fMode;
		
		protected void init(String mode) {
			fMode= mode;
			fPageCreated= false;
			if (fPage != null) {
				fPage.removePartListener(this);
			}
			fPage= null;
			fDebuggerView= null;
			if (fWindow != null) {
				fWindow.removePageListener(this);
			}
			fWindow= getActiveWorkbenchWindow();
			fWindow.addPageListener(this);
			fContextChanged= false;
		}
		
		protected void shutdown() {
			if (fWindow == null) {
				return;
			}
			Shell shell= fWindow.getShell();
			if (shell == null || shell.isDisposed()) {
				return;
			}
			if (fPage != null) {
				fPage.removePartListener(this);
			}
			if (fWindow != null) {
				fWindow.removePageListener(this);
			}
		}
		
		/**
		 * Returns whether the world has changed in a way that will
		 * require switching to the debug perspective.
		 */
		protected boolean contextChanged(String mode) {
			if (!fContextChanged) {
				if (fMode == null || !fMode.equals(mode)) {
					return true;
				}
			}
			boolean changed=  fContextChanged || 
				!fWindow.equals(getActiveWorkbenchWindow()) ||
				!fWindow.getActivePage().equals(fPage) ||
				!fWindow.getActivePage().getPerspective().equals(fPerspective);
			
			return changed;
		}
		
		/**
		 * Returns whether the DebugUIPlugin has recently caused
		 * a debugger page to be created.
		 */
		protected boolean wasPageCreated() {
			return fPageCreated;
		}
		
		/**
		 * Sets whether the DebugUIPlugin has recently caused
		 * a debugger page to be created.
		 */
		protected void setPageCreated(boolean created) {
			fPageCreated= created;
		}
		
		protected IWorkbenchPage getPage() {
			return fPage;
		}
		
		protected void setPage(IWorkbenchPage page) {
			if (fPage != null) {
				fPage.removePartListener(this);
			}
			fPage = page;
			fPage.addPartListener(this);
			fPerspective= page.getPerspective();
		}
	
		protected IWorkbenchWindow getWindow() {
			return fWindow;
		}
		
		protected void setWindow(IWorkbenchWindow window) {
			fWindow.removePageListener(this);
			fWindow = window;
			fWindow.addPageListener(this);
		}
		
		protected LaunchesView getDebuggerView() {
			return fDebuggerView;
		}
		
		protected void setDebuggerView(LaunchesView debuggerView) {
			fDebuggerView = debuggerView;
		}
		/**
		 * @see IPartListener#partActivated(IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part) {
			if (part == fDebuggerView) {
				fContextChanged= false;
			}
		}
		/**
		 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
		 */
		public void partBroughtToTop(IWorkbenchPart arg0) {
		}

		/**
		 * @see IPartListener#partClosed(IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part) {
			if (part == fDebuggerView) {
				fContextChanged= true;
			}
		}

		/**
		 * @see IPartListener#partDeactivated(IWorkbenchPart)
		 */
		public void partDeactivated(IWorkbenchPart part) {
			if (part == fDebuggerView) {
				fContextChanged= true;
			}
		}

		/**
		 * @see IPartListener#partOpened(IWorkbenchPart)
		 */
		public void partOpened(IWorkbenchPart arg0) {
		}
		
		/**
		 * @see IPageListener#pageActivated(IWorkbenchPage)
		 */
		public void pageActivated(IWorkbenchPage arg0) {
		}

		/**
		 * @see IPageListener#pageClosed(IWorkbenchPage)
		 */
		public void pageClosed(IWorkbenchPage page) {
			if (page.equals(fPage)) {
				init(null);
				fContextChanged= true;
			}
		}

		/**
		 * @see IPageListener#pageOpened(IWorkbenchPage)
		 */
		public void pageOpened(IWorkbenchPage arg0) {
		}
	}

	/**
	 * Constructs the debug UI plugin
	 */
	public DebugUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgDebugUIPlugin= this;
	}

	/**
	 * On a SUSPEND event, show the debug view or if no debug view is open,
	 * switch to the perspective specified by the launcher.
	 *
	 * @see IDebugEventListener
	 */
	public void handleDebugEvent(final DebugEvent event) {
		// open the debugger if this is a suspend event and the debug view is not yet open
		// and the preferences are set to switch
		if (event.getKind() == DebugEvent.SUSPEND) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (fSwitchContext.contextChanged(ILaunchManager.DEBUG_MODE)) {
						if (showSuspendEvent(event)) {				
							if (event.getDetail() == event.BREAKPOINT || getPreferenceStore().getBoolean(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW)) {
								switchToDebugPerspective(event.getSource(), ILaunchManager.DEBUG_MODE);
							}
						} 
					}
					if (fSwitchContext.wasPageCreated()) {
						fSwitchContext.setPageCreated(false);
						LaunchesView view= fSwitchContext.getDebuggerView();
						if (view != null) {
							view.autoExpand(event.getSource(), true);
						}
					}
				}
			});
		}
	}

	/**
	 * Poll the filters to determine if the event should be shown
	 */
	protected boolean showSuspendEvent(DebugEvent event) {
		Object s= event.getSource();
		if (s instanceof ITerminate) {
			if (((ITerminate)s).isTerminated()) {
				return false;
			}
		}
		if (!fEventFilters.isEmpty()) {
			Object[] filters = fEventFilters.getListeners();
			for (int i = 0; i < filters.length; i++) {
				if (!((IDebugUIEventFilter)filters[i]).showDebugEvent(event)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Poll the filters to determine if the launch should be shown
	 */
	protected boolean showLaunch(ILaunch launch) {
		if (!fEventFilters.isEmpty()) {
			Object[] filters = fEventFilters.getListeners();
			for (int i = 0; i < filters.length; i++) {
				if (!((IDebugUIEventFilter)filters[i]).showLaunch(launch)) {
					return false;
				}
			}
		}
		return true;
	}

	/** 
	 * Opens the a workbench page with the layout specified by the launcher 
	 * if it had not already been opened in any workbench window.
	 * 
	 * A switch only occurs if a debug view is not present in any workbench window.
	 */
	protected void switchToDebugPerspective(Object source, String mode) {
		fSwitchContext.init(mode);
		String layoutId= getLauncherPerspective(source);
		findDebugPresentation(fSwitchContext, mode, layoutId);
		activateDebugLayoutPage(fSwitchContext, layoutId);
		// bring debug to the front
		activateDebuggerPart(fSwitchContext, mode);
		if (fSwitchContext.wasPageCreated()) {
			LaunchesView view2= fSwitchContext.getDebuggerView();
			if (view2 != null) {
				view2.autoExpand(source, true);
			}
		}
	}
	
	/**
	 * Debug ui thread safe access to a display
	 */
	protected Display getDisplay() {
		//we can rely on not creating a display as we 
		//prereq the base eclipse ui plugin.
		return Display.getDefault();
	}
	
	/**
	 * Activates (may include creates) a debugger part based on the mode
	 * in the specified switch context.
	 * Must be called in the UI thread.
	 */
	protected void activateDebuggerPart(SwitchContext switchContext, String mode) {
		String viewId= 
			mode.equals(ILaunchManager.DEBUG_MODE) ? IDebugUIConstants.ID_DEBUG_VIEW: IDebugUIConstants.ID_PROCESS_VIEW;
		
		LaunchesView debugPart= (LaunchesView) switchContext.getPage().findView(viewId);							
		
		if (debugPart == null) {
			switchContext.setPageCreated(true);
		}
		
	
		if (debugPart == null) {
			try {
				debugPart= (LaunchesView) switchContext.getPage().showView(viewId);							
			} catch (PartInitException pie) {
				IStatus status= new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), DebugException.INTERNAL_ERROR, pie.getMessage(), pie);
				errorDialog(getActiveWorkbenchWindow().getShell(), DebugUIMessages.getString("DebugUIPlugin.Problem_Switching_to_the_Debug_Perspective_1"), DebugUIMessages.getString("DebugUIPlugin.Exceptions_occurred_switching_to_the_specified_debug_layout._2"), status); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			switchContext.getPage().activate(debugPart);
		}
		
		switchContext.setDebuggerView(debugPart);
	}
	
	/**
	 * Checks all active pages for a debugger view.  
	 * If no debugger view is found, looks for a page in any window
	 * that has the specified debugger layout id.
	 * Must be called in UI thread.
	 */
	protected void findDebugPresentation(SwitchContext switchContext, String mode, String layoutId) {
		
		IWorkbenchWindow[] windows= getWorkbench().getWorkbenchWindows();
		IWorkbenchWindow activeWindow= getActiveWorkbenchWindow();
		
		//check the active page of the active window for
		//debug view
		LaunchesView part= findDebugPart(activeWindow, mode);
		if (part != null) {
			switchContext.setWindow(activeWindow);
			switchContext.setPage(part.getSite().getPage());
			return;
		}
		//check active pages of all windows for debug view
		int i;
		for (i= 0; i < windows.length; i++) {
			IWorkbenchWindow window= windows[i];
			LaunchesView lPart= findDebugPart(window, mode);
			if (lPart != null) {
				switchContext.setWindow(window);
				switchContext.setPage(lPart.getSite().getPage());
				return;
			}
		}
		
		//check the pages for the debugger layout.
		//check the pages of the active window first
		IWorkbenchPage page= null;
		IWorkbenchPage[] pages= activeWindow.getPages();
		for (i= 0; i < pages.length; i++) {
			if (pages[i].getPerspective().getId().equals(layoutId)) {
				page= pages[i];
				break;
			}
		}
		if (page != null) {
			switchContext.setWindow(activeWindow);
			switchContext.setPage(page);
			return;
		}

		i= 0;
		i: for (i= 0; i < windows.length; i++) {	
			IWorkbenchWindow window= windows[i];
			if (window.equals(activeWindow)) {
				continue;
			}
			pages= window.getPages();
			
			j: for (int j= 0; j < pages.length; j++) {
				if (pages[j].getPerspective().getId().equals(layoutId)) {
					page= pages[j];
					break i;
				}
			}
		} 
				
		if (page != null) {
			switchContext.setWindow(windows[i]);
			switchContext.setPage(page);
			return;
		} 
	}
	
	/**
	 * Returns a launches view if the specified window contains the debugger part for the
	 * specified debug mode.
	 */
	protected LaunchesView findDebugPart(IWorkbenchWindow window, String mode) {
		if (window == null) {
			return null;
		}
		IWorkbenchPage activePage= window.getActivePage();
		if (activePage == null) {
			return null;
		}
		IViewPart debugPart= null;
		if (mode == ILaunchManager.DEBUG_MODE) {
			debugPart= activePage.findView(IDebugUIConstants.ID_DEBUG_VIEW);							
		} else {
			debugPart= activePage.findView(IDebugUIConstants.ID_PROCESS_VIEW);
		}
		return (LaunchesView)debugPart;
	}
	
	/**
	 * Activates (may include creates) a debugger page with the
	 * specified layout within the switch context.
	 */
	protected void activateDebugLayoutPage(SwitchContext switchContext, String layoutId) {
		IWorkbenchPage page= switchContext.getPage();
		IWorkbenchWindow window= switchContext.getWindow();
		if (page == null) {
			try {
				IContainer root= ResourcesPlugin.getWorkspace().getRoot();
				// adhere to the workbench preference when openning the debugger
				AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
				String perspectiveSetting = plugin.getPreferenceStore().getString(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);
				
				IWorkbench wb = window.getWorkbench();
				if (perspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW)) {
					// in new window
					window= wb.openWorkbenchWindow(layoutId, root);
					page= window.getActivePage();
				} else if (perspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE)) {
					// in new page
					page= window.openPage(layoutId, root);
				} else if (perspectiveSetting.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE)) {
					// replace current
					page= window.getActivePage();
					if (page == null) {
						// no pages - open a new one
						page = window.openPage(layoutId, root);
					} else {
						page.setPerspective(wb.getPerspectiveRegistry().findPerspectiveWithId(layoutId));
					}
				}
				switchContext.setPageCreated(true);
			} catch (WorkbenchException e) {
				IStatus status= new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), DebugException.INTERNAL_ERROR, e.getMessage(), e);
				errorDialog(window.getShell(), DebugUIMessages.getString("DebugUIPlugin.Problem_Switching_to_the_Debug_Perspective_3"), DebugUIMessages.getString("DebugUIPlugin.Exceptions_occurred_switching_to_the_specified_debug_layout._4"), status); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			switchContext.setPage(page);
			switchContext.setWindow(window);
		} else {
			window.getShell().moveAbove(null);
			window.getShell().setFocus();
			window.setActivePage(page);
		}
	}
	/**
	 * Returns the launcher perspective specified in the launcher
	 * associated with the source of a <code>DebugEvent</code>
	 */
	protected String getLauncherPerspective(Object eventSource) {
		ILaunch launch= null;
		if (eventSource instanceof IDebugElement) {
			launch= ((IDebugElement) eventSource).getLaunch();
		} else
			if (eventSource instanceof ILaunch) {
				launch= (ILaunch) eventSource;
			}
		String perspectiveID= launch == null ? null : launch.getLauncher().getPerspectiveIdentifier();
		if (perspectiveID == null) {
			perspectiveID= IDebugUIConstants.ID_DEBUG_PERSPECTIVE;
		}
		return perspectiveID;
	}

	/**
	 * Returns the singleton instance of the debug plugin.
	 */
	public static DebugUIPlugin getDefault() {
		return fgDebugUIPlugin;
	}

	public static IDebugModelPresentation getModelPresentation() {
		if (fgPresentation == null) {
			fgPresentation = new DelegatingModelPresentation();
		}
		return fgPresentation;
	}
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

/**
 * Creates an extension.  If the extension plugin has not
 * been loaded a busy cursor will be activated during the duration of
 * the load.
 *
 * @param element the config element defining the extension
 * @param classAttribute the name of the attribute carrying the class
 * @returns the extension object
 */
public static Object createExtension(final IConfigurationElement element, final String classAttribute) throws CoreException {
	// If plugin has been loaded create extension.
	// Otherwise, show busy cursor then create extension.
	IPluginDescriptor plugin = element.getDeclaringExtension().getDeclaringPluginDescriptor();
	if (plugin.isPluginActivated()) {
		return element.createExecutableExtension(classAttribute);
	} else {
		final Object [] ret = new Object[1];
		final CoreException [] exc = new CoreException[1];
		BusyIndicator.showWhile(null, new Runnable() {
			public void run() {
				try {
					ret[0] = element.createExecutableExtension(classAttribute);
				} catch (CoreException e) {
					exc[0] = e;
				}
			}
		});
		if (exc[0] != null) {
			throw exc[0];
		}
		else {
			return ret[0];
		}
	}
}	
	
	protected ImageRegistry createImageRegistry() {
		return DebugPluginImages.initializeImageRegistry();
	}

	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * If a plug-in has been started, this method is automatically
	 * invoked by the platform core when the workbench is closed.
	 * <p> 
	 * This method is intended to perform simple termination
	 * of the plug-in environment. The platform may terminate invocations
	 * that do not complete in a timely fashion.
	 * </p><p>
	 * By default this will save the preference and dialog stores (if they are in use).
	 * </p><p>
	 * Subclasses which override this method must call super first.
	 * </p>
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		removeSelectionListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		fColorManager.dispose();
		Iterator docs= fConsoleDocuments.values().iterator();
		while (docs.hasNext()) {
			ConsoleDocument doc= (ConsoleDocument)docs.next();
			doc.removeDocumentListener(this);
			doc.close();
		}
		fSwitchContext.shutdown();
		try {
			persistLaunchHistory();
		} catch (IOException e) {
			logError(e);
		}
	}

	/**
	 * Starts up this plug-in.
	 * <p>
	 * This method is automatically invoked by the platform core
	 * the first time any code in the plug-in is executed.
	 * <p>
	 */
	public void startup() throws CoreException {
		DebugPlugin.getDefault().addDebugEventListener(this);
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchListener(this);
		addSelectionListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		//set up the docs for launches already registered
		ILaunch[] launches= launchManager.getLaunches();
		try {
			restoreLaunchHistory();
		} catch (IOException e) {
			logError(e);
		}
		for (int i = 0; i < launches.length; i++) {
			launchRegistered(launches[i]);
		}

		// Create & register the adapter factory that will dispense objects that 
		// know about the properties that different breakpoint types support
		IAdapterFactory factory = new BreakpointPropertiesAdapterFactory();
		Platform.getAdapterManager().registerAdapters(factory, IBreakpoint.class);
		
	}

	/**
	 * Adds the selection provider for the debug UI.
	 */
	public void addSelectionProvider(ISelectionProvider provider, IWorkbenchPart part) {
		fSelectionProviders.add(provider);
		fSelectionParts.add(part);
		provider.addSelectionChangedListener(this);
	}

	/**
	 * Removes the selection provider from the debug UI.
	 */
	public void removeSelectionProvider(ISelectionProvider provider, IWorkbenchPart part) {
		fSelectionProviders.remove(provider);
		fSelectionParts.remove(part);
		provider.removeSelectionChangedListener(this);
		selectionChanged(null);
	}

	/**
	 * Adds an <code>ISelectionListener</code> to the debug selection manager.
	 */
	public void addSelectionListener(ISelectionListener l) {
		fListeners.add(l);
	}

	/**
	 * Removes an <code>ISelectionListener</code> from the debug selection manager.
	 */
	public synchronized void removeSelectionListener(ISelectionListener l) {
		fListeners.remove(l);
	}

	/**
	 * Selection has changed in the debug selection provider.
	 * Notify the listeners.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		Object[] copiedListeners= fListeners.getListeners();
				
		ISelection selection= null;
		ISelectionProvider provider= null;
		IWorkbenchPart part= null;
		if (event != null) {
			selection= event.getSelection();
			provider= (ISelectionProvider) event.getSource();
			int index= fSelectionProviders.indexOf(provider);
			if (index == -1) {
				return;
			}
			part= (IWorkbenchPart) fSelectionParts.get(index);
		}
		for (int i= 0; i < copiedListeners.length; i++) {
			((ISelectionListener)copiedListeners[i]).selectionChanged(part, selection);
		}
	}

	/**
	 * Sets the console document for the specified process.
	 * If the document is <code>null</code> the mapping for the
	 * process is removed.
	 */
	public void setConsoleDocument(IProcess process, IDocument doc) {
		if (doc == null) {
			fConsoleDocuments.remove(process);
		} else {

			fConsoleDocuments.put(process, doc);
		}
	}

	/**
	 * Returns the correct document for the process, determining the current
	 * process if required (process argument is null).
	 */
	public IDocument getConsoleDocument(IProcess process) {
		return getConsoleDocument(process, true);
	}
	/**
	 * Returns the correct document for the process, determining the current
	 * process if specified.
	 */
	public IDocument getConsoleDocument(IProcess process, boolean determineCurrentProcess) {
		if (process != null) {
			IDocument document= (IDocument) fConsoleDocuments.get(process);
			if (document != null) {
				return document;
			}
			document= new ConsoleDocument(process);
			fConsoleDocuments.put(process, document);
			return document;
		}
		
		if (determineCurrentProcess) {
			if (getCurrentProcess() == null) {
				setCurrentProcess(determineCurrentProcess());
			}

			IProcess currentProcess= getCurrentProcess();
			if (currentProcess != null) {
				IDocument document= (IDocument) fConsoleDocuments.get(currentProcess);
				if (document != null) {
					return document;
				}
				document= new ConsoleDocument(currentProcess);
				fConsoleDocuments.put(currentProcess, document);
				return document;
			}
		}

		return new ConsoleDocument(null);
	}
	
	/**
	 * Returns the current document being displayed in the console
	 * view. Never returns <code>null</code>.
	 */
	public static IDocument getCurrentConsoleDocument() {
		DebugUIPlugin plugin = DebugUIPlugin.getDefault();
		IProcess currentProcess = plugin.getCurrentProcess();
		IDocument document = plugin.getConsoleDocument(currentProcess, true);
		return document;
	}
	/**
	 * Returns the color manager to use in the debug UI
	 */
	public ColorManager getColorManager() {
		return fColorManager;
	}

	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(IPreferenceStore prefs) {
		DebugPreferencePage.initDefaults(prefs);
		ConsolePreferencePage.initDefaults(prefs);
	}
	
	/**
	 * @see ISelectionListener
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (!(part instanceof LaunchesView)) {
			return;
		}
		
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		IWorkbenchPage page= window.getActivePage();
		if (page == null) {
			return;
		}
		Object input= null;
		if (sel instanceof IStructuredSelection) {
			input= ((IStructuredSelection) sel).getFirstElement();
		}
		ConsoleView consoleView= (ConsoleView)page.findView(IDebugUIConstants.ID_CONSOLE_VIEW);
		if (input == null) {
			if (consoleView != null && getCurrentProcess() != null) {
				consoleView.setViewerInput(getCurrentProcess());
			} else {
				IProcess currentProcess= determineCurrentProcess();
				if (currentProcess == null) { 
					setCurrentProcess(currentProcess);
					if (consoleView != null) {
						consoleView.setViewerInput(currentProcess);
					}
				}
			}
		} else {
			IProcess processFromInput= getProcessFromInput(input);
			if (processFromInput == null) {
				if (consoleView != null) {
					consoleView.setViewerInput(null, false);
				}
				setCurrentProcess(null);
				return;
			}
			if (!processFromInput.equals(getCurrentProcess())) {
				setCurrentProcess(processFromInput);
				if (consoleView != null) { 
					consoleView.setViewerInput(processFromInput);
				} else {
					IDocument doc= getConsoleDocument(processFromInput);
					if (doc != null) {
						doc.addDocumentListener(this);
					}
				}
			}
		}
	}
	
	/**
	 * Returns the current process to use as input for the console view.
	 * Returns the first <code>IProcess</code> that has a launch and is associated with a debug target.
	 * If no debug targets, returns the first <code>IProcess</code> found in the launch manager.
	 * Can return <code>null</code>.
	 */
	public IProcess determineCurrentProcess() {
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		IDebugTarget[] debugTargets= launchManager.getDebugTargets();
		for (int i = 0; i < debugTargets.length; i++) {
			IDebugTarget target= debugTargets[i];
			IProcess process= target.getProcess();
			if (process != null && process.getLaunch() != null) {
				return process;
			}
		}

		IProcess[] processes= launchManager.getProcesses();
		for (int i=0; i < processes.length; i++) {
			IProcess process= processes[i];
			if (process.getLaunch() != null) {
				return process;
			}
		}

		return null;
	}

	protected IProcess getProcessFromInput(Object input) {
		IProcess processInput= null;
		if (input instanceof IProcess) {
			processInput= (IProcess) input;
		} else
			if (input instanceof ILaunch) {
				IDebugTarget target= ((ILaunch) input).getDebugTarget();
				if (target != null) {
					processInput= target.getProcess();
				} else {
					IProcess[] processes= ((ILaunch) input).getProcesses();
					if ((processes != null) && (processes.length > 0)) {
						processInput= processes[0];
					}
				}
			} else
				if (input instanceof IDebugElement) {
					processInput= ((IDebugElement) input).getDebugTarget().getProcess();
				}

		if ((processInput == null) || (processInput.getLaunch() == null)) {
			return null;
		} else {
			return processInput;
		}
	}

	/**
	 * @see IDocumentListener
	 */
	public void documentAboutToBeChanged(final DocumentEvent e) {
		// if the prefence is set, show the conosle
		if (!getPreferenceStore().getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN)) {
			return;
		}
		
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = getActiveWorkbenchWindow();
				if (window != null) {
					IWorkbenchPage page= window.getActivePage();
					if (page != null) {
						try { // show the console
							ConsoleView consoleView= (ConsoleView)page.findView(IDebugUIConstants.ID_CONSOLE_VIEW);
							if(consoleView == null) {
								IWorkbenchPart activePart= page.getActivePart();
								consoleView= (ConsoleView)page.showView(IDebugUIConstants.ID_CONSOLE_VIEW);
								consoleView.setViewerInput(getCurrentProcess());
								//restore focus stolen by the creation of the console
								page.activate(activePart);
							} else {
								page.bringToTop(consoleView);
							}
						} catch (PartInitException pie) {
							logError(pie);
						}
					}
				}
			}
		});
	}

	/**
	 * @see IDocumentListener
	 */
	public void documentChanged(DocumentEvent e) {
	}
	
	public IProcess getCurrentProcess() {
		return fCurrentProcess;
	}
	public void setCurrentProcess(IProcess process) {
		if (fCurrentProcess != null) {
			getConsoleDocument(fCurrentProcess).removeDocumentListener(this);
		}
		fCurrentProcess= process;
		if (fCurrentProcess != null) {
			getConsoleDocument(fCurrentProcess).addDocumentListener(this);
		}
	}
	
	/**
	 * @see IResourceChangeListener 
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) {
			try {
				if (fgDeletedVisitor == null) {
					fgDeletedVisitor= new ResourceDeletedVisitor();
				}
				delta.accept(fgDeletedVisitor, false);
			} catch (CoreException ce) {
				logError(ce);
			}
		}		
	}
	
	/**
	 * @see ILaunchListener
	 */
	public void launchDeregistered(final ILaunch launch) {
		getDisplay().syncExec(new Runnable () {
			public void run() {
				IProcess[] processes= launch.getProcesses();
				for (int i= 0; i < processes.length; i++) {
					ConsoleDocument doc= (ConsoleDocument)getConsoleDocument(processes[i]);
					doc.removeDocumentListener(DebugUIPlugin.this);
					doc.close();
					setConsoleDocument(processes[i], null);
				}
				IProcess currentProcess= getCurrentProcess();
				if (currentProcess != null && currentProcess.getLaunch() == null) {
					fCurrentProcess= null;
				}
			}
		});
	}

	/**
	 * Must not assume that will only be called from the UI thread.
	 *
	 * @see ILaunchListener
	 */
	public void launchRegistered(final ILaunch launch) {
		if (launch.getLaunchConfiguration() != null) {
			// new launch configuration processing
			return;
		} else {		
			// old launcher processing
			updateHistories(launch);
			switchToDebugPerspectiveIfPreferred(launch);
		}
		
		getDisplay().syncExec(new Runnable () {
			public void run() {
				IProcess[] processes= launch.getProcesses();
				if (processes != null) {
					for (int i= 0; i < processes.length; i++) {
						ConsoleDocument doc= new ConsoleDocument(processes[i]);
						doc.startReading();
						setConsoleDocument(processes[i], doc);
					}
				}
			}
		});
		
		IProcess newProcess= null;
		IDebugTarget target= launch.getDebugTarget();
		if (target != null) {
			newProcess= target.getProcess();
		} else {
			IProcess[] processes= launch.getProcesses();
			if ((processes != null) && (processes.length > 0)) {
				newProcess= processes[processes.length - 1];
			}
		}
		setCurrentProcess(newProcess);
		setConsoleInput(newProcess);
	}
	
	/**
	 * In a thread safe manner, switches the workbench to the debug perspective  
	 * if the user preferences specify to do so.
	 */
	protected void switchToDebugPerspectiveIfPreferred(final ILaunch launch) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				String mode= launch.getLaunchMode();
				if (fSwitchContext.contextChanged(mode)) {	
					boolean isDebug= mode.equals(ILaunchManager.DEBUG_MODE);
					boolean doSwitch= userPreferenceToSwitchPerspective(isDebug);
					if (doSwitch && showLaunch(launch)) {
						switchToDebugPerspective(launch, mode);
					}
				} 
			}
		});
	}
	
	/**
	 * Must be called in the UI thread
	 */
	public boolean userPreferenceToSwitchPerspective(boolean isDebug) {
		IPreferenceStore prefs= getPreferenceStore();
		return isDebug ? prefs.getBoolean(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW) : prefs.getBoolean(IDebugUIConstants.PREF_AUTO_SHOW_PROCESS_VIEW);
	}
	
	/**
	 * Sets the input console view viewer input for all
	 * consoles that exist in a thread safe manner.
	 */
	protected void setConsoleInput(final IProcess process) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow[] windows= getWorkbench().getWorkbenchWindows();
				for (int j= 0; j < windows.length; j++) {
					IWorkbenchWindow window= windows[j];
					IWorkbenchPage[] pages= window.getPages();
					if (pages != null) {
						for (int i= 0; i < pages.length; i++) {
							IWorkbenchPage page= pages[i];
							ConsoleView consoleView= (ConsoleView)page.findView(IDebugUIConstants.ID_CONSOLE_VIEW);
							if (consoleView != null) {
								consoleView.setViewerInput(process);
							} 
						}
					}
				}
			}
		});
	}

	/**
	 * Returns the collection of most recent debug launches, which 
	 * can be empty.
	 *
	 * @return an array of launches
	 */	
	public LaunchHistoryElement[] getDebugHistory() {
		return getHistoryArray(fDebugHistory);
	}	
	
	/**
	 * Returns the set of most recent run launches, which can be empty.
	 *
	 * @return an array of launches
	 */
	public LaunchHistoryElement[] getRunHistory() {
		return getHistoryArray(fRunHistory);
	}
	
	protected LaunchHistoryElement[] getHistoryArray(Vector history) {
		LaunchHistoryElement[] array = new LaunchHistoryElement[history.size()];
		history.copyInto(array);
		return array;
	}
	
	/**
	 * Returns the most recent launch, or <code>null</code> if there
	 * have been no launches.
	 *	
	 * @return the last launch, or <code>null</code> if none
	 */	
	public LaunchHistoryElement getLastLaunch() {
		return fRecentLaunch;
	}
	
	/**
	 * Adjust all histories, removing deleted launches.
	 */
	protected void removeDeletedHistories() {
		removeDeletedHistories(fDebugHistory);
		removeDeletedHistories(fRunHistory);
	}	
	
	/**
	 * Update the given history, removing launches with no element.
	 */
	protected void removeDeletedHistories(Vector history) {
		List remove = null;
		Iterator iter = history.iterator();
		while (iter.hasNext()) {
			LaunchHistoryElement element = (LaunchHistoryElement)iter.next();
			if (element.getLaunchElement() == null) {
				if (remove == null) {
					remove = new ArrayList(1);
				}
				remove.add(element);
			}
		}
		if (remove != null) {
			iter = remove.iterator();
			while (iter.hasNext()) {
				history.remove(iter.next());
			}
		}
	}

	/**
	 * Given a launch, try to add it to both of the run & debug histories.
	 */
	protected void updateHistories(ILaunch launch) {
		if (isVisible(launch.getLauncher())) {
			String elementMemento = launch.getLauncher().getDelegate().getLaunchMemento(launch.getElement());
			if (elementMemento == null) {
				return;
			}
			updateHistory(ILaunchManager.DEBUG_MODE, fDebugHistory, launch, elementMemento);
			updateHistory(ILaunchManager.RUN_MODE, fRunHistory, launch, elementMemento);
		}
	}
	
	/**
	 * Add the given launch to the debug history if the
	 * launcher supports the debug mode.  
	 */
	protected void updateHistory(String mode, Vector history, ILaunch launch, String memento) {
		
		// First make sure the launcher used supports the mode of the history list
		ILauncher launcher= launch.getLauncher();
		Set supportedLauncherModes= launcher.getModes();
		if (!supportedLauncherModes.contains(mode)) {
			return;
		}
		
		// create new history item
		LaunchHistoryElement item= new LaunchHistoryElement(launcher.getIdentifier(), memento, mode, getModelPresentation().getText(launch));
		
		// update the most recent launch
		if (launch.getLaunchMode().equals(mode)) {
			fRecentLaunch = item;
		}
		
		// Look for an equivalent launch in the history list
		int index;
		
		index = history.indexOf(item);
		
		//It's already listed as the most recent launch, so nothing to do
		if (index == 0) {
			return;
		}
		
		// It's in the history, but not the most recent, so make it the most recent
		if (index > 0) {
			history.remove(item);
		} 			
		history.add(0, item);
		if (history.size() > MAX_HISTORY_SIZE) {
			history.remove(history.size() - 1);
		}	
	}	
	
	protected String getHistoryAsXML() throws IOException {

		org.w3c.dom.Document doc = new DocumentImpl();
		Element historyRootElement = doc.createElement("launchHistory"); //$NON-NLS-1$
		doc.appendChild(historyRootElement);
		
		List all = new ArrayList(fDebugHistory.size() + fRunHistory.size());
		all.addAll(fDebugHistory);
		all.addAll(fRunHistory);

		Iterator iter = all.iterator();
		while (iter.hasNext()) {
			Element historyElement =
				getHistoryEntryAsXMLElement(doc, (LaunchHistoryElement)iter.next());
			historyRootElement.appendChild(historyElement);
		}
		if (fRecentLaunch != null) {
			Element recent = getRecentLaunchAsXMLElement(doc, fRecentLaunch);
			historyRootElement.appendChild(recent);
		}

		// produce a String output
		StringWriter writer = new StringWriter();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				writer,
				format);
		serializer.asDOMSerializer().serialize(doc);
		return writer.toString();
			
	}
	
	protected Element getHistoryEntryAsXMLElement(org.w3c.dom.Document doc, LaunchHistoryElement element) {
		Element entry = doc.createElement("launch"); //$NON-NLS-1$
		setAttributes(entry, element);
		return entry;
	}
	
	protected Element getRecentLaunchAsXMLElement(org.w3c.dom.Document doc, LaunchHistoryElement element) {
		Element entry = doc.createElement("lastLaunch"); //$NON-NLS-1$
		setAttributes(entry, element);
		return entry;
	}
	
	protected void setAttributes(Element entry, LaunchHistoryElement element) {
		entry.setAttribute("launcherId", element.getLauncherIdentifier()); //$NON-NLS-1$
		entry.setAttribute("elementMemento", element.getElementMemento()); //$NON-NLS-1$
		entry.setAttribute("launchLabel", element.getLabel()); //$NON-NLS-1$
		entry.setAttribute("mode", element.getMode());		 //$NON-NLS-1$
	}
	
	
	protected void persistLaunchHistory() throws IOException {
		IPath path = getStateLocation();
		path = path.append("launchHistory.xml"); //$NON-NLS-1$
		String osPath = path.toOSString();
		File file = new File(osPath);
		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		writer.write(getHistoryAsXML());
		writer.close();
	}
	
	protected void restoreLaunchHistory() throws IOException {
		IPath path = getStateLocation();
		path = path.append("launchHistory.xml"); //$NON-NLS-1$
		String osPath = path.toOSString();
		File file = new File(osPath);
		
		if (!file.exists()) {
			// no history to restore
			return;
		}
		
		FileInputStream stream = new FileInputStream(file);
		Element rootHistoryElement = null;
		try {
			DocumentBuilder parser =
				DocumentBuilderFactory.newInstance().newDocumentBuilder();
			rootHistoryElement = parser.parse(new InputSource(stream)).getDocumentElement();
		} catch (SAXException e) {
			logError(e);
			return;
		} catch (ParserConfigurationException e) {
			logError(e);
			return;
		} finally {
			stream.close();
		}
		if (!rootHistoryElement.getNodeName().equalsIgnoreCase("launchHistory")) { //$NON-NLS-1$
			return;
		}
		NodeList list = rootHistoryElement.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase("launch")) { //$NON-NLS-1$
					LaunchHistoryElement item = createHistoryElement(entry);
					if (item.getMode().equals(ILaunchManager.DEBUG_MODE)) {
						fDebugHistory.add(item);
					} else {
						fRunHistory.add(item);
					}
				} else if (entry.getNodeName().equalsIgnoreCase("lastLaunch")) { //$NON-NLS-1$
					fRecentLaunch = createHistoryElement(entry);
				}
			}
		}
	}
	
	public LaunchHistoryElement createHistoryElement(Element entry) {
		String launcherId = entry.getAttribute("launcherId"); //$NON-NLS-1$
		String mode = entry.getAttribute("mode"); //$NON-NLS-1$
		String memento = entry.getAttribute("elementMemento"); //$NON-NLS-1$
		String label = entry.getAttribute("launchLabel"); //$NON-NLS-1$
		return new LaunchHistoryElement(launcherId, memento, mode, label);
	}
	
	public void addEventFilter(IDebugUIEventFilter filter) {
		fEventFilters.add(filter);
	}
	
	/**
	 * Removes the event filter after the current set
	 * of events posted to the queue have been processed.
	 */
	public void removeEventFilter(final IDebugUIEventFilter filter) {
		Runnable runnable = new Runnable() {
			public void run() {
				fEventFilters.remove(filter);
			}
		};
		getDisplay().asyncExec(runnable);
	}
	
	/**
	 * Returns whether the given launcher should be visible in the UI.
	 * If a launcher is not visible, it will not appear
	 * in the UI - i.e. not as a default launcher, not in the run/debug
	 * drop downs, and not in the launch history.
	 * Based on the public attribute.
	 */
	public boolean isVisible(ILauncher launcher) {
		IConfigurationElement e = launcher.getConfigurationElement();
		String publc=  e.getAttribute("public"); //$NON-NLS-1$
		if (publc == null || publc.equals("true")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	/**
	 * Returns whether the given launcher specifies a wizard.
	 */
	public boolean hasWizard(ILauncher launcher) {
		IConfigurationElement e = launcher.getConfigurationElement();
		return e.getAttribute("wizard") != null; //$NON-NLS-1$
	}
	
	/**
	 * Utility method with conventions
	 */
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		// if the 'message' resource string and the IStatus' message are the same,
		// don't show both in the dialog
		if (s != null && message.equals(s.getMessage())) {
			message= null;
		}
		ErrorDialog.openError(shell, title, message, s);
	}

	/**
	 * Convenience method to log internal UI errors
	 */
	public static void logError(Exception e) {
		if (getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			System.out.println("Internal error logged from Debug UI: "); //$NON-NLS-1$
			e.printStackTrace();
			System.out.println();
		}
	}
}

