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
package org.eclipse.help.internal.base;
import org.eclipse.core.runtime.*;
/**
 * Simple plugin for help system.
 */
public class HelpBasePlugin extends Plugin {
	public final static String PLUGIN_ID = "org.eclipse.help.base";
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_SEARCH = false;

	protected static HelpBasePlugin plugin;
	/** 
	 * Logs an Error message with an exception. Note that the message should already 
	 * be localized to proper locale.
	 * ie: Resources.getString() should already have been called
	 */
	public static synchronized void logError(String message, Throwable ex) {
		if (message == null)
			message = "";
		Status errorStatus =
			new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex);
		HelpBasePlugin.getDefault().getLog().log(errorStatus);
	}
	/** 
	 * Logs a Warning message with an exception. Note that the message should already 
	 * be localized to proper local.
	 * ie: Resources.getString() should already have been called
	 */
	public static synchronized void logWarning(String message) {
		if (HelpBasePlugin.DEBUG) {
			if (message == null)
				message = "";
			Status warningStatus =
				new Status(
					IStatus.WARNING,
					PLUGIN_ID,
					IStatus.OK,
					message,
					null);
			HelpBasePlugin.getDefault().getLog().log(warningStatus);
		}
	}

	/**
	 * HelpViewerPlugin constructor. It is called as part of plugin
	 * activation.
	 */
	public HelpBasePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}
	/**
	 * @return the singleton instance of the help plugin
	 */
	public static HelpBasePlugin getDefault() {
		return plugin;
	}
	/**
	 * Shuts down this plug-in and discards all plug-in state.
	 * <p>
	 * This method should be re-implemented in subclasses that need to do something
	 * when the plug-in is shut down.  Implementors should call the inherited method
	 * to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * Plug-in shutdown code should be robust. In particular, this method
	 * should always make an effort to shut down the plug-in. Furthermore,
	 * the code should not assume that the plug-in was started successfully,
	 * as this method will be invoked in the event of a failure during startup.
	 * </p>
	 * <p>
	 * Note 1: If a plug-in has been started, this method will be automatically
	 * invoked by the platform when the platform is shut down.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple termination
	 * of the plug-in environment. The platform may terminate invocations
	 * that do not complete in a timely fashion.
	 * </p>
	 * <b>Cliens must never explicitly call this method.</b>
	 *
	 * @exception CoreException if this method fails to shut down
	 *   this plug-in 
	 */
	public void shutdown() throws CoreException {
		BaseHelpSystem.shutdown();
	}
	/**
	 * Starts up this plug-in.
	 * <p>
	 * This method should be overridden in subclasses that need to do something
	 * when this plug-in is started.  Implementors should call the inherited method
	 * to ensure that any system requirements can be met.
	 * </p>
	 * <p>
	 * If this method throws an exception, it is taken as an indication that
	 * plug-in initialization has failed; as a result, the plug-in will not
	 * be activated; moreover, the plug-in will be marked as disabled and 
	 * ineligible for activation for the duration.
	 * </p>
	 * <p>
	 * Plug-in startup code should be robust. In the event of a startup failure,
	 * the plug-in's <code>shutdown</code> method will be invoked automatically,
	 * in an attempt to close open files, etc.
	 * </p>
	 * <p>
	 * Note 1: This method is automatically invoked by the platform 
	 * the first time any code in the plug-in is executed.
	 * </p>
	 * <p>
	 * Note 2: This method is intended to perform simple initialization 
	 * of the plug-in environment. The platform may terminate initializers 
	 * that do not complete in a timely fashion.
	 * </p>
	 * <b>Cliens must never explicitly call this method.</b>
	 *
	 * @exception CoreException if this plug-in did not start up properly
	 */
	public void startup() throws CoreException {
		// Setup debugging options
		DEBUG = isDebugging();
		if (DEBUG) {
			DEBUG_SEARCH = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.help.base/debug/search")); //$NON-NLS-1$
		}

		BaseHelpSystem.startup();
	}

	/**
	* Initializes the default preferences settings for this plug-in.
	* 
	* @since 2.0
	*/
	protected void initializeDefaultPluginPreferences() {
		Preferences prefs = getPluginPreferences();

		String os = System.getProperty("os.name").toLowerCase();
		boolean isWindows = os.indexOf("windows") != -1;

		if (isWindows)
			prefs.setDefault(
				"custom_browser_path",
				"\"C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE\" %1");
		else
			prefs.setDefault("custom_browser_path", "mozilla %1");
	}
}
