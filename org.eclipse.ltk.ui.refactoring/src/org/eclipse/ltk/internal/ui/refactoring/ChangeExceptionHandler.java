/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

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
		public RefactorErrorDialog(Shell parentShell, String dialogTitle, String message, IStatus status, int displayMask) {
			super(parentShell, dialogTitle, message, status, displayMask);
		}
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			Button ok= getButton(IDialogConstants.OK_ID);
			ok.setText( RefactoringUIMessages.getString("ChangeExceptionHandler.undo")); //$NON-NLS-1$
			Button abort= createButton(parent, IDialogConstants.CANCEL_ID, RefactoringUIMessages.getString("ChangeExceptionHandler.abort"), true); //$NON-NLS-1$
			abort.moveBelow(ok);
			abort.setFocus();
		}
		protected Control createMessageArea (Composite parent) {
			Control result= super.createMessageArea(parent);
			new Label(parent, SWT.NONE); // filler
			Label label= new Label(parent, SWT.NONE);
			label.setText(RefactoringUIMessages.getString("ChangeExceptionHandler.button_explanation")); //$NON-NLS-1$
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
		IStatus status= null;
		if (exception.getMessage() == null) {
			status= new Status(IStatus.ERROR, RefactoringUIPlugin.getPluginId(), IStatus.ERROR, 
				RefactoringUIMessages.getString("ChangeExceptionHandler.no_details"), exception); //$NON-NLS-1$
		} else {
			status= new Status(IStatus.ERROR, RefactoringUIPlugin.getPluginId(), IStatus.ERROR, 
				exception.getMessage(), exception);
		}
		handle(change, status);
	}
	
	public void handle(Change change, CoreException exception) {
		handle(change, exception.getStatus());
	}
	
	private void handle(Change change, IStatus status) {
		if (change instanceof CompositeChange) {
			Change undo= ((CompositeChange)change).getUndoUntilException();
			if (undo != null) {
				RefactoringUIPlugin.log(status);
				final ErrorDialog dialog= new RefactorErrorDialog(fParent,
					RefactoringUIMessages.getString("ChangeExceptionHandler.refactoring"), //$NON-NLS-1$
					RefactoringUIMessages.getFormattedString("ChangeExceptionHandler.unexpected_exception", new String[] {fName}), //$NON-NLS-1$
					status, IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR); 
				int result= dialog.open();
				if (result == IDialogConstants.OK_ID) {
					performUndo(undo);
				}
				return;
			}
		}
		ErrorDialog dialog= new ErrorDialog(fParent,
			RefactoringUIMessages.getString("ChangeExceptionHandler.refactoring"), //$NON-NLS-1$
			RefactoringUIMessages.getFormattedString("ChangeExceptionHandler.unexpected_exception", new String[] {fName}), //$NON-NLS-1$
			status, IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR); 
		dialog.open();
	}
	
	private void performUndo(final Change undo) {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask("", 11); //$NON-NLS-1$
				undo.initializeValidationData(new SubProgressMonitor(monitor, 1));
				if (undo.isValid(new SubProgressMonitor(monitor,1)).hasFatalError()) {
					monitor.done();
					return;
				}
				undo.perform(new SubProgressMonitor(monitor, 9));
			}
		};
		WorkbenchRunnableAdapter adapter= new WorkbenchRunnableAdapter(runnable,
			ResourcesPlugin.getWorkspace().getRoot());
		ProgressMonitorDialog dialog= new ProgressMonitorDialog(fParent);
		try {
			dialog.run(false, false, adapter);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, fParent, 
				RefactoringUIMessages.getString("ChangeExceptionHandler.rollback.title"),  //$NON-NLS-1$
				RefactoringUIMessages.getString("ChangeExceptionHandler.rollback.message") + fName); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// can't happen
		}
	}
}
