/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * A helper class that supports error report and logging for 
 * Spy plug-in classes. 
 */
public class ErrorUtil {

	/**
	 * Logs the provided exception and user message in Spy plug-in's log.
	 * 
	 * @param exception the exception to be logged
	 * @param userMessage an optional  higher-level explanation for the exception
	 */
	public static void logException(Exception exception, String userMessage) {
		String pluginID = CoreToolsPlugin.PLUGIN_ID;
		if (userMessage == null)
			userMessage = exception.getMessage();
		IStatus status = new Status(IStatus.ERROR, pluginID, -1, userMessage, exception);
		CoreToolsPlugin.getDefault().getLog().log(status);
	}

	/**
	 * Shows the provided message using a MessageDialog.
	 * 
	 * @param message
	 * @param title
	 * @see org.eclipse.jface.dialogs.MessageDialog#openError(Shell,String,String)
	 */
	public static void showErrorMessage(String message, String title) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		if (title == null)
			title = "Error in Spy plug-in"; //$NON-NLS-1$
		MessageDialog.openError(shell, title, message);
	}
}