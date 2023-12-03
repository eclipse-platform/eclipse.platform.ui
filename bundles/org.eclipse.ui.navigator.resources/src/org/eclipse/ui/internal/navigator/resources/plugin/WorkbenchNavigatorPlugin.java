/*******************************************************************************
 * Copyright (c) 2003, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.plugin;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class for the workbench Navigator.
 *
 * @since 3.2
 */
public class WorkbenchNavigatorPlugin extends AbstractUIPlugin {
	// The shared instance.
	private static WorkbenchNavigatorPlugin plugin;

	/** The plugin id */
	public static String PLUGIN_ID = "org.eclipse.ui.navigator.resources"; //$NON-NLS-1$

	/**
	 * Creates a new instance of the receiver
	 */
	public WorkbenchNavigatorPlugin() {
		super();
		plugin = this;
	}

	/**
	 * @return the shared instance.
	 */
	public static WorkbenchNavigatorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Logs errors.
	 * @param message The message to log
	 * @param status The status to log
	 */
	public static void log(String message, IStatus status) {
		if (message != null) {
			getDefault().getLog().log(
					new Status(IStatus.ERROR, PLUGIN_ID, 0, message, null));
		}
		if(status != null) {
			getDefault().getLog().log(status);
		}
	}

	/**
	 * Create a status associated with this plugin.
	 *
	 * @return A status configured with this plugin's id and the given
	 *         parameters.
	 */
	public static IStatus createStatus(int severity, int aCode, String aMessage, Throwable exception) {
		return new Status(severity, PLUGIN_ID, aCode, aMessage != null ? aMessage : "No message.", exception); //$NON-NLS-1$
	}

	/**
	 *
	 * @return A status configured with this plugin's id and the given
	 *         parameters.
	 */
	public static IStatus createErrorStatus(int aCode, String aMessage,
			Throwable exception) {
		return createStatus(IStatus.ERROR, aCode, aMessage, exception);
	}


	/**
	 *
	 * @return A status configured with this plugin's id and the given
	 *         parameters.
	 */
	public static IStatus createErrorStatus(String aMessage,	Throwable exception) {
		return createStatus(IStatus.ERROR, 0, aMessage, exception);
	}

	/**
	 *
	 * @return A status configured with this plugin's id and the given
	 *         parameters.
	 */
	public static IStatus createErrorStatus(String aMessage) {
		return createStatus(IStatus.ERROR, 0, aMessage, null);
	}


	/**
	 *
	 * @return A status configured with this plugin's id and the given
	 *         parameters.
	 */
	public static IStatus createInfoStatus(String aMessage) {
		return createStatus(IStatus.INFO, 0, aMessage, null);
	}

}
