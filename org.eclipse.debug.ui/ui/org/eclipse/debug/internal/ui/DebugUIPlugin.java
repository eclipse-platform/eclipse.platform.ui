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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.debug.internal.ui.launchConfigurations.PerspectiveManager;
import org.eclipse.debug.internal.ui.views.ConsoleDocument;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
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
public class DebugUIPlugin extends AbstractUIPlugin implements ILaunchListener,
															   IResourceChangeListener, 
															   IPropertyChangeListener, 
															   ILaunchConfigurationListener {															   
															   	

	/**
	 * The singleton debug plugin instance
	 */
	private static DebugUIPlugin fgDebugUIPlugin= null;
	
	/**
	 * A utility presentation used to obtain labels
	 */
	protected static IDebugModelPresentation fgPresentation = null;

	/**
	 * Default label provider
	 */	
	private static DefaultLabelProvider fgDefaultLabelProvider;

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
	protected LaunchConfigurationHistoryElement fRecentLaunch = null;
	
	protected final static int MAX_HISTORY_SIZE= 5;
	/**
	 * The most recent debug launches
	 */
	protected Vector fDebugHistory;
	
	/**
	 * The most recent run launches
	 */
	protected Vector fRunHistory;
	
	/**
	 * The most recent debug launches
	 */
	protected Vector fDebugFavorites;
	
	/**
	 * The most recent run launches
	 */
	protected Vector fRunFavorites;	
	
	/**
	 * Flag indicating whether the debug UI is in trace
	 * mode. When in trace mode, extra debug information
	 * is produced.
	 */
	private boolean fTrace = false;	
	
	/**
	 * The visitor used to traverse resource deltas and keep the run & debug
	 * histories in synch with resource deletions.
	 */
	protected static ResourceDeletedVisitor fgDeletedVisitor;
	
	/**
	 * The mapping of launch configuration type IDs to lists of perspectives.
	 * A shortcut for bringing up the launch configuration dialog initialized to
	 * the specified config type will appear in each specified perspective.
	 */
	protected Map fLaunchConfigurationShortcuts;
	
	/**
	 * Visitor for handling resource deltas
	 */
	class ResourceDeletedVisitor implements IResourceDeltaVisitor {
		
		/**
		 * @see IResourceDeltaVisitor#visit(IResourceDelta)
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
	 * The names of the files used to persist the launch history.
	 */
	private static final String LAUNCH_CONFIGURATION_HISTORY_FILENAME = "launchConfigurationHistory.xml"; //$NON-NLS-1$
	private static final String LAUNCH_OLD_HISTORY_FILENAME = "launchHistory.xml"; //$NON-NLS-1$
	
	private static final String HISTORY_ROOT_NODE = "launchHistory"; //$NON-NLS-1$
	private static final String HISTORY_LAUNCH_NODE = "launch"; //$NON-NLS-1$
	private static final String HISTORY_LAST_LAUNCH_NODE = "lastLaunch"; //$NON-NLS-1$
	private static final String HISTORY_MEMENTO_ATT = "memento"; //$NON-NLS-1$
	private static final String HISTORY_MODE_ATT = "mode"; //$NON-NLS-1$
	private static final String HISTORY_LABEL_ATT = "label"; //$NON-NLS-1$
	
	/**
	 * Returns whether the debug UI plug-in is in trace
	 * mode.
	 * 
	 * @return whether the debug UI plug-in is in trace
	 *  mode
	 */
	public boolean isTraceMode() {
		return fTrace;
	}
	
	/**
	 * Logs the given message if in trace mode.
	 * 
	 * @param String message to log
	 */
	public static void logTraceMessage(String message) {
		if (getDefault().isTraceMode()) {
			IStatus s = new Status(IStatus.WARNING, IDebugUIConstants.PLUGIN_ID, IDebugUIConstants.INTERNAL_ERROR, message, null);
			getDefault().getLog().log(s);
		}
	}

	/**
	 * Constructs the debug UI plugin
	 */
	public DebugUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgDebugUIPlugin= this;
		setEmptyLaunchHistories();
	}		
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
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
		String perspectiveID= null;
		if (launch != null) {
			if (launch.getLauncher() != null) {
				perspectiveID= launch.getLauncher().getPerspectiveIdentifier();
			}
		} 
		if (perspectiveID == null) {
			perspectiveID = IDebugUIConstants.ID_DEBUG_PERSPECTIVE;
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
	
	public static Shell getShell() {
		return getActiveWorkbenchWindow().getShell();
	}
	/**
	 * Returns the default label provider for the debug UI.
	 */
	public static ILabelProvider getDefaultLabelProvider() {
		if (fgDefaultLabelProvider == null) {
			fgDefaultLabelProvider = new DefaultLabelProvider();
		}
		return fgDefaultLabelProvider;
	}

	/**
	 * Creates an extension.  If the extension plugin has not
	 * been loaded a busy cursor will be activated during the duration of
	 * the load.
	 *
	 * @param element the config element defining the extension
	 * @param classAttribute the name of the attribute carrying the class
	 * @return the extension object
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
		
		// shutdown the perspective manager
		PerspectiveManager.getDefault().shutdown();		
		DebugActionGroupsManager.getDefault().shutdown();
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.removeLaunchListener(this);
		launchManager.removeLaunchConfigurationListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		getPreferenceStore().removePropertyChangeListener(this);
		fColorManager.dispose();
		Iterator docs= fConsoleDocuments.values().iterator();
		while (docs.hasNext()) {
			ConsoleDocument doc= (ConsoleDocument)docs.next();
			doc.close();
		}
		try {
			persistLaunchHistory();
		} catch (IOException e) {
			log(e);
		}
		if (fgPresentation != null) {
			fgPresentation.dispose();
		}
	}

	/**
	 * @see AbstractUIPlugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		ILaunchManager launchManager= DebugPlugin.getDefault().getLaunchManager();
		launchManager.addLaunchListener(this);	
		launchManager.addLaunchConfigurationListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		getPreferenceStore().addPropertyChangeListener(this);
		//set up the docs for launches already registered
		ILaunch[] launches= launchManager.getLaunches();
		try {
			restoreLaunchHistory();
		} catch (IOException e) {
			log(e);
		}
		for (int i = 0; i < launches.length; i++) {
			launchAdded(launches[i]);
		}
		
		// startup the perspective manager
		PerspectiveManager.getDefault().startup();
		DebugActionGroupsManager.getDefault().startup();
		
		IAdapterManager manager= Platform.getAdapterManager();
		manager.registerAdapters(new DebugUIPropertiesAdapterFactory(), IDebugElement.class);
		manager.registerAdapters(new DebugUIPropertiesAdapterFactory(), IProcess.class);
		
		loadLaunchConfigurationShortcuts();

		getStandardDisplay().asyncExec(
			new Runnable() {
				public void run() {
					createImageRegistry();
				}
			}
		);
		
	}
	
	/**
	 * Load all registered extensions of the 'launch configuration shortcut' extension point.
	 */
	private void loadLaunchConfigurationShortcuts() {
		// Get the configuration elements
		IPluginDescriptor descriptor= getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IDebugUIConstants.EXTENSION_POINT_LAUNCH_CONFIGURATION_SHORTCUTS);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();

		// Load the configuration elements into a Map 
		fLaunchConfigurationShortcuts = new HashMap(infos.length);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configElement = infos[i];
			String configTypeID = configElement.getAttribute("configTypeID");
			IConfigurationElement[] children = configElement.getChildren("perspective");
			List perspChildren = new ArrayList(children.length);
			for (int j = 0; j < children.length; j++) {
				String perspID = children[j].getAttribute("id");
				perspChildren.add(perspID);
			}
			fLaunchConfigurationShortcuts.put(configTypeID, perspChildren);
		}
	}
	
	public Map getLaunchConfigurationShortcuts() {
		return fLaunchConfigurationShortcuts;
	}

	/**
	 * Sets the console document for the specified process.
	 * If the document is <code>null</code> the mapping for the
	 * process is removed.
	 */
	protected void setConsoleDocument(IProcess process, IDocument doc) {
		if (doc == null) {
			fConsoleDocuments.remove(process);
		} else {
			fConsoleDocuments.put(process, doc);
		}
	}
	
	/**
	 * Returns the document for the process, or <code>null</code>
	 * if none.
	 */
	public IDocument getConsoleDocument(IProcess process) {
		return (IDocument) fConsoleDocuments.get(process);
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

		return processInput;
	}

	/**
	 * Notifies the DebugUIPlugin that system out is about to be written
	 * to the console. The plugin will open the console if the preference is
	 * set to show the console on system out.
	 */	
	public void aboutToWriteSystemOut() {
		if (getPreferenceStore().getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT)) {
			showConsole();
		}
	}
	
	/**
	 * Notifies the DebugUIPlugin that system err is about to be written
	 * to the console. The plugin will open the console if the preference is
	 * set to show the console on system err.
	 */
	public void aboutToWriteSystemErr() {
		if (getPreferenceStore().getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR)) {
			showConsole();
		}
	}
	
	/**
	 * Opens the console view. If the view is already open, it is brought to the front.
	 */
	protected void showConsole() {
		getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = getActiveWorkbenchWindow();
				if (window != null) {
					IWorkbenchPage page= window.getActivePage();
					if (page != null) {
						try { // show the console
							IViewPart consoleView= page.findView(IDebugUIConstants.ID_CONSOLE_VIEW);
							if(consoleView == null) {
								IWorkbenchPart activePart= page.getActivePart();
								page.showView(IDebugUIConstants.ID_CONSOLE_VIEW);
								//restore focus stolen by the creation of the console
								page.activate(activePart);
							} else {
								page.bringToTop(consoleView);
							}
						} catch (PartInitException pie) {
							log(pie);
						}
					}
				}
			}
		});
	}

	/**
	 * @see IDocumentListener#documentChanged(DocumentEvent)
	 */
	public void documentChanged(DocumentEvent e) {
	}
	
	public IProcess getCurrentProcess() {
		return fCurrentProcess;
	}
	private void setCurrentProcess(IProcess process) {
		fCurrentProcess= process;
	}
	
	/**
	 * @see IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		// only traverse the delta if using old launchers
		if (usingConfigurationStyleLaunching()) {
			return;
		}
		
		IResourceDelta delta= event.getDelta();
		if (delta != null) {
			try {
				if (fgDeletedVisitor == null) {
					fgDeletedVisitor= new ResourceDeletedVisitor();
				}
				delta.accept(fgDeletedVisitor, false);
			} catch (CoreException ce) {
				log(ce);
			}
		}		
	}
	
	/**
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(final ILaunch launch) {
		getStandardDisplay().syncExec(new Runnable () {
			public void run() {
				IProcess currentProcess= getCurrentProcess();
				IProcess[] processes= launch.getProcesses();
				for (int i= 0; i < processes.length; i++) {
					IProcess iProcess = processes[i];
					ConsoleDocument doc= (ConsoleDocument)getConsoleDocument(iProcess);
					if (doc != null) {
						doc.close();
						setConsoleDocument(processes[i], null);
					}
					if (iProcess.equals(currentProcess)) {
						fCurrentProcess= null;
					}
				}
			}
		});
	}
	
	/**
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(final ILaunch launch) {	
		getStandardDisplay().syncExec(new Runnable () {
			public void run() {
				IProcess[] processes= launch.getProcesses();
				for (int i= 0; i < processes.length; i++) {
					if (getConsoleDocument(processes[i]) == null) {
						ConsoleDocument doc= new ConsoleDocument(processes[i]);
						doc.startReading();
						setConsoleDocument(processes[i], doc);
					}
				}
			}
		});		
	}

	/**
	 * Must not assume that will only be called from the UI thread.
	 *
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(final ILaunch launch) {
		updateHistories(launch);
		
		getStandardDisplay().syncExec(new Runnable () {
			public void run() {
				IProcess[] processes= launch.getProcesses();
				for (int i= 0; i < processes.length; i++) {
					ConsoleDocument doc= new ConsoleDocument(processes[i]);
					doc.startReading();
					setConsoleDocument(processes[i], doc);
				}
			}
		});
		
		IProcess newProcess= null;
		IDebugTarget target= launch.getDebugTarget();
		if (target != null) {
			newProcess= target.getProcess();
		} else {
			IProcess[] processes= launch.getProcesses();
			if (processes.length > 0) {
				newProcess= processes[processes.length - 1];
			}
		}
		setCurrentProcess(newProcess);
	}
	
	protected void updateFavorites(ILaunchConfiguration config) {
		try {
			if (config.getAttribute(IDebugUIConstants.ATTR_DEBUG_FAVORITE, false)) {
				addDebugFavorite(config);
				removeLaunchConfigurationFromHistoryList(fDebugHistory, config);
			} else {
				removeDebugFavorite(config);
			}
			if (config.getAttribute(IDebugUIConstants.ATTR_RUN_FAVORITE, false)) {
				addRunFavorite(config);
				removeLaunchConfigurationFromHistoryList(fRunHistory, config);
			} else {
				removeRunFavorite(config);
			}
		} catch (CoreException e) {
			log(e);
		}	
	}
	
	/**
	 * @see ILaunchConfigurationListener#launchConfigurationAdded(ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration config) {		
		updateFavorites(config);
	}
	
	/**
	 * @see ILaunchConfigurationListener#launchConfigurationChanged(ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration config) {		
		if (!config.isWorkingCopy()) {
			updateFavorites(config);
		}
	}
	
	/**
	 * If the deleted config appeared in either of the history lists, delete it from the list(s).
	 * 
	 * @see ILaunchConfigurationListener#launchConfigurationRemoved(ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration config) {
		removeLaunchConfigurationFromHistoryList(fRunHistory, config);
		removeLaunchConfigurationFromHistoryList(fDebugHistory, config);
		removeLaunchConfigurationFromHistoryList(fDebugFavorites, config);
		removeLaunchConfigurationFromHistoryList(fRunFavorites, config);
	}
	
	/**
	 * Remove the specified launch configuration from the specified history list.  If the 
	 * configuration does not appear in the list, this method does nothing.
	 */
	protected void removeLaunchConfigurationFromHistoryList(Vector list, ILaunchConfiguration config) {
		ListIterator iterator = list.listIterator();
		while (iterator.hasNext()) {
			LaunchConfigurationHistoryElement element = (LaunchConfigurationHistoryElement) iterator.next();
			ILaunchConfiguration elementConfig = element.getLaunchConfiguration();
			if (config.equals(elementConfig)) {
				iterator.remove();
			}
		}
	}
		
	/**
	 * Returns an array of the most recent debug launches, which can be empty.
	 *
	 * @return an array of launches
	 */	
	public LaunchConfigurationHistoryElement[] getDebugHistory() {
		return getHistoryArray(fDebugHistory);
	}
	
	/**
	 * Returns an array of the favorite debug launches, which can be empty.
	 *
	 * @return an array of launches
	 */	
	public LaunchConfigurationHistoryElement[] getDebugFavorites() {
		return getHistoryArray(fDebugFavorites);
	}
	
	/**
	 * Sets the favorite debug launches, which can be empty.
	 *
	 * @param favorites an array of launches
	 */	
	public void setDebugFavorites(Vector favorites) {
		fDebugFavorites = favorites;
	}	
	
	/**
	 * Sets the recent debug launches, which can be empty.
	 *
	 * @param hsitory an array of launches
	 */	
	public void setDebugHistory(Vector history) {
		fDebugHistory = history;
	}	
	
	/**
	 * Sets the recent run launches, which can be empty.
	 *
	 * @param hsitory an array of launches
	 */	
	public void setRunHistory(Vector history) {
		fRunHistory = history;
	}			
	
	/**
	 * Sets the favorite run launches, which can be empty.
	 *
	 * @param favorites an array of launches
	 */	
	public void setRunFavorites(Vector favorites) {
		fRunFavorites = favorites;
	}
		
	/**
	 * Returns an array of the most recent run launches, which can be empty.
	 *
	 * @return an array of launches
	 */
	public LaunchConfigurationHistoryElement[] getRunHistory() {
		return getHistoryArray(fRunHistory);
	}
	
	/**
	 * Returns an array of the favorite run launches, which can be empty.
	 *
	 * @return an array of launches
	 */
	public LaunchConfigurationHistoryElement[] getRunFavorites() {
		return getHistoryArray(fRunFavorites);
	}	
	
	protected LaunchConfigurationHistoryElement[] getHistoryArray(Vector history) {
		LaunchConfigurationHistoryElement[] array = new LaunchConfigurationHistoryElement[history.size()];
		history.copyInto(array);
		return array;
	}
	
	/**
	 * Returns the most recent launch, or <code>null</code> if there
	 * have been no launches.
	 *	
	 * @return the last launch, or <code>null</code> if none
	 */	
	public LaunchConfigurationHistoryElement getLastLaunch() {
		return fRecentLaunch;
	}
	
	/**
	 * Adjust all histories, removing deleted launches.
	 */
	protected void removeDeletedHistories() {
		Runnable r = new Runnable() {
			public void run() {
				removeDeletedHistories(fDebugHistory);
				removeDeletedHistories(fRunHistory);
			}
		};
		getStandardDisplay().asyncExec(r);
	}	
	
	/**
	 * Update the given history, removing launches with no element.
	 */
	protected void removeDeletedHistories(Vector history) {
		List remove = null;
		Iterator iter = history.iterator();
		while (iter.hasNext()) {
			LaunchConfigurationHistoryElement element = (LaunchConfigurationHistoryElement)iter.next();
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
	 * Return whether the preference is currently set to use configuration-style launching.
	 */
	public boolean usingConfigurationStyleLaunching() {
		String launchingStyle = getPreferenceStore().getString(IDebugPreferenceConstants.LAUNCHING_STYLE);
		if (IDebugPreferenceConstants.LAUNCHING_STYLE_CONFIGURATIONS.equals(launchingStyle)) {
			return true;
		} else {
			return false;
		}		
	}
	
	/**
	 * Erase both (run & debug) launch histories.
	 */
	protected void setEmptyLaunchHistories() {
		fRecentLaunch = null;
		fRunHistory = new Vector(MAX_HISTORY_SIZE);
		fDebugHistory = new Vector(MAX_HISTORY_SIZE);
		setRunFavorites(new Vector(MAX_HISTORY_SIZE));
		setDebugFavorites(new Vector(MAX_HISTORY_SIZE));		
	}
	
	/**
	 * Given a launch, try to add it to both of the run & debug histories.
	 */
	protected void updateHistories(ILaunch launch) {
		if (usingConfigurationStyleLaunching()) {
			updateNewHistories(launch);
		} else {
			updateOldHistories(launch);
		}
	}

	/**
	 * Update both history lists when they contain configurations (new style)
	 */
	protected void updateNewHistories(ILaunch launch) {
		//
		// ToDo: Do we need an equivalent of 'public' for launch config types?
		//
		//if (isVisible(launch.getLauncher())) {
			updateNewHistory(ILaunchManager.DEBUG_MODE, fDebugHistory, fDebugFavorites, launch);
			updateNewHistory(ILaunchManager.RUN_MODE, fRunHistory, fRunFavorites, launch);
		//}
	}
	
	/**
	 * Update both history lists when they contain launcher ids & launched element mementos (old style)
	 */
	protected void updateOldHistories(ILaunch launch) {
		if (isVisible(launch.getLauncher())) {
			String elementMemento = launch.getLauncher().getDelegate().getLaunchMemento(launch.getElement());
			if (elementMemento == null) {
				return;
			}
			updateOldHistory(ILaunchManager.DEBUG_MODE, fDebugHistory, launch, elementMemento);
			updateOldHistory(ILaunchManager.RUN_MODE, fRunHistory, launch, elementMemento);
		}		
	}
	
	/**
	 * Add the given launch to the specified history if the launcher supports the mode.  
	 */
	protected void updateNewHistory(String mode, Vector history, Vector favorites, ILaunch launch) {
		
		// First make sure the launch configuration exists, supports the mode of the history list,
		// and isn't private
		ILaunchConfiguration launchConfig = launch.getLaunchConfiguration();
		if (launchConfig == null) {
			return;
		}
		try {
			if (!launchConfig.supportsMode(mode) ||
				 launchConfig.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false)) {
				return;
			}
		} catch (CoreException ce) {
			return;
		}
		
		// Create a new history item
		String label = getModelPresentation().getText(launch);
		LaunchConfigurationHistoryElement item= new LaunchConfigurationHistoryElement(launchConfig, mode, label);
		
		// Update the most recent launch field
		if (launch.getLaunchMode().equals(mode)) {
			fRecentLaunch = item;
		}
		
		// Look for an equivalent launch in the favorites
		int index = findConfigInHistoryList(favorites, item.getLaunchConfiguration());
		if (index >= 0) {
			// a favorite, do not add to history
			return;
		}
		
		// Look for an equivalent launch in the history list
		index = findConfigInHistoryList(history, item.getLaunchConfiguration());
		
		//It's already listed as the most recent launch, so nothing to do
		if (index == 0) {
			return;
		}
		
		// Make it the top item in the list, removing it from it's previous location, if there was one
		if (index > 0) {
			history.remove(index);
		} 			
		history.add(0, item);
		
		if (history.size() > MAX_HISTORY_SIZE) {
			history.remove(history.size() - 1);
		}	
	}
	
	/**
	 * Returns whether the given config is displayed in the favorites
	 * menu
	 * 
	 * @param config launch configuration
	 * @return whether the given config is displayed in the favorites
	 *  menu
	 */
	public boolean isDebugFavorite(ILaunchConfiguration config) {
		return (findConfigInHistoryList(fDebugFavorites, config)) >= 0;
	}	
	
	/**
	 * Returns whether the given config is displayed in the favorites
	 * menu
	 * 
	 * @param config launch configuration
	 * @return whether the given config is displayed in the favorites
	 *  menu
	 */
	public boolean isRunFavorite(ILaunchConfiguration config) {
		return(findConfigInHistoryList(fRunFavorites, config)) >= 0;
	}	
	
	/**
	 * Adds the given config to the debug favorites. Has no
	 * effect if already a debug favorite.
	 * 
	 * @param config launch configuration
	 */
	public void addDebugFavorite(ILaunchConfiguration config) {
		if (!isDebugFavorite(config)) {
			LaunchConfigurationHistoryElement hist = new LaunchConfigurationHistoryElement(config, ILaunchManager.DEBUG_MODE, getModelPresentation().getText(config));
			fDebugFavorites.add(hist);
		}
	}	
	
	/**
	 * Adds the given config to the run favorites. Has no
	 * effect if already a run favorite.
	 * 
	 * @param config launch configuration
	 */
	public void addRunFavorite(ILaunchConfiguration config) {
		if (!isRunFavorite(config)) {
			LaunchConfigurationHistoryElement hist = new LaunchConfigurationHistoryElement(config, ILaunchManager.RUN_MODE, getModelPresentation().getText(config));
			fRunFavorites.add(hist);
		}
	}	
	
	/**
	 * Removes the given config from the debug favorites. Has no
	 * effect if not a favorite.
	 * 
	 * @param config launch configuration
	 */
	public void removeDebugFavorite(ILaunchConfiguration config) {
		int index = findConfigInHistoryList(fDebugFavorites, config);
		if (index >= 0) {
			fDebugFavorites.remove(index);
		}
	}	
	
	/**
	 * Adds the given config to the run favorites. Has no
	 * effect if already a run favorite.
	 * 
	 * @param config launch configuration
	 */
	public void removeRunFavorite(ILaunchConfiguration config) {
		int index = findConfigInHistoryList(fRunFavorites, config);
		if (index >= 0) {
			fRunFavorites.remove(index);
		}
	}	
	
	/**
	 * Find the specified history element in the specified list and return the index at which
	 * it was found.  Return -1 if the element wasn't found in the list.
	 */
	protected int findConfigInHistoryList(Vector list, ILaunchConfiguration config) {
		for (int i = 0; i < list.size(); i++) {
			LaunchConfigurationHistoryElement historyElement = (LaunchConfigurationHistoryElement) list.get(i);
			if (historyElement.getLaunchConfiguration().contentsEqual(config)) {
				return i;
			}
		}
		
		// Element wasn't in list
		return -1;
	}
	
	/**
	 * Find the specified history element in the history list for the mode that is not the one
	 * specified.  For example, if mode is 'debug', the 'run' list is searched.
	 */
	protected int findConfigInOtherHistoryList(String mode, ILaunchConfiguration config) {
		Vector historyList = getOtherHistoryList(mode);
		return findConfigInHistoryList(historyList, config);
	}
	
	/**
	 * Return the 'other' history list from the mode specified.  For example, if
	 * mode is 'debug', return the 'run' history list.
	 */
	protected Vector getOtherHistoryList(String mode) {
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			return fRunHistory;
		} else {
			return fDebugHistory;
		}
	}
	
	/**
	 * Return the 'other' mode from the one specified.
	 */
	protected String getOtherMode(String mode) {
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			return ILaunchManager.RUN_MODE;
		} else {
			return ILaunchManager.DEBUG_MODE;
		}
	}
	
	/**
	 * Removes the given element from launch histories.
	 * 
	 * @param history the history element to remove
	 */
	public void removeHistoryElement(LaunchConfigurationHistoryElement history) {
		fDebugHistory.remove(history);
		fRunHistory.remove(history);
		if (history.equals(fRecentLaunch)) {
			fRecentLaunch = null;
		}
	}
	
	protected String getHistoryAsXML() throws IOException, CoreException {
		org.w3c.dom.Document doc = new DocumentImpl();
		Element historyRootElement = doc.createElement(HISTORY_ROOT_NODE); 
		doc.appendChild(historyRootElement);
		
		List all = new ArrayList(fDebugHistory.size() + fDebugFavorites.size() + fRunHistory.size() + fRunFavorites.size());
		all.addAll(fDebugFavorites);
		all.addAll(fRunFavorites);
		all.addAll(fDebugHistory);
		all.addAll(fRunHistory);
		

		Iterator iter = all.iterator();
		while (iter.hasNext()) {
			Element historyElement = getHistoryEntryAsXMLElement(doc, (LaunchConfigurationHistoryElement)iter.next());
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
	
	protected Element getHistoryEntryAsXMLElement(org.w3c.dom.Document doc, LaunchConfigurationHistoryElement element) throws CoreException {
		Element entry = doc.createElement(HISTORY_LAUNCH_NODE); 
		setAttributes(entry, element);
		return entry;
	}
	
	protected Element getRecentLaunchAsXMLElement(org.w3c.dom.Document doc, LaunchConfigurationHistoryElement element) throws CoreException {
		Element entry = doc.createElement(HISTORY_LAST_LAUNCH_NODE); 
		setAttributes(entry, element);
		return entry;
	}
	
	protected void setAttributes(Element entry, LaunchConfigurationHistoryElement element) throws CoreException {
		if (usingConfigurationStyleLaunching()) {
			setNewAttributes(entry, element);
		} else {
			setOldAttributes(entry, element);
		}
	}
	
	protected void setNewAttributes(Element entry, LaunchConfigurationHistoryElement element) throws CoreException {
		ILaunchConfiguration config = element.getLaunchConfiguration();
		if (config instanceof ILaunchConfigurationWorkingCopy) {
			config = ((ILaunchConfigurationWorkingCopy)config).getOriginal();
		}
		String memento = config.getMemento();
		entry.setAttribute(HISTORY_MEMENTO_ATT, memento); 
		entry.setAttribute(HISTORY_MODE_ATT, element.getMode());		
		entry.setAttribute(HISTORY_LABEL_ATT, element.getLabel());		 
	}
	
	protected void setOldAttributes(Element entry, LaunchConfigurationHistoryElement element) {
		entry.setAttribute("launcherId", element.getLauncherIdentifier()); //$NON-NLS-1$
		entry.setAttribute("elementMemento", element.getElementMemento()); //$NON-NLS-1$
		entry.setAttribute(HISTORY_LABEL_ATT, element.getLabel()); 
		entry.setAttribute(HISTORY_MODE_ATT, element.getMode());
	}
		
	protected IPath getHistoryFilePath() {
		IPath historyPath = getStateLocation();
		if (usingConfigurationStyleLaunching()) {
			historyPath = historyPath.append(LAUNCH_CONFIGURATION_HISTORY_FILENAME); 
		} else {
			historyPath = historyPath.append(LAUNCH_OLD_HISTORY_FILENAME); 			
		}
		return historyPath;		
	}

	/**
	 * Write out an XML file indicating the entries on the run & debug history lists and
	 * the most recent launch.
	 */
	protected void persistLaunchHistory() throws IOException, CoreException {
		IPath historyPath = getHistoryFilePath();
		String osHistoryPath = historyPath.toOSString();
		File file = new File(osHistoryPath);
		file.createNewFile();
		FileWriter writer = new FileWriter(file);
		writer.write(getHistoryAsXML());
		writer.close();		
	}
	
	/**
	 * Find the XML history file and parse it.  Place the corresponding history elements
	 * in the appropriate history lists, and set the most recent launch.
	 */
	protected void restoreLaunchHistory() throws IOException {
		
		// Find the history file
		IPath historyPath = getHistoryFilePath();
		String osHistoryPath = historyPath.toOSString();
		File file = new File(osHistoryPath);
		
		// If no history file, nothing to do
		if (!file.exists()) {
			return;
		}
		
		// Parse the history file
		FileInputStream stream = new FileInputStream(file);
		Element rootHistoryElement = null;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			rootHistoryElement = parser.parse(new InputSource(stream)).getDocumentElement();
		} catch (SAXException e) {
			log(e);
			return;
		} catch (ParserConfigurationException e) {
			log(e);
			return;
		} finally {
			stream.close();
		}
		
		// If root node isn't what we expect, return
		if (!rootHistoryElement.getNodeName().equalsIgnoreCase(HISTORY_ROOT_NODE)) { 
			return;
		}

		// For each child of the root node, construct a history element wrapper and add it to
		// the appropriate history list, or set the most recent launch
		NodeList list = rootHistoryElement.getChildNodes();
		int length = list.getLength();
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			short type = node.getNodeType();
			if (type == Node.ELEMENT_NODE) {
				Element entry = (Element) node;
				if (entry.getNodeName().equalsIgnoreCase(HISTORY_LAUNCH_NODE)) { 
					LaunchConfigurationHistoryElement item = createHistoryElement(entry);
					if (item != null) {
						if (item.isFavorite()) {
							if (item.getMode().equals(ILaunchManager.DEBUG_MODE)) {
								fDebugFavorites.add(item);
							} else {
								fRunFavorites.add(item);
							}							
						} else {
							if (item.getMode().equals(ILaunchManager.DEBUG_MODE)) {
								fDebugHistory.add(item);
							} else {
								fRunHistory.add(item);
							}
						}
					}
				} else if (entry.getNodeName().equalsIgnoreCase(HISTORY_LAST_LAUNCH_NODE)) { 
					fRecentLaunch = createHistoryElement(entry);
				}
			}
		}
	}
	
	/**
	 * Construct & return a <code>LaunchConfigurationHistoryElement</code> corresponding to
	 * the specified XML element.
	 */
	protected LaunchConfigurationHistoryElement createHistoryElement(Element entry) {
		if (usingConfigurationStyleLaunching()) {
			return createNewHistoryElement(entry);
		} else {
			return createOldHistoryElement(entry);
		}
	}
	
	protected LaunchConfigurationHistoryElement createNewHistoryElement(Element entry) {
		String memento = entry.getAttribute(HISTORY_MEMENTO_ATT); 
		String mode = entry.getAttribute(HISTORY_MODE_ATT);       
		String label = entry.getAttribute(HISTORY_LABEL_ATT);
		LaunchConfigurationHistoryElement hist = null;
		try {
			ILaunchConfiguration launchConfig = getLaunchManager().getLaunchConfiguration(memento);
			if (launchConfig.exists()) {
				hist = new LaunchConfigurationHistoryElement(launchConfig, mode, label);
			}
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}	
		return hist;
	}
	
	protected LaunchConfigurationHistoryElement createOldHistoryElement(Element entry) {
		String launcherId = entry.getAttribute("launcherId"); //$NON-NLS-1$
		String memento = entry.getAttribute("elementMemento"); //$NON-NLS-1$
		String mode = entry.getAttribute(HISTORY_MODE_ATT); 
		String label = entry.getAttribute(HISTORY_LABEL_ATT); 
		return new LaunchConfigurationHistoryElement(launcherId, memento, mode, label);		
	}
	
	/**
	 * Add the given launch to the debug history if the
	 * launcher supports the debug mode.  
	 */
	protected void updateOldHistory(String mode, Vector history, ILaunch launch, String memento) {
		
		// First make sure the launcher used supports the mode of the history list
		ILauncher launcher= launch.getLauncher();
		Set supportedLauncherModes= launcher.getModes();
		if (!supportedLauncherModes.contains(mode)) {
			return;
		}
		
		// create new history item
		LaunchConfigurationHistoryElement item= new LaunchConfigurationHistoryElement(launcher.getIdentifier(), memento, mode, getModelPresentation().getText(launch));
		
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
		
	/**
	 * Returns whether the given launcher should be visible in the UI.
	 * If a launcher is not visible, it will not appear
	 * in the UI - i.e. not as a default launcher, not in the run/debug
	 * drop downs, and not in the launch history.
	 * Based on the public attribute.
	 */
	public boolean isVisible(ILauncher launcher) {
		if (launcher == null) {
			return false;
		}
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
		log(s);
		// if the 'message' resource string and the IStatus' message are the same,
		// don't show both in the dialog
		if (s != null && message.equals(s.getMessage())) {
			message= null;
		}
		ErrorDialog.openError(shell, title, message, s);
	}
	
	/**
	 * Utility method with conventions
	 */
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		log(t);
		IStatus status;
		if (t instanceof CoreException) {
			status= ((CoreException)t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message= null;
			}
		} else {
			status= new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "Error within Debug UI: ", t); //$NON-NLS-1$	
		}
		ErrorDialog.openError(shell, title, message, status);
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 */
	public static void log(Throwable t) {
		IStatus status= new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "Error logged from Debug UI: ", t); //$NON-NLS-1$
		log(status);
	}
	
	/**
	 * Logs the given message if in debug mode.
	 * 
	 * @param String message to log
	 */
	public static void logDebugMessage(String message) {
		if (getDefault().isDebugging()) {
			logErrorMessage(message);
		}
	}
	
	/**
	 * Logs an internal error with the specified message.
	 * 
	 * @param message the error message to log
	 */
	public static void logErrorMessage(String message) {
		// this message is intentionally not internationalized, as an exception may
		// be due to the resource bundle itself
		log(new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "Internal message logged from Debug UI: " + message, null)); //$NON-NLS-1$	
	}
	
	/**
	 * Save all dirty editors of all the workbench pages.
	 * Returns whether the operation succeeded.
	 * 
	 * @return whether all saving was completed
	 */
	protected static boolean saveAllPages() {
		IWorkbench wb = getActiveWorkbenchWindow().getWorkbench();
		IWorkbenchWindow[] windows = wb.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int j = 0; j < pages.length; j++) {
				if (!pages[j].saveAllEditors(true)) {
					return false;
				};
			}
		}
		return true;
	}	
	
	/**
	 * If the "build before launch" preference is on, save
	 * and build. This prompts the user to save any editors
	 * with unsaved changes. Returns whether the operation
	 * succeeded.
	 * 
	 * @return whether saving and building was completed
	 */
	public static boolean saveAndBuild() {
		if (!getDefault().getPreferenceStore().getBoolean(IDebugUIConstants.PREF_AUTO_BUILD_BEFORE_LAUNCH)) {
			return true;
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.isAutoBuilding()) {
			// if auto-building, saving will trigger a build for us
			return saveAllPages();
		}
		
		// prompt for save and then do build if required
		if (saveAllPages()) {
			return doBuild();
		}
		return false;	
	}
	
	private static boolean doBuild() {
		ProgressMonitorDialog dialog= new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InterruptedException e) {
			// cancelled by user
			return false;
		} catch (InvocationTargetException e) {
			String title= DebugUIMessages.getString("DebugUIPlugin.Run/Debug_1"); //$NON-NLS-1$
			String message= DebugUIMessages.getString("DebugUIPlugin.Build_error._Check_log_for_details._2"); //$NON-NLS-1$
			Throwable t = e.getTargetException();
			errorDialog(getShell(), title, message, t);
			return false;
		}
		return true;
	}	
	
	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display;
		display= Display.getCurrent();
		if (display == null)
			display= Display.getDefault();
		return display;		
	}	

	/**
	 * If the 'launching style' preference changes, wipe out both launch histories.
	 * 
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName= event.getProperty();
		if (propertyName.equals(IDebugPreferenceConstants.LAUNCHING_STYLE)) {
			setEmptyLaunchHistories();
		}
	}	
}

