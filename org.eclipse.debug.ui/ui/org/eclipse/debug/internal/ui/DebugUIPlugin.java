package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;import org.eclipse.core.resources.*;import org.eclipse.core.runtime.*;import org.eclipse.debug.core.*;import org.eclipse.debug.core.model.*;import org.eclipse.debug.ui.IDebugUIConstants;import org.eclipse.debug.ui.IDebugUIEventFilter;import org.eclipse.jface.preference.IPreferenceStore;import org.eclipse.jface.preference.PreferenceConverter;import org.eclipse.jface.resource.ImageRegistry;import org.eclipse.jface.text.*;import org.eclipse.jface.util.ListenerList;import org.eclipse.jface.viewers.*;import org.eclipse.swt.custom.BusyIndicator;import org.eclipse.swt.graphics.RGB;import org.eclipse.swt.widgets.Display;import org.eclipse.swt.widgets.Shell;import org.eclipse.ui.*;import org.eclipse.ui.model.IWorkbenchAdapter;import org.eclipse.ui.plugin.AbstractUIPlugin;

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
	protected ILaunch fRecentLaunch = null;
	
	protected final static int MAX_HISTORY_SIZE= 5;
	/**
	 * The most recent debug launches
	 */
	protected ILaunch[] fDebugHistory = new ILaunch[MAX_HISTORY_SIZE];
	
	/**
	 * The launched resources for the most recent debug launches.
	 * The position of each resource corresponds to the position of the 
	 * associated launch in fDebugHistory.
	 */
	protected IResource[] fDebugHistoryResources = new IResource[MAX_HISTORY_SIZE];

	/**
	 * The most recent run launches
	 */
	protected ILaunch[] fRunHistory = new ILaunch[MAX_HISTORY_SIZE];
	
	/**
	 * The launched resources for the most recent run launches
	 * The position of each resource corresponds to the position of the 
	 * associated launch in fRunHistory.
	 */
	protected IResource[] fRunHistoryResources = new IResource[MAX_HISTORY_SIZE];
	
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
	 * Visitor for handling resource deltas
	 */
	class ResourceDeletedVisitor implements IResourceDeltaVisitor {
		
		/**
		 * @see IResourceDeltaVisitor
		 */
		public boolean visit(IResourceDelta delta) {
			if (delta.getKind() != IResourceDelta.REMOVED) {
				return true;
			}
			IResource resource= delta.getResource();
			updateHistoriesForDeletedResource(resource);
			return true;
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
	 * On a SUSPEND event, switch to the perspective specified
	 * by the launcher.
	 *
	 * @see IDebugEventListener
	 */
	public void handleDebugEvent(final DebugEvent event) {
		// open the debugger if this is a suspend event and the debugger is not yet open
		// and the preferences are set to switch
		if (event.getKind() == DebugEvent.SUSPEND) {
			Display display= getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						if (showSuspendEvent(event)) {				
							if (getPreferenceStore().getBoolean(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW)) {
								switchToDebugPerspective0(event.getSource(), ILaunchManager.DEBUG_MODE, true);
							}
						}
					}
				});
			}
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
	 * If the switch is suspend triggered, a switch only occurs if 
	 * a debug view is not present in any workbench window.
	 */
	private void switchToDebugPerspective0(Object source, String mode, boolean suspendTriggered) {
		String layoutId= getLauncherPerspective(source);
		Object[] results= findDebugPresentation(source, mode, layoutId, suspendTriggered);
		if (results == null) {
			return;
		} 
		IWorkbenchWindow window= null;
		IWorkbenchPage page= null;
		if (results.length == 0) {
			window= getActiveWorkbenchWindow();
		} else {
			window= (IWorkbenchWindow)results[0];
			page= (IWorkbenchPage)results[1];
		}
				
		page= activateDebugLayoutPage(page, window, layoutId);
		// bring debug to the front
		activateDebuggerPart(source, page, mode);
	}
	
	/**
	 * Debug ui thread safe access to a display
	 */
	protected Display getDisplay() {
		IWorkbench workbench= getWorkbench();
		if (workbench != null) {
			IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
			Display display= null;
			if (windows != null && windows.length > 0) {
				Shell shell= windows[0].getShell();
				if (!shell.isDisposed()) {
					return shell.getDisplay();
				}
			}
		}
		return null;
	}
	
	/**
	 * Activates (may include creates) a debugger part based on the mode
	 * in the specified page.
	 */
	protected void activateDebuggerPart(final Object source, IWorkbenchPage page, String mode) {
		LaunchesView debugPart= null;
		try {
			if (mode == ILaunchManager.DEBUG_MODE) {
				debugPart= (LaunchesView) page.showView(IDebugUIConstants.ID_DEBUG_VIEW);							
			} else {
				debugPart= (LaunchesView) page.showView(IDebugUIConstants.ID_PROCESS_VIEW);
			}
		} catch (PartInitException pie) {
			IStatus status= new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), IDebugStatusConstants.INTERNAL_ERROR, pie.getMessage(), pie);
			DebugUIUtils.errorDialog(page.getWorkbenchWindow().getShell(), "debug_ui_plugin.switch_perspective.error.", status);
		}
		if (debugPart != null) {
			final LaunchesView dp= debugPart;
			Display display= getDisplay();
			if (display != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						dp.autoExpand(source);
					}
				});
			}
		}
	}
	
	/**
	 * Checks all active pages for a debugger view.  
	 * If no debugger view is found, looks for a page in any window
	 * that has the specified debugger layout id.
	 *
	 * @return an Object array that 
	 * 		is null if a debugger part has been found
	 * 		is empty if no page could be found with the debugger layout id
	 * 		has two elements if a debugger layout page is found
	 *			 first element is a window, the second is a page
	 */
	protected Object[] findDebugPresentation(final Object source, String mode, String layoutId, boolean suspendTriggered) {
		Display display= getDisplay();
		if (display == null) {
			return null;
		}
		IWorkbenchWindow[] windows= getWorkbench().getWorkbenchWindows();
		IWorkbenchWindow activeWindow= getActiveWorkbenchWindow();
		int i;
		if (suspendTriggered) {
			final LaunchesView part= findDebugPart(activeWindow, mode);
			if (part != null) {
				display.asyncExec(new Runnable() {
					public void run() {
						part.autoExpand(source);
					}
				});
				return null;
			}
			//check active pages for debugger view
			for (i= 0; i < windows.length; i++) {
				IWorkbenchWindow window= windows[i];
				final LaunchesView lPart= findDebugPart(window, mode);
				if (lPart != null) {
					display.asyncExec(new Runnable() {
						public void run() {
							lPart.autoExpand(source);
						}
					});
					return null;
				}
			}
		} 
		
		//check the pages for the debugger layout.
		//check the pages of the active window first
		IWorkbenchPage page= null;
		IWorkbenchPage[] pages= activeWindow.getPages();
		for (int j= 0; j < pages.length; j++) {
			if (pages[j].getPerspective().getId().equals(layoutId)) {
				page= pages[j];
				break;
			}
		}
		if (page != null) {
			return new Object[]{activeWindow, page};
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
			return new Object[]{windows[i], page};
		} 
		
		return new Object[0];
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
	 * Activates (may include creates) and returns a debugger page with the
	 * specified layout in the supplied window.
	 *
	 * @return the page that was activated (and possibly created)
	 */
	protected IWorkbenchPage activateDebugLayoutPage(IWorkbenchPage page, IWorkbenchWindow window, String layoutId) {
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
			} catch (WorkbenchException e) {
				IStatus status= new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), IDebugStatusConstants.INTERNAL_ERROR, e.getMessage(), e);
				DebugUIUtils.errorDialog(window.getShell(), "debug_ui_plugin.switch_perspective.error.", status);
				return null;
			}
		} else {
			window.getShell().moveAbove(null);
			window.setActivePage(page);
		}
		return page;
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
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
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
		for (int i = 0; i < launches.length; i++) {
			launchRegistered(launches[i]);
		}
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
			document= new ConsoleDocument((IProcess) process);
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
	 * Returns the color manager to use in the debug UI
	 */
	public ColorManager getColorManager() {
		return fColorManager;
	}

	/**
	 * @see AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(IPreferenceStore prefs) {
		prefs.setDefault(IDebugUIConstants.PREF_AUTO_SHOW_DEBUG_VIEW, true);
		prefs.setDefault(IDebugUIConstants.PREF_AUTO_SHOW_PROCESS_VIEW, true);
		prefs.setDefault(IDebugPreferenceConstants.CONSOLE_OPEN, true);

		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_OUT_RGB, new RGB(0, 0, 255));
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_IN_RGB, new RGB(0, 200, 125));
		PreferenceConverter.setDefault(prefs, IDebugPreferenceConstants.CONSOLE_SYS_ERR_RGB, new RGB(255, 0, 0));
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
		if ((processes != null) && (processes.length > 0)) {
			return processes[0];
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
					processInput= ((IDebugElement) input).getProcess();
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
		
		Display display= getDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
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
							}
						}
					}
				}
			});
		}
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
				DebugUIUtils.logError(ce);
			}
		}		
	}
	

	/**
	 * @see ILaunchListener
	 */
	public void launchDeregistered(ILaunch launch) {
		Display display= getDisplay();
		if (display != null) {
			display.syncExec(new Runnable () {
				public void run() {
					IProcess currentProcess= getCurrentProcess();
					if (currentProcess != null && currentProcess.getLaunch() == null) {
						ConsoleDocument doc= (ConsoleDocument)getConsoleDocument(currentProcess);
						doc.removeDocumentListener(DebugUIPlugin.this);
						doc.close();
						setConsoleDocument(currentProcess, null);
						fCurrentProcess= null;
					}
				}
			});
		} 
		setCurrentProcess(determineCurrentProcess());
		setConsoleInput(getCurrentProcess());
	}

	/**
	 * Must not assume that will only be called from the UI thread.
	 *
	 * @see ILaunchListener
	 */
	public void launchRegistered(final ILaunch launch) {
		fRecentLaunch = launch;
		updateHistories(launch);
		switchToDebugPerspectiveIfPreferred(launch);
		
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

		Display display= getDisplay();
		if (display != null) {
			display.syncExec(new Runnable () {
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
		}
	
		setCurrentProcess(newProcess);
		setConsoleInput(newProcess);
	}
	
	/**
	 * In a thread safe manner, switches the workbench to the debug perspective  
	 * if the user preferences specify to do so.
	 */
	protected void switchToDebugPerspectiveIfPreferred(final ILaunch launch) {
		Display display= getDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					String mode= launch.getLaunchMode();
					boolean isDebug= mode.equals(ILaunchManager.DEBUG_MODE);
					boolean doSwitch= userPreferenceToSwitchPerspective(isDebug);
					if (doSwitch) {
						if (showLaunch(launch)) {	
							switchToDebugPerspective0(launch, mode, false);
						}
					}
				}
			});
		}
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
		Display display= getDisplay();
		if (display != null) {
			display.asyncExec(new Runnable() {
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
	}

	/**
	 * Returns the collection of most recent debug launches, which 
	 * can be empty.
	 *
	 * @return an array of launches
	 */	
	public ILaunch[] getDebugHistory() {
		return fDebugHistory;
	}	
	
	/**
	 * Returns the set of most recent run launches, which can be empty.
	 *
	 * @return an array of launches
	 */
	public ILaunch[] getRunHistory() {
		return fRunHistory;
	}
	
	/**
	 * Returns the most recent launch, or <code>null</code> if there
	 * have been no launches.
	 *	
	 * @return the last launch, or <code>null</code> if none
	 */	
	public ILaunch getLastLaunch() {
		return fRecentLaunch;
	}
	
	/**
	 * Adjust all histories for the given deleted resource.
	 */
	protected void updateHistoriesForDeletedResource(IResource deletedResource) {
		updateHistoryForDeletedResource(deletedResource, fDebugHistory, fDebugHistoryResources);
		updateHistoryForDeletedResource(deletedResource, fRunHistory,   fRunHistoryResources);
	}	
	
	/**
	 * Remove the given resource from the resource history, and remove the corresponding
	 * launch from the launch history.
	 */
	protected void updateHistoryForDeletedResource(IResource deletedResource, ILaunch[] launchHistory, IResource[] resourceHistory) {
		int resourceLength = resourceHistory.length;
		for (int i = 0; i < resourceLength; i++) {
			if (deletedResource.equals(resourceHistory[i])) {
				if (i < resourceLength - 1) {
					System.arraycopy(resourceHistory, i + 1, resourceHistory, i, resourceLength - (i + 1));
					System.arraycopy(launchHistory, i + 1, launchHistory, i, resourceLength - (i + 1));					
				}
				resourceHistory[resourceLength - 1]= null;
				launchHistory[resourceLength - 1]= null;			
			}
		}
	}

	/**
	 * Given a launch, try to add it to both of the run & debug histories.
	 */
	protected void updateHistories(ILaunch launch) {
		if (isVisible(launch.getLauncher())) {
			IResource resource= getResourceForLaunch(launch);
			String launchMode= launch.getLaunchMode();
			String otherLaunchMode= launchMode == ILaunchManager.DEBUG_MODE ? ILaunchManager.RUN_MODE : ILaunchManager.DEBUG_MODE;
			ILaunch[] history= launchMode == ILaunchManager.DEBUG_MODE ? fDebugHistory : fRunHistory;
			ILaunch[] otherHistory= otherLaunchMode == ILaunchManager.RUN_MODE ? fRunHistory : fDebugHistory;			
			IResource[] resourceHistory= launchMode == ILaunchManager.DEBUG_MODE ? fDebugHistoryResources : fRunHistoryResources;
			IResource[] otherResourceHistory= otherLaunchMode == ILaunchManager.RUN_MODE ? fRunHistoryResources : fDebugHistoryResources;			
			updateHistory(history, launchMode, launch, resource, resourceHistory);
			updateHistory(otherHistory, otherLaunchMode, launch, resource, otherResourceHistory);		
		}
	}
	
	/**
	 * Add the given launch to the given history (whose mode is specified by 
	 * historyMode) if the launcher used by the launch supports the history mode.  
	 */
	protected void updateHistory(ILaunch[] history, String historyMode, ILaunch launch, IResource resource, IResource[] resourceHistory) {
		
		// First make sure the launcher used supports the mode of the history list
		ILauncher launcher= launch.getLauncher();
		Set supportedLauncherModes= launcher.getModes();
		if (!supportedLauncherModes.contains(historyMode)) {
			return;
		}
		
		// Look for the launch in the history list
		int index;
		boolean found= false;
		for (index= 0; index < history.length; index++) {
			ILaunch historyLaunch= history[index];
			if (historyLaunch != null && historyLaunch.getElement().equals(launch.getElement()) &&
			    launcher.equals(historyLaunch.getLauncher())) { 
					found= true;
					break;
			}
		}
		
		//It's already listed as the most recent launch, so nothing to do
		if (index == 0 && found) {
			return;
		}
		
		// It's in the history, but not the most recent, so make it the most recent
		if (found) {
			//removes from old position
			System.arraycopy(history, index + 1, history, index, history.length - (index + 1));
			System.arraycopy(resourceHistory, index + 1, resourceHistory, index, resourceHistory.length - (index + 1));
		} 			
		
		// It's not in the history, so add it to the beginning, removing the last 
		// launch in the list if the list is already at maximum size	
		System.arraycopy(history, 0, history, 1, history.length - 1);
		System.arraycopy(resourceHistory, 0, resourceHistory, 1, resourceHistory.length - 1);
		history[0]= launch;	
		resourceHistory[0]= resource;
	}	
	
	/**
	 * Return the IResource adapter associated with the given launch, or null 
	 * if there is none.
	 */
	protected IResource getResourceForLaunch(ILaunch launch) {
		Object candidate= launch.getElement();
		IResource resource= null;
		// If the launched element doesn't have an IResource adapter, work up the parental
		// chain until we find someone who does
		while (candidate instanceof IAdaptable) {
			IResource candidateResource= (IResource)((IAdaptable)candidate).getAdapter(IResource.class);
			if (candidateResource != null) {
				return candidateResource;
			}
			IWorkbenchAdapter workbenchAdapter= (IWorkbenchAdapter)((IAdaptable)candidate).getAdapter(IWorkbenchAdapter.class);
			if (workbenchAdapter != null) {
				candidate= workbenchAdapter.getParent(candidate);
			} else {
				return null;
			}
		}		
		return resource;
	}
	
	public void addEventFilter(IDebugUIEventFilter filter) {
		fEventFilters.add(filter);
	}
	
	/**
	 * Removes the event filter after the current set
	 * of events posted to the queue have been processed.
	 */
	public void removeEventFilter(final IDebugUIEventFilter filter) {
		Display display= getDisplay();
		if (display != null) {
			Runnable runnable = new Runnable() {
				public void run() {
					fEventFilters.remove(filter);
				}
			};
			display.asyncExec(runnable);
		}
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
		String publc=  e.getAttribute("public");
		if (publc == null || publc.equals("true")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns whether the given launcher specifies a wizard.
	 */
	public boolean hasWizard(ILauncher launcher) {
		IConfigurationElement e = launcher.getConfigurationElement();
		return e.getAttribute("wizard") != null;
	}
}

