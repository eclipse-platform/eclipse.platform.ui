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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILauncher;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.launchConfigurations.PerspectiveManager;
import org.eclipse.debug.internal.ui.views.ConsoleDocument;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIEventFilter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.util.ListenerList;
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
public class DebugUIPlugin extends AbstractUIPlugin implements IDocumentListener, 
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
	}
	/**
	 * Poll the filters to determine if the event should be shown
	 */
	public boolean showSuspendEvent(DebugEvent event) {
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
	public boolean showLaunch(ILaunch launch) {
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
	 * Debug ui thread safe access to a display
	 */
	public Display getDisplay() {
		//we can rely on not creating a display as we 
		//prereq the base eclipse ui plugin.
		return Display.getDefault();
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
		try {
			persistLaunchHistory();
		} catch (IOException e) {
			logError(e);
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
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		//set up the docs for launches already registered
		ILaunch[] launches= launchManager.getLaunches();
		try {
			restoreLaunchHistory();
		} catch (IOException e) {
			logError(e);
		}
		for (int i = 0; i < launches.length; i++) {
			launchAdded(launches[i]);
		}
		
		// startup the perspective manager
		PerspectiveManager.getDefault().startup();
		
		IAdapterManager manager= Platform.getAdapterManager();
		// Create & register the adapter factory that will dispense objects that 
		// know about the properties that different breakpoint types support
		manager.registerAdapters(new BreakpointPropertiesAdapterFactory(), IBreakpoint.class);
		
		manager.registerAdapters(new DebugUIPropertiesAdapterFactory(), IDebugElement.class);
		manager.registerAdapters(new DebugUIPropertiesAdapterFactory(), IProcess.class);
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

		if ((processInput == null) || (processInput.getLaunch() == null)) {
			return null;
		} else {
			return processInput;
		}
	}

	/**
	 * @see IDocumentListener#documentAboutToBeChanged(DocumentEvent)
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
							logError(pie);
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
		if (fCurrentProcess != null) {
			getConsoleDocument(fCurrentProcess).removeDocumentListener(this);
		}
		fCurrentProcess= process;
		if (fCurrentProcess != null) {
			getConsoleDocument(fCurrentProcess).addDocumentListener(this);
		}
	}
	
	/**
	 * @see IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
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
	 * @see ILaunchListener#launchRemoved(ILaunch)
	 */
	public void launchRemoved(final ILaunch launch) {
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
	 * @see ILaunchListener#launchChanged(ILaunch)
	 */
	public void launchChanged(ILaunch launch) {	
	}

	/**
	 * Must not assume that will only be called from the UI thread.
	 *
	 * @see ILaunchListener#launchAdded(ILaunch)
	 */
	public void launchAdded(final ILaunch launch) {
		if (launch.getLaunchConfiguration() != null) {
			// new launch configuration processing
			//return;
		} else {		
			// old launcher processing
			updateHistories(launch);
			//switchToDebugPerspectiveIfPreferred(launch);
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
		Runnable r = new Runnable() {
			public void run() {
				removeDeletedHistories(fDebugHistory);
				removeDeletedHistories(fRunHistory);
			}
		};
		getDisplay().asyncExec(r);
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
	 * Removes the given element from launch histories.
	 * 
	 * @param history the history element to remove
	 */
	public void removeHistoryElement(LaunchHistoryElement history) {
		fDebugHistory.remove(history);
		fRunHistory.remove(history);
		if (history.equals(fRecentLaunch)) {
			fRecentLaunch = null;
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
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Convenience method to log internal UI errors
	 */
	public static void logError(Exception e) {
		if (getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			log(new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, "Internal error logged from Debug UI: ", e));  //$NON-NLS-1$		
		}
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
			IStatus status = null;
			if (t instanceof CoreException) {
				status = ((CoreException)t).getStatus();
			}
			errorDialog(getShell(), title, message, status);
			return false;
		}
		return true;
	}		
}

