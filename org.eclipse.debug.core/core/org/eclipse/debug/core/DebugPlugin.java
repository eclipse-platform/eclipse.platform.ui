package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.HashMap;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.core.BreakpointManager;
import org.eclipse.debug.internal.core.ExpressionManager;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.core.Launcher;
import org.eclipse.debug.internal.core.ListenerList;
import org.eclipse.debug.internal.core.RuntimeProcess;

/**
 * There is one instance of the debug plug-in available from
 * <code>DebugPlugin.getDefault()</code>. The debug plug-in provides:
 * <ul>
 * <li>access to the breakpoint manager</li>
 * <li>access to the launch manager</li>
 * <li>access to the expression manager</li>
 * <li>access to the registered launcher extensions</li>
 * <li>debug event notification</li>
 * </ul>
 * <p>
 * Clients may not instantiate or subclass this class.
 * </p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 */
public class DebugPlugin extends Plugin {
	
	/**
	 * Debug plug-in identifier
	 * (value <code>"org.eclipse.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.debug.core"; //$NON-NLS-1$
	
	/**
	 * Launcher extension point identifier
	 * (value <code>"launchers"</code>).
	 */
	public static final String EXTENSION_POINT_LAUNCHER= "launchers"; //$NON-NLS-1$
	
	/**
	 * Launch configuration types extension point identifier
	 * (value <code>"launchConfigurationTypes"</code>).
	 */
	public static final String EXTENSION_POINT_LAUNCH_CONFIGURATION_TYPES= "launchConfigurationTypes"; //$NON-NLS-1$	
	
	/**
	 * Breakpoint extension point identifier
	 * (value <code>"breakpoints"</code>).
	 */
	public static final String EXTENSION_POINT_BREAKPOINTS= "breakpoints";	 //$NON-NLS-1$
	
	/**
	 * Status handler extension point identifier
	 * (value <code>"statusHandlers"</code>).
	 */
	public static final String EXTENSION_POINT_STATUS_HANDLERS= "statusHandlers";	 //$NON-NLS-1$	

	/**
	 * Source locator extension point identifier
	 * (value <code>"sourceLocators"</code>).
	 */
	public static final String EXTENSION_POINT_SOURCE_LOCATORS= "sourceLocators";	 //$NON-NLS-1$	
		
	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 120;		

	/**
	 * The singleton debug plug-in instance.
	 */
	private static DebugPlugin fgDebugPlugin= null;

	/**
	 * The collection of launcher extensions.
	 */
	private Launcher[] fLaunchers= new Launcher[0];

	/**
	 * The singleton breakpoint manager.
	 */
	private BreakpointManager fBreakpointManager;
	
	/**
	 * The singleton expression manager.
	 */
	private ExpressionManager fExpressionManager;	

	/**
	 * The singleton launch manager.
	 */
	private LaunchManager fLaunchManager;

	/**
	 * The collection of debug event listeners.
	 */
	private ListenerList fEventListeners= new ListenerList(20);
	
	/**
	 * Event filters, or <code>null</code> if none.
	 */
	private ListenerList fEventFilters = null;

	/**
	 * Whether this plugin is in the process of shutting
	 * down.
	 */
	private boolean fShuttingDown= false;
	
	/**
	 * This plug-in's save participant
	 */
	private ISaveParticipant fSaveParticipant = null;	
	
	/**
	 * Table of status handlers. Keys are {plug-in identifier, status code}
	 * pairs, and values are associated <code>IConfigurationElement</code>s.
	 */
	private HashMap fStatusHandlers = null;
	
	/**
	 * Returns the singleton instance of the debug plug-in.
	 */
	public static DebugPlugin getDefault() {
		return fgDebugPlugin;
	}
	
	/**
	 * Sets the singleton instance of the debug plug-in.
	 * 
	 * @param plugin the debug plug-in, or <code>null</code>
	 *  when shutting down
	 */
	private static void setDefault(DebugPlugin plugin) {
		fgDebugPlugin = plugin;
	}

