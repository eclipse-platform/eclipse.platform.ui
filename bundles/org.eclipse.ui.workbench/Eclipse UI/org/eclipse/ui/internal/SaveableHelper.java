/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.dialogs.EventLoopProgressMonitor;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;

/**
 * Helper class for prompting to save dirty views or editors.
 * 
 * @since 3.0.1
 */
public class SaveableHelper {
	
	private static int AutomatedResponse = -1; 
	
	/**
	 * FOR USE BY THE AUTOMATED TEST HARNESS ONLY.
	 * 
	 * Sets the response to use when <code>savePart</code> is called with <code>confirm=true</code>. 
	 * 
	 * @param response 0 for yes, 1 for no, 2 for cancel, -1 for default (prompt)
	 */
	public static void testSetAutomatedResponse(int response) {
		AutomatedResponse = response;
	}
	
	/*
	 * Saves the workbench part.
	 */
	static boolean savePart(final ISaveablePart saveable, IWorkbenchPart part, IWorkbenchWindow window, boolean confirm) {
		// Short circuit.
		if (!saveable.isDirty())
			return true;

		// If confirmation is required ..
		if (confirm) {
			int choice = AutomatedResponse;
			if (choice == -1) {
				String message = WorkbenchMessages.format("EditorManager.saveChangesQuestion", new Object[] { part.getTitle()}); //$NON-NLS-1$
				// Show a dialog.
				String[] buttons = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
					MessageDialog d = new MessageDialog(
						window.getShell(), WorkbenchMessages.getString("Save_Resource"), //$NON-NLS-1$
						null, message, MessageDialog.QUESTION, buttons, 0);
				choice = d.open();
			}

			// Branch on the user choice.
			// The choice id is based on the order of button labels above.
			switch (choice) {
				case 0 : //yes
					break;
				case 1 : //no
					return true;
				default :
				case 2 : //cancel
					return false;
			}
		}

		// Create save block.
		IRunnableWithProgress progressOp = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				IProgressMonitor monitorWrap = new EventLoopProgressMonitor(monitor);
				saveable.doSave(monitorWrap);
			}
		};

		// Do the save.
		return runProgressMonitorOperation(WorkbenchMessages.getString("Save"), progressOp,window); //$NON-NLS-1$
	}
	/**
	 * Runs a progress monitor operation.
	 * Returns true if success, false if cancelled.
	 */
	static boolean runProgressMonitorOperation(String opName, final IRunnableWithProgress progressOp,IWorkbenchWindow window) {
		IRunnableContext ctx;
		if (window instanceof ApplicationWindow) {
			ctx = window;
		} else {
			ctx = new ProgressMonitorJobsDialog(window.getShell());
		}
		final boolean[] wasCanceled = new boolean[1];
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				progressOp.run(monitor);
				wasCanceled[0] = monitor.isCanceled();
			}
		};

		try {
			ctx.run(false, true, runnable);
		} catch (InvocationTargetException e) {
			String title = WorkbenchMessages.format("EditorManager.operationFailed", new Object[] { opName }); //$NON-NLS-1$
			Throwable targetExc = e.getTargetException();
			WorkbenchPlugin.log(title, new Status(IStatus.WARNING, PlatformUI.PLUGIN_ID, 0, title, targetExc));
			MessageDialog.openError(window.getShell(), WorkbenchMessages.getString("Error"), //$NON-NLS-1$
			title + ':' + targetExc.getMessage());
		} catch (InterruptedException e) {
			// Ignore.  The user pressed cancel.
			wasCanceled[0] = true;
		}
		return !wasCanceled[0];
	}
}
