/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablePart2;
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
	
	/**
	 * The helper must prompt.
	 */
	public static final int USER_RESPONSE = -1;
	
	private static int AutomatedResponse = USER_RESPONSE; 
	
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
	
	/**
	 * FOR USE BY THE AUTOMATED TEST HARNESS ONLY.
	 * 
	 * Sets the response to use when <code>savePart</code> is called with <code>confirm=true</code>. 
	 * 
	 * @return 0 for yes, 1 for no, 2 for cancel, -1 for default (prompt)
	 */
	public static int testGetAutomatedResponse() {
		return AutomatedResponse;
	}
	
	/**
	 * Saves the workbench part.
	 * @param saveable the part
	 * @param part the same part
	 * @param window the workbench window
	 * @param confirm request confirmation
	 * @return <code>true</code> for continue, <code>false</code> if the operation
	 * was cancelled.
	 */
	static boolean savePart(final ISaveablePart saveable, IWorkbenchPart part, 
			IWorkbenchWindow window, boolean confirm) {
		// Short circuit.
		if (!saveable.isDirty())
			return true;

		// If confirmation is required ..
		if (confirm) {
			int choice = AutomatedResponse;
			if (choice == USER_RESPONSE) {				
				if (saveable instanceof ISaveablePart2) {
					choice = ((ISaveablePart2)saveable).promptToSaveOnClose();
				}
				if (choice == USER_RESPONSE || choice == ISaveablePart2.DEFAULT) {
					String message = NLS.bind(WorkbenchMessages.EditorManager_saveChangesQuestion, part.getTitle()); 
					// Show a dialog.
					String[] buttons = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
						MessageDialog d = new MessageDialog(
							window.getShell(), WorkbenchMessages.Save_Resource,
							null, message, MessageDialog.QUESTION, buttons, 0);
					choice = d.open();
				}
			}

			// Branch on the user choice.
			// The choice id is based on the order of button labels above.
			switch (choice) {
				case ISaveablePart2.YES : //yes
					break;
				case ISaveablePart2.NO : //no
					return true;
				default :
				case ISaveablePart2.CANCEL : //cancel
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
		return runProgressMonitorOperation(WorkbenchMessages.Save, progressOp,window); 
	}
	
	/**
	 * Saves the workbench part ... this is similar to 
	 * {@link SaveableHelper#savePart(ISaveablePart, IWorkbenchPart, IWorkbenchWindow, boolean) }
	 * except that the {@link ISaveablePart2#DEFAULT } case must cause the
	 * calling function to allow this part to participate in the default saving
	 * mechanism.
	 * 
	 * @param saveable the part
	 * @param window the workbench window
	 * @param confirm request confirmation
	 * @return the ISaveablePart2 constant
	 */
	static int savePart(final ISaveablePart2 saveable, 
			IWorkbenchWindow window, boolean confirm) {
		// Short circuit.
		if (!saveable.isDirty())
			return ISaveablePart2.YES;

		// If confirmation is required ..
		if (confirm) {
			int choice = AutomatedResponse;
			if (choice == USER_RESPONSE) {
				choice = saveable.promptToSaveOnClose();
			}

			// Branch on the user choice.
			// The choice id is based on the order of button labels above.
			if (choice!=ISaveablePart2.YES) {
				return (choice==USER_RESPONSE?ISaveablePart2.DEFAULT:choice);
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
		if (!runProgressMonitorOperation(WorkbenchMessages.Save, progressOp,window)) {
			return ISaveablePart2.CANCEL;
		}
		return ISaveablePart2.YES;
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
			String title = NLS.bind(WorkbenchMessages.EditorManager_operationFailed, opName ); 
			Throwable targetExc = e.getTargetException();
			WorkbenchPlugin.log(title, new Status(IStatus.WARNING, PlatformUI.PLUGIN_ID, 0, title, targetExc));
			MessageDialog.openError(window.getShell(), WorkbenchMessages.Error, title + ':' + targetExc.getMessage());
		} catch (InterruptedException e) {
			// Ignore.  The user pressed cancel.
			wasCanceled[0] = true;
		}
		return !wasCanceled[0];
	}
}
