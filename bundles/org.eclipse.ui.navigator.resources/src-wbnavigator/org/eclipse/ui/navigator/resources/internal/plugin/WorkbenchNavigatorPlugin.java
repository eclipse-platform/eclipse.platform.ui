/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.resources.internal.plugin;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus; 
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class for the workbench Navigator.
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * @since 3.2
 */
public class WorkbenchNavigatorPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static WorkbenchNavigatorPlugin plugin;
	public static String PLUGIN_ID = "org.eclipse.ui.navigator.resources"; //$NON-NLS-1$

	/**
	 * Creates a new instance of the receiver
	 */
	public WorkbenchNavigatorPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static WorkbenchNavigatorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Logs errors.
	 */
	public static void log(String message, IStatus status) {
		if (message != null) {
			getDefault().getLog().log( new Status(IStatus.ERROR, PLUGIN_ID, 0, message, null));
			System.err.println(message + "\nReason:"); //$NON-NLS-1$
		}
		getDefault().getLog().log(status);
		System.err.println(status.getMessage());
	}
}
