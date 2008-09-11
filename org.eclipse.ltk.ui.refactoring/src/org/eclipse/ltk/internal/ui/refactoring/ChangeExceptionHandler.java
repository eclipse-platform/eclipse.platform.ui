/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;

public class ChangeExceptionHandler {

	private Shell fParent;
	private String fName;

	private static class RefactorErrorDialog extends ErrorDialog {
		public RefactorErrorDialog(Shell parentShell, String dialogTitle, String dialogMessage, IStatus status, int displayMask) {
			super(parentShell, dialogTitle, dialogMessage, status, displayMask);
		}
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			Button ok= getButton(IDialogConstants.OK_ID);
			ok.setText( RefactoringUIMessages.ChangeExceptionHandler_undo);
			Button abort= createButton(parent, IDialogConstants.CANCEL_ID, RefactoringUIMessages.ChangeExceptionHandler_abort, true);
			abort.moveBelow(ok);
			abort.setFocus();
		}
		protected Control createMessageArea (Composite parent) {
			Control result= super.createMessageArea(parent);

			// Panic code: use 'parent' instead of 'result' in case super implementation changes in the future
			new Label(parent, SWT.NONE); // filler as parent has 2 columns (icon and label)
			Label label= new Label(parent, SWT.NONE);
			label.setText(RefactoringUIMessages.ChangeExceptionHandler_button_explanation);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			applyDialogFont(result);
			return result;
		}
	}

	public ChangeExceptionHandler(Shell parent, Refactoring refactoring) {
		fParent= parent;
		fName= refactoring.getName();
	}

	public void handle(Change change, RuntimeException exception) {
		RefactoringUIPlugin.log(exception);
		IStatus status= null;
		if (exception.getMessage() == null) {
			status= new Status(IStatus.ERROR, RefactoringUIPlugin.getPluginId(), IStatus.ERROR,
				RefactoringUIMessages.ChangeExceptionHandler_no_details, exception);
		} else {
			status= new Status(IStatus.ERROR, RefactoringUIPlugin.getPluginId(), IStatus.ERROR,
				exception.getMessage(), exception);
		}
		handle(change, status);
	}

	public void handle(Change change, CoreException exception) {
		RefactoringUIPlugin.log(exception);
		handle(change, exception.getStatus());
	}

	private void handle(Change change, IStatus status) {
		if (change instanceof CompositeChange) {
			Change undo= ((CompositeChange)change).getUndoUntilException();
			if (undo != null) {
				RefactoringUIPlugin.log(status);
				final ErrorDialog dialog= new RefactorErrorDialog(fParent,
					RefactoringUIMessages.ChangeExceptionHandler_refactoring,
					Messages.format(RefactoringUIMessages.ChangeExceptionHandler_unexpected_exception, fName),
					status, IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
				int result= dialog.open();
				if (result == IDialogConstants.OK_ID) {
					performUndo(undo);
				}
				return;
		}
		}
		ErrorDialog dialog= new ErrorDialog(fParent,
			RefactoringUIMessages.ChangeExceptionHandler_refactoring,
			Messages.format(RefactoringUIMessages.ChangeExceptionHandler_unexpected_exception, fName),
			status, IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
		dialog.open();
	}

	private void performUndo(final Change undo) {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask("", 11); //$NON-NLS-1$
				try {
					undo.initializeValidationData(new NotCancelableProgressMonitor(new SubProgressMonitor(monitor, 1)));
					if (undo.isValid(new SubProgressMonitor(monitor,1)).hasFatalError()) {
						monitor.done();
						return;
					}
					undo.perform(new SubProgressMonitor(monitor, 9));
				} finally {
					undo.dispose();
				}
			}
		};
		WorkbenchRunnableAdapter adapter= new WorkbenchRunnableAdapter(runnable,
			ResourcesPlugin.getWorkspace().getRoot());
		ProgressMonitorDialog dialog= new ProgressMonitorDialog(fParent);
		try {
			dialog.run(false, false, adapter);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, fParent,
				RefactoringUIMessages.ChangeExceptionHandler_rollback_title,
				RefactoringUIMessages.ChangeExceptionHandler_rollback_message + fName);
		} catch (InterruptedException e) {
			// can't happen
		}
	}
}
