package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.text.MessageFormat;
import java.util.HashMap;

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
import org.eclipse.debug.internal.core.DebugCoreMessages;
import org.eclipse.debug.internal.core.ExpressionManager;
import org.eclipse.debug.internal.core.LaunchManager;
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
 * <li>status handlers</li>
 * </ul>
 * <p>
 * Clients may not instantiate or subclass this class.
 * </p>
 */
public class DebugPlugin extends Plugin {
	
	/**
	 * Debug plug-in identifier
	 * (value <code>"org.eclipse.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.debug.core"; //$NON-NLS-1$
		
	/**
	 * Simple identifier constant (value <code>"launchConfigurationTypes"</code>)
	 * for the launch configuration types extension point.
	 * 
	 * @since 2.0
	 */
	public static final String EXTENSION_POINT_LAUNCH_CONFIGURATION_TYPES= "launchConfigurationTypes"; //$NON-NLS-1$	
	
	/**
	 * Simple identifier constant (value <code>"breakpoints"</code>) for the
	 * breakpoints extension point.
	 * 
	 * @since 2.0
	 */
	public static final String EXTENSION_POINT_BREAKPOINTS= "breakpoints";	 //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"statusHandlers"</code>) for the
	 * status handlers extension point.
	 * 
	 * @since 2.0
	 */
	public static final String EXTENSION_POINT_STATUS_HANDLERS= "statusHandlers";	 //$NON-NLS-1$	

	/**
	 * Simple identifier constant (value <code>"sourceLocators"</code>) for the
	 * source locators extension point.
	 * 
	 * @since 2.0
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
	 * @since 2.0
	 */
	public void addDebugEventListener(IDebugEventSetListener listener) {
		fEventListeners.add(listener);
	}	
	
	/**
	 * Notifies all registered debug event set listeners of the given
	 * debug events. Events which are filtered by a registered debug event
	 * filter are not fired.
	 * 
	 * @param events array of debug events to fire
	 * @see IDebugEventFilter
	 * @see IDebugEventSetListener
	 * @since 2.0
	 */
	public void fireDebugEventSet(DebugEvent[] events) {
		if (isShuttingDown() || events == null)
			return;
		events = filterEvents(events);
		if (events == null) {
			return;
		} else {
			Object[] listeners= getEventListeners();
			for (int i= 0; i < listeners.length; i++) {
				((IDebugEventSetListener)listeners[i]).handleDebugEvents(events);
			}		
		}
	}
	
	/**
	 * Returns a collection of events, based on the given event
	 * set, removing any events that are filtered by registered
	 * debug event filters. Returns <code>null</code> or an empty 
	 * collection if all events are filtered.
	 * 
	 * @param events the event set to filter
	 * @return filtered event set
	 */
	private DebugEvent[] filterEvents(DebugEvent[] events) {
		if (hasEventFilters()) {
			Object[] filters = fEventFilters.getListeners();
			for (int i = 0; i < filters.length; i++) {
				events = ((IDebugEventFilter)filters[i]).filterDebugEvents(events);
				if (events == null || events.length == 0) {
					break;
				}
			}

		}
		return events;
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
	 * @since 2.0
	 */
	public IStatusHandler getStatusHandler(IStatus status) {
		StatusHandlerKey key = new StatusHandlerKey(status.getPlugin(), status.getCode());
		IConfigurationElement config = (IConfigurationElement)fStatusHandlers.get(key);
		if (config != null) {
			try {
				return (IStatusHandler)config.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				log(e);
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
	 * Removes the given listener from the collection of registered debug
	 * event listeners. Has no effect if an identical listener is not already
	 * registered.
	 *
	 * @param listener the listener to remove
	 * @since 2.0
	 */
	public void removeDebugEventListener(IDebugEventSetListener listener) {
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
		fBreakpointManager.startup();
		fLaunchManager.startup();
		initializeStatusHandlers();
	}
	
	/**
	 * Creates and returns a new process representing the given
	 * <code>java.lang.Process</code>. A streams proxy is created
	 * for the I/O streams in the system process. The process
	 * is added to the given launch.
	 *
	 * @param launch the launch the process is conatined in
	 * @param process the system process to wrap
	 * @param label the label assigned to the process
	 * @return the process
	 * @see IProcess
	 */
	public static IProcess newProcess(ILaunch launch, Process process, String label) {
		return new RuntimeProcess(launch, process, label);
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
	 *  of <code>IDebugEventSetListeners</code>
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
	 * Removes the given debug event filter from the registered
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
	 * Logs the given message if in debug mode.
	 * 
	 * @param message the message to log
	 * @since 2.0
	 */
	public static void logDebugMessage(String message) {
		if (getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			log(new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), INTERNAL_ERROR, "Internal message logged from Debug Core: " + message, null)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status status to log
	 * @since 2.0
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t throwable to log 
	 * @since 2.0
	 */
	public static void log(Throwable t) {
		IStatus status= new Status(IStatus.ERROR, getDefault().getDescriptor().getUniqueIdentifier(), INTERNAL_ERROR, "Error logged from Debug Core: ", t); //$NON-NLS-1$
		log(status);
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
		log(new Status(IStatus.ERROR, getDescriptor().getUniqueIdentifier(), INTERNAL_ERROR, MessageFormat.format(DebugCoreMessages.getString("DebugPlugin.Invalid_status_handler_extension__{0}_2"), new String[] {id}), e)); //$NON-NLS-1$
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

	/**
	 * Returns whether any event filters are registered
	 * 
	 * @return whether any event filters are registered
	 */
	private boolean hasEventFilters() {
		return fEventFilters != null && fEventFilters.size() > 0;
	}

	
	
}


