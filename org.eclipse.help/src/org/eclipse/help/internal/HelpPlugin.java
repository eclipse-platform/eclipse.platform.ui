/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.context.*;
import org.eclipse.help.internal.toc.*;
/**
 * Help System Core plug-in
 */
public class HelpPlugin extends Plugin {
	public final static String PLUGIN_ID = "org.eclipse.help";
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_CONTEXT = false;
	public static boolean DEBUG_PROTOCOLS = false;
	protected static HelpPlugin plugin;

	public final static String BASE_TOCS_KEY = "baseTOCS";

	protected TocManager tocManager;
	protected static Object tocManagerCreateLock = new Object();
	protected ContextManager contextManager;

	private IHelpRoleManager roleManager;
	/**
	 * Logs an Error message with an exception. Note that the message should
	 * already be localized to proper locale. ie: Resources.getString() should
	 * already have been called
	 */
	public static synchronized void logError(String message, Throwable ex) {
		if (message == null)
			message = "";
		Status errorStatus =
			new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex);
		HelpPlugin.getDefault().getLog().log(errorStatus);
	}
	/**
	 * Logs a Warning message with an exception. Note that the message should
	 * already be localized to proper local. ie: Resources.getString() should
	 * already have been called
	 */
	public static synchronized void logWarning(String message) {
		if (HelpPlugin.DEBUG) {
			if (message == null)
				message = "";
			Status warningStatus =
				new Status(
					IStatus.WARNING,
					PLUGIN_ID,
					IStatus.OK,
					message,
					null);
			HelpPlugin.getDefault().getLog().log(warningStatus);
		}
	}

	/**
	 * Plugin constructor. It is called as part of plugin activation.
	 */
	public HelpPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}
	/**
	 * @return the singleton instance of the plugin
	 */
	public static HelpPlugin getDefault() {
		return plugin;
	}
	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * <p>
	 * This method should be re-implemented in subclasses that need to do
	 * something when the plug-in is shut down. Implementors should call the
	 * inherited method to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * Plug-in shutdown code should be robust. In particular, this method
	 * should always make an effort to shut down the plug-in. Furthermore, the
	 * code should not assume that the plug-in was started successfully, as
	 * this method will be invoked in the event of a failure during startup.
	 * </p>
	 * <p>
	 * Note 1: If a plug-in has been started, this method will be automatically
	 * invoked by the platform when the platform is shut down.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple termination of the
	 * plug-in environment. The platform may terminate invocations that do not
	 * complete in a timely fashion.
	 * </p>
	 * <b>Cliens must never explicitly call this method.</b>
	 * 
	 * @exception CoreException
	 *                if this method fails to shut down this plug-in
	 */
	public void shutdown() throws CoreException {
	}
	/**
	 * Starts up this plug-in.
	 * <p>
	 * This method should be overridden in subclasses that need to do something
	 * when this plug-in is started. Implementors should call the inherited
	 * method to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * If this method throws an exception, it is taken as an indication that
	 * plug-in initialization has failed; as a result, the plug-in will not be
	 * activated; moreover, the plug-in will be marked as disabled and
	 * ineligible for activation for the duration.
	 * </p>
	 * <p>
	 * Plug-in startup code should be robust. In the event of a startup
	 * failure, the plug-in's <code>shutdown</code> method will be invoked
	 * automatically, in an attempt to close open files, etc.
	 * </p>
	 * <p>
	 * Note 1: This method is automatically invoked by the platform the first
	 * time any code in the plug-in is executed.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple initialization of the
	 * plug-in environment. The platform may terminate initializers that do not
	 * complete in a timely fashion.
	 * </p>
	 * <b>Cliens must never explicitly call this method.</b>
	 * 
	 * @exception CoreException
	 *                if this plug-in did not start up properly
	 */
	public void startup() throws CoreException {
		// Setup debugging options
		DEBUG = isDebugging();
		if (DEBUG) {
			DEBUG_CONTEXT = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/context")); //$NON-NLS-1$
			DEBUG_PROTOCOLS = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/protocols")); //$NON-NLS-1$
		}
	}
	/**
	 * Used to obtain Toc Naviagiont Manager
	 * 
	 * @return instance of TocManager
	 */
	public static TocManager getTocManager() {
		if (getDefault().tocManager == null) {
			synchronized (tocManagerCreateLock) {
				if (getDefault().tocManager == null) {
					getDefault().tocManager = new TocManager();
				}
			}
		}
		return getDefault().tocManager;
	}
	/**
	 * Used to obtain Context Manager returns an instance of ContextManager
	 */
	public static ContextManager getContextManager() {
		if (getDefault().contextManager == null)
			getDefault().contextManager = new ContextManager();
		return getDefault().contextManager;
	}

	/**
	 * Used to obtain Role Manager
	 * 
	 * @return instance of IHelpRoleManager
	 */
	public static IHelpRoleManager getRoleManager() {
		return getDefault().roleManager;
	}

	/**
	 * Sets the role manager
	 * 
	 * @param roleManager
	 */
	public static void setRoleManager(IHelpRoleManager roleManager) {
		getDefault().roleManager = roleManager;
	}
}
