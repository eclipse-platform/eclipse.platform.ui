/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * 
 * Not exposed as API.
 * @since 3.2
 *
 */
public class NavigatorTestsPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static NavigatorTestsPlugin plugin; 
	/**
	 * The plugin id
	 */
	public static String PLUGIN_ID = "org.eclipse.ui.tests.navigator"; //$NON-NLS-1$

	/**
	 * Creates a new instance of the receiver
	 */
	public NavigatorTestsPlugin() {
 		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static NavigatorTestsPlugin getDefault() {
		return plugin;
	}
 

	/**
	 * Log the given status to the ISV log.
	 * 
	 * When to use this:
	 * 
	 * This should be used when a PluginException or a ExtensionException occur but for which an
	 * error dialog cannot be safely shown.
	 * 
	 * If you can show an ErrorDialog then do so, and do not call this method.
	 * 
	 * If you have a plugin exception or core exception in hand call log(String, IStatus)
	 * 
	 * This convenience method is for internal use by the Workbench only and must not be called
	 * outside the workbench.
	 * 
	 * This method is supported in the event the log allows plugin related information to be logged
	 * (1FTTJKV). This would be done by this method.
	 * 
	 * This method is internal to the workbench and must not be called by any plugins, or examples.
	 * 
	 * @param message
	 *            A high level UI message describing when the problem happened.
	 *  
	 */

	public static void log(String message) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, 0, message, null));
		System.err.println(message);
		//1FTTJKV: ITPCORE:ALL - log(status) does not allow plugin information to be recorded
	}

	/**
	 * Logs errors.
	 */
	public static void log(String message, IStatus status) {
		if (message != null) {
			getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, 0, message, null));
			System.err.println(message + "\nReason:"); //$NON-NLS-1$
		}
		getDefault().getLog().log(status);
		System.err.println(status.getMessage());
	} 
	

	public static void logError(int aCode, String aMessage, Throwable anException) { 
		getDefault().getLog().log(createErrorStatus(aCode, aMessage, anException)); 
	}

	public static void log(int severity, int aCode, String aMessage, Throwable exception) {
		log(createStatus(severity, aCode, aMessage, exception));
	}

	public static void log(IStatus aStatus) { 
		getDefault().getLog().log(aStatus); 
	} 
	 
	public static IStatus createStatus(int severity, int aCode, String aMessage, Throwable exception) {
		return new Status(severity, PLUGIN_ID, aCode, aMessage, exception);
	}
 
	public static IStatus createErrorStatus(int aCode, String aMessage, Throwable exception) {
		return createStatus(IStatus.ERROR, aCode, aMessage, exception);
	} 
}
