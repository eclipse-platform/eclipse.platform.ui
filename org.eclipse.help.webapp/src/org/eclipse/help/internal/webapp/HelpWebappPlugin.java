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
package org.eclipse.help.internal.webapp;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
/**
 * Welp web application plug-in.
 */
public class HelpWebappPlugin extends Plugin {
	public final static String PLUGIN_ID = "org.eclipse.help.webapp";
	// debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_WORKINGSETS = false;

	protected static HelpWebappPlugin plugin;
	/** 
	 * Logs an Error message with an exception. Note that the message should already 
	 * be localized to proper locale.
	 * ie: WebappResources.getString() should already have been called
	 */
	public static synchronized void logError(String message, Throwable ex) {
		if (message == null)
			message = "";
		Status errorStatus =
			new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, ex);
		HelpWebappPlugin.getDefault().getLog().log(errorStatus);
	}
	/** 
	 * Logs a Warning message with an exception. Note that the message should already 
	 * be localized to proper local.
	 * ie: WebappResources.getString() should already have been called
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
			HelpWebappPlugin.getDefault().getLog().log(warningStatus);
		}
	}

	/**
	 * Plug-in constructor.  It is called as part of plugin
	 * activation.
	 */
	public HelpWebappPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}
	/**
	 * @return the singleton instance of the help webapp plugin
	 */
	public static HelpWebappPlugin getDefault() {
		return plugin;
	}
	public void startup() throws CoreException {
		// Setup debugging options
		DEBUG = isDebugging();
		if (DEBUG) {
			DEBUG_WORKINGSETS = "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.help.webapp/debug/workingsets")); //$NON-NLS-1$
		}
	}
}
