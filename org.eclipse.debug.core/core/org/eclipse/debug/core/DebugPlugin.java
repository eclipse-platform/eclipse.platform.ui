package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.core.*;

/**
 * There is one instance of the debug plugin available from
 * <code>DebugPlugin.getDefault()</code>. The debug plugin provides:
 * <ul>
 * <li>access to the breakpoint manager</li>
 * <li>access to the launch manager</li>
 * <li>access to the registered launcher extensions</li>
 * <li>registration for debug events</li>
 * <li>notification of debug events</li>
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
	 * The singleton debug plug-in instance.
	 */
	private static DebugPlugin fgDebugPlugin= null;

	/**
	 * The collection of launcher extensions.
	 */
	private Launcher[] fLaunchers= new Launcher[0];

	/**
	 * The collection of breakpoint factory extensions.
	 */
	private IBreakpointFactory[] fBreakpointFactories= new IBreakpointFactory[0];

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
	 * Internal state...<code>true</code> if this plugin is in the 
	 * process of shutting down.
	 */
	private boolean fShuttingDown= false;	
	
	/**
	 * Returns the singleton instance of the debug plug-in.
	 */
	public static DebugPlugin getDefault() {
		return fgDebugPlugin;
	}

	/**
	 * Constructs the debug plug-in.
	 * <p>
	 * An instance of this plug-in runtime class is automatically created 
	 * when the facilities provided by the Resources plug-in are required.
	 * <b>Clients must never explicitly instantiate a plug-in runtime class.</b>
	 * </p>
	 * 
	 * @param pluginDescriptor the plug-in descriptor for the
	 *   debug plug-in
	 */
	public DebugPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		fgDebugPlugin= this;
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
		if (fShuttingDown || event == null)
			return;
		
		Object[] listeners= fEventListeners.getListeners();
		for (int i= 0; i < listeners.length; i++) {
			((IDebugEventListener) listeners[i]).handleDebugEvent(event);
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
	 * Returns a collection of launcher extensions. Launchers represent
	 * and provide access to launcher extensions, delaying instantiation of
	 * the underlying launcher delegates until required.
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
	 * Loads all launcher extensions.
	 *
	 * @exception CoreException if creation of a launcher extension fails
	 */
	protected void loadLaunchers() throws CoreException {
		IPluginDescriptor descriptor= getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IDebugConstants.EXTENSION_POINT_LAUNCHER);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		fLaunchers= new Launcher[infos.length];
		for (int i= 0; i < infos.length; i++) {
			fLaunchers[i]= new Launcher(infos[i]);
		}
	}
	
	/**
	 * Returns a collection of breakpoint factory extensions. Breakpoint factory represent
	 * and provide access to breakpoint factory extensions.
	 *
	 * @return an array of breakpoint factory
	 * @see org.eclipse.debug.core.IBreakpointFactory
	 */
	public IBreakpointFactory[] getBreakpointFactories() {
		return fBreakpointFactories;
	}
	
	/**
	 * Loads all breakpoint factory extensions.
	 *
	 * @exception CoreException if creation of a breakpoint factory extension fails
	 */
	protected void loadBreakpointFactories() throws CoreException {
		IPluginDescriptor descriptor= getDescriptor();
		IExtensionPoint extensionPoint= descriptor.getExtensionPoint(IDebugConstants.EXTENSION_POINT_BREAKPOINT_FACTORY);
		IConfigurationElement[] infos= extensionPoint.getConfigurationElements();
		fBreakpointFactories= new IBreakpointFactory[infos.length];
		for (int i= 0; i < infos.length; i++) {
			fBreakpointFactories[i]= new BreakpointFactory(infos[i]);
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
		fShuttingDown= true;
		super.shutdown();
		fLaunchManager.shutdown();
		fBreakpointManager.shutdown();
		fgDebugPlugin= null;
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
		loadLaunchers();
		loadBreakpointFactories();		
		fBreakpointManager.startup();
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
}


