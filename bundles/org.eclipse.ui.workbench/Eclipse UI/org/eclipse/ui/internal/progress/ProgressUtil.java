/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;

/**
 * The ProgressUtil is a class that contains static utility methods used for the progress
 * API.
 */
class ProgressUtil {

	/**
	 * Return a status for the exception.
	 * @param exception
	 * @return
	 */
	static Status exceptionStatus(Throwable exception) {
		return new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, exception.getMessage(), exception);
	}

	/**
	 * Log the exception for debugging.
	 * @param exception
	 */
	static void logException(Throwable exception) {
		Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog().log(exceptionStatus(exception));
	}

	/**
	 * Open a progress view in the current page. This method
	 * must be called from the UI Thread as this works within
	 * the UI.
	 * @param IWorkbenchWindow the window to open the view in.
	 */
	static void openProgressView(IWorkbenchWindow window) {
		try {
			IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null)
				activePage.showView(JobProgressManager.PROGRESS_VIEW_NAME);
		} catch (PartInitException exception) {
			logException(exception);
		}
	}

}
