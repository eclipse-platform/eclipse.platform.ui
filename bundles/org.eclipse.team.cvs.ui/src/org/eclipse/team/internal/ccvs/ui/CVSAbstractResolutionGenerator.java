/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class CVSAbstractResolutionGenerator implements IMarkerResolutionGenerator {
	protected Shell getShell() {
		IWorkbenchWindow window = CVSUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		} else {
			Display display = Display.getCurrent();
			return new Shell(display);
		}
	}
	
	protected void run(final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		final Exception[] exception = new Exception[] {null};
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
					new ProgressMonitorDialog(getShell()).run(true, true, runnable);
				} catch (InterruptedException e) {
					exception[0] = e;
				} catch (InvocationTargetException e) {
					exception[0] = e;
				}
			}
		});
		if (exception[0] != null) {
			if (exception[0] instanceof InvocationTargetException) {
				throw (InvocationTargetException)exception[0];
			} else if (exception[0] instanceof InterruptedException) {
				throw (InterruptedException)exception[0];
			} else {
				throw new InvocationTargetException(exception[0]);
			}
		}
	}
	
	/**
	 * Shows the given errors to the user.
	 * 
	 * @param status  the status containing the error
	 * @param title  the title of the error dialog
	 * @param message  the message for the error dialog
	 * @param shell  the shell to open the error dialog in
	 */
	protected void handle(Throwable exception, String title, final String message) {		
		// Handle the target exception for InvocationTargetExceptions
		if (exception instanceof InvocationTargetException) {
			handle(((InvocationTargetException)exception).getTargetException(), title, message);
			return;
		}
		
		// Create a status to be displayed for the exception
		IStatus status = null;
		boolean log = false;
		boolean dialog = false;
		if (exception instanceof TeamException) {
			status = ((TeamException)exception).getStatus();
			log = false;
			dialog = true;
		} else if (exception instanceof CoreException) {
			status = ((CoreException)exception).getStatus();
			log = true;
			dialog = true;
		} else if (exception instanceof InterruptedException) {
			return;
		} else {
			status = new Status(IStatus.ERROR, CVSUIPlugin.ID, 1, Policy.bind("TeamAction.internal"), exception); //$NON-NLS-1$
			log = true;
			dialog = true;
		}
		
		// Display and/or log as appropriate
		if (status == null) return;
		if (!status.isOK()) {
			IStatus toShow = status;
			if (status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				if (children.length == 1) {
					toShow = children[0];
				}
			}
			if (title == null) title = status.getMessage();
			if (dialog) {
				final IStatus showStatus = toShow;
				final String displayTitle = title;
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						ErrorDialog.openError(getShell(), displayTitle, message, showStatus);
					}
				});
			}
			if (log) CVSUIPlugin.log(toShow);
		}
	}
}
