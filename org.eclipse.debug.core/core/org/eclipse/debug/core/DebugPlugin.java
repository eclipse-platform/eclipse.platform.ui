package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.core.BreakpointManager;
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
	 * The singleton launch manager.
	 */
	private LaunchManager fLaunchManager;

	/**
	 * The collection of debug event listeners.
	 */
	private ListenerList fEventListeners= new ListenerList(20);

	/**
	 * Whether this plugin is in the process of shutting
	 * down.
	 */
	private boolean fShuttingDown= false;	
	
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
	 *
	 * @param event the debug event to fire
	 */
	public void fireDebugEvent(DebugEvent event) {
		if (isShuttingDown() || event == null)
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
	 * Returns the expression manager.
	 *
	 * @return the expression manager
	 * @see IExpressionManager
	 * @since 2.0
	 */
	public IExpressionManager getExpressionManager() {
		return null;
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
		createLaunchers();	
		fBreakpointManager.startup();
		fLaunchManager.startup();
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
}