	/**
	 * Constructs the debug plug-in.
	 * <p>
	 * An instance of this plug-in runtime class is automatically created 
	 * when the facilities provided by this plug-in are required.
	 * <b>Clients must never explicitly instantiate a plug-in runtime class.</b>
	 * </p>
	 * 
	 * @param pluginDescriptor the plug-in descriptor for the
	 *   debug plug-in
	 */
	public DebugPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		setDefault(this);
	}

	/**
	 * Adds the given listener to the collection of registered debug
	 * event listeners. Has no effect if an identical listener is already
	 * registered.
	 *
	 * @param listener the listener to add
	 */
	public void addDebugEventListener(IDebugEventListener listener) {
		fEventListeners.add(listener);
	}

	/**
	 * Notifies all registered debug event listeners of the given event.
	 * Has no effect if the event is filtered by a registered
	 * debug event filter.
	 *
	 * @param event the debug event to fire
	 * @see IDebugEventFilter
	 */
	public void fireDebugEvent(DebugEvent event) {
		if (isShuttingDown() || event == null || isFiltered(event))
			return;
		
		Object[] listeners= getEventListeners();
		for (int i= 0; i < listeners.length; i++) {
			((IDebugEventListener)listeners[i]).handleDebugEvent(event);
		}
	}
	
	/**
	 * Returns the breakpoint manager.
	 *
	 * @return the breakpoint manager
	 * @see IBreakpointManager
	 */
	public IBreakpointManager getBreakpointManager() {
		return fBreakpointManager;
	}

	/**
	 * Returns a collection of launchers. Launchers represent
	 * and provide access to launcher extensions, delaying
	 * instantiation of the underlying delegate until
	 * required.
	 *
	 * @return an array of launchers
	 * @see org.eclipse.debug.core.model.ILauncherDelegate
	 * @deprecated to be removed
	 */
	public ILauncher[] getLaunchers() {
		return fLaunchers;
	}
	
	/**
	 * Returns the launch manager.
	 *
	 * @return the launch manager
	 * @see ILaunchManager
	 */
	public ILaunchManager getLaunchManager() {
		return fLaunchManager;
	}
	
	/**
	 * Returns the status handler registered for the given
	 * status, or <code>null</code> if none.
	 *
	 * @return the status handler registered for the given
	 *  status, or <code>null</code> if none
	 */
	public IStatusHandler getStatusHandler(IStatus status) {
		StatusHandlerKey key = new StatusHandlerKey(status.getPlugin(), status.getCode());
		IConfigurationElement config = (IConfigurationElement)fStatusHandlers.get(key);
		if (config != null) {
			try {
				return (IStatusHandler)config.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				log(e.getStatus());
			}
		}
		return null;
	}	
	
	/**
	 * Returns the expression manager.
	 *
	 * @return the expression manager
	 * @see IExpressionManager
	 * @since 2.0
	 */
	public IExpressionManager getExpressionManager() {
		return fExpressionManager;
	}	

	/**
	 * Creates proxy launchers for all launcher delegates
	 * defined in launcher extensions.
	 *
	 * @exception CoreException if creation of a launcher extension fails
	 */
	private void createLaunchers() throws CoreException {
		IPluginDescriptor descriptor= getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(EXTENSION_POINT_LAUNCHER);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		fLaunchers= new Launcher[infos.length];
		for (int i= 0; i < infos.length; i++) {
			fLaunchers[i]= new Launcher(infos[i]);
		}
	}	

	/**
	 * Removes the given listener from the collection of registered debug
	 * event listeners. Has no effect if an identical listener is not already
	 * registered.
	 *
	 * @param listener the listener to remove
	 */
	public void removeDebugEventListener(IDebugEventListener listener) {
		fEventListeners.remove(listener);
	}

	/**
	 * Shuts down this debug plug-in and discards all plug-in state.
	 * <p> 
	 * This method will be automatically invoked by the platform when
	 * the platform is shut down.
	 * </p>
	 * <b>Clients must never explicitly call this method.</b>
	 *
	 * @exception CoreException if this plug-in fails to shut down
	 */
	public void shutdown() throws CoreException {
		setShuttingDown(true);
		super.shutdown();
		fLaunchManager.shutdown();
		fBreakpointManager.shutdown();
		fEventListeners.removeAll();
		setDefault(null);
		ResourcesPlugin.getWorkspace().removeSaveParticipant(this);
	}

	/**
	 * Starts up the debug plug-in. This involves creating the launch and
	 * breakpoint managers, creating proxies to all launcher extensions,
	 * and restoring all persisted breakpoints.
	 * <p>
 	 * This method is automatically invoked by the platform 
	 * the first time any code in this plug-in is executed.
	 * </p>
	 * <b>Clients must never explicitly call this method.</b>
	 *
	 * @see Plugin#startup
	 * @exception CoreException if this plug-in fails to start up
	 */
	public void startup() throws CoreException {
		fLaunchManager= new LaunchManager();
		fBreakpointManager= new BreakpointManager();
		fExpressionManager = new ExpressionManager();
		createLaunchers();	
		fBreakpointManager.startup();
		fLaunchManager.startup();
		//recoverState();
		initializeStatusHandlers();
	}
	
	/**
	 * Processes resource deltas that may have been missed
	 * while this plug-in was not active.
	 * 
	 * @exception CoreException if unable to restore state
	 */
	private void recoverState() throws CoreException {
		// restore launch manager's launch configuration indicies
		fSaveParticipant = new DebugPluginSaveParticipant();
		ISavedState savedState = ResourcesPlugin.getWorkspace().addSaveParticipant(this, fSaveParticipant);
		boolean restored = false;
		if (savedState != null) {
			DebugSavedStateResourceChangedListener listener = new DebugSavedStateResourceChangedListener();
			savedState.processResourceChangeEvents(listener);
			if (listener.receivedNotification()) {
				restored = true;
			}
		}
		if (!restored) {
			fLaunchManager.rebuildLaunchConfigIndex();
		}		
	}
	
	/**
	 * Creates and returns a new process representing the given
	 * <code>java.lang.Process</code>. A streams proxy is created
	 * for the I/O streams in the system process.
	 *
	 * @param process the system process to wrap
	 * @param label the label assigned to the process
	 * @return the process
	 * @see IProcess
	 */
	public static IProcess newProcess(Process process, String label) {
		return new RuntimeProcess(process, label);
	}
	
	/**
	 * Returns whether this plug-in is in the process of 
	 * being shutdown.
	 * 
	 * @return whether this plug-in is in the process of 
	 *  being shutdown
	 */
	private boolean isShuttingDown() {
		return fShuttingDown;
	}
	
	/**
	 * Sets whether this plug-in is in the process of 
	 * being shutdown.
	 * 
	 * @param value whether this plug-in is in the process of 
	 *  being shutdown
	 */
	private void setShuttingDown(boolean value) {
		fShuttingDown = value;
	}
	
	/**
	 * Returns the collection of debug event listeners registered
	 * with this plug-in.
	 * 
	 * @return list of registered debug event listeners, instances
	 *  of <code>IDebugEventListener</code>	
	 */
	private Object[] getEventListeners() {
		return fEventListeners.getListeners();
	}
	
	/**
	 * Adds the given debug event filter to the registered
	 * event filters. Has no effect if an identical filter
	 * is already registerd.
	 * 
	 * @param filter debug event filter
	 * @since 2.0
	 */
	public void addDebugEventFilter(IDebugEventFilter filter) {
		if (fEventFilters == null) {
			fEventFilters = new ListenerList(2);
		}
		fEventFilters.add(filter);
	}
	
	/**
	 * Removed the given debug event filter from the registered
	 * event filters. Has no effect if an identical filter
	 * is not already registerd.
	 * 
	 * @param filter debug event filter
	 * @since 2.0
	 */
	public void removeDebugEventFilter(IDebugEventFilter filter) {
		if (fEventFilters != null) {
			fEventFilters.remove(filter);
			if (fEventFilters.size() == 0) {
				fEventFilters = null;
			}
		}
	}	
	
	/**
	 * Returns whether the given event is filtered.
	 * 
	 * @param event debug event
	 * @return whether the given event is filtered
	 */
	private boolean isFiltered(DebugEvent event) {
		if (fEventFilters != null) {
			Object[] filters = fEventFilters.getListeners();
			for (int i = 0; i < filters.length; i++) {
				if (((IDebugEventFilter)filters[i]).filterDebugEvent(event)) {
					return true;
				}
			}
		}
		return false;
	}	
	
	/**
	 * Convenience method to log internal errors
	 */
	public static void logError(Exception e) {
		if (getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			log(new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), INTERNAL_ERROR, "Internal error logged from Debug Core: ", e));  //$NON-NLS-1$		
		}
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
	 * A save participant that requests a resource delta
	 * on the next startup.
	 */
	class DebugPluginSaveParticipant implements ISaveParticipant {
		/**
		 * @see ISaveParticipant#doneSaving(ISaveContext)
		 */
		public void doneSaving(ISaveContext context) {
		}

		/**
		 * @see ISaveParticipant#prepareToSave(ISaveContext)
		 */
		public void prepareToSave(ISaveContext context) throws CoreException {
		}

		/**
		 * @see ISaveParticipant#rollback(ISaveContext)
		 */
		public void rollback(ISaveContext context) {
		}

		/**
		 * @see ISaveParticipant#saving(ISaveContext)
		 */
		public void saving(ISaveContext context) throws CoreException {
			context.needDelta();
		}

	}
	
	/**
	 * Dispatches any resource delta since this plug-in was
	 * last shutdown and started to interested handlers:
	 * <ul>
	 * <li>The launch manager</li>
	 * </ul>
	 */ 
	class DebugSavedStateResourceChangedListener implements IResourceChangeListener {
		
		/**
		 * Whether a resource change notification was
		 * received by this listener.
		 */
		private boolean fReceivedNotification = false;
		
		/**
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			fReceivedNotification = true;
			fLaunchManager.resourceChanged(event);
		}
		
		/**
		 * Returns whether a change notification was received
		 * by this listener
		 * 
		 * @return whether a change notification was received
		 *  by this listener
		 */
		protected boolean receivedNotification() {
			return fReceivedNotification;
		}

	}
	
	/**
	 * Register status handlers.
	 * 
	 * @exception CoreException if an exception occurs reading
	 *  the extensions
	 */
	private void initializeStatusHandlers() throws CoreException {
		fStatusHandlers = new HashMap(10);
		IPluginDescriptor descriptor= DebugPlugin.getDefault().getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(EXTENSION_POINT_STATUS_HANDLERS);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		for (int i= 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			String id = configurationElement.getAttribute("plugin"); //$NON-NLS-1$
			String code = configurationElement.getAttribute("code"); //$NON-NLS-1$
			
			if (id != null && code != null) {
				try {
					StatusHandlerKey key = new StatusHandlerKey(id, Integer.parseInt(code));
					fStatusHandlers.put(key, configurationElement);
				} catch (NumberFormatException e) {
					// invalid status handler
					invalidStatusHandler(e, configurationElement.getAttribute("id")); //$NON-NLS-1$
				}
			} else {
				// invalid status handler
				invalidStatusHandler(null, configurationElement.getAttribute("id")); //$NON-NLS-1$
			}
		}			
	}
	
	private void invalidStatusHandler(Exception e, String id) {
		log(new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), INTERNAL_ERROR, MessageFormat.format("Invalid status handler extension: {0}", new String[] {id}), e));
	}
	
	/**
	 * Key for status handler extensions - a plug-in identifier/code pair
	 */
	class StatusHandlerKey {
		
		String fPluginId;
		int fCode;
		
		StatusHandlerKey(String pluginId, int code) {
			fPluginId = pluginId;
			fCode = code;
		}
		
		public int hashCode() {
			return fPluginId.hashCode() + fCode;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof StatusHandlerKey) {
				StatusHandlerKey s = (StatusHandlerKey)obj;
				return fCode == s.fCode && fPluginId.equals(s.fPluginId);
			}
			return false;
		}
	}
	
	
}


