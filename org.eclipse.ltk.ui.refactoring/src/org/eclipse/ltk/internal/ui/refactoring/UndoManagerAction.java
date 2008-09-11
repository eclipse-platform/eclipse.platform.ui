/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.OperationCanceledException;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.UndoManagerAdapter;
import org.eclipse.ltk.ui.refactoring.RefactoringUI;

public abstract class UndoManagerAction implements IWorkbenchWindowActionDelegate {

	private static final int MAX_LENGTH= 30;

	private IAction fAction;
	private IWorkbenchWindow fWorkbenchWindow;
	private UndoManagerAdapter fUndoManagerListener;

	protected static abstract class Query implements IValidationCheckResultQuery  {
		private Shell fParent;
		private String fTitle;
		public Query(Shell parent, String title) {
			fParent= parent;
			fTitle= title;
		}
		public boolean proceed(RefactoringStatus status) {
			final Dialog dialog= RefactoringUI.createRefactoringStatusDialog(status, fParent, fTitle, false);
			final int[] result= new int[1];
			Runnable r= new Runnable() {
				public void run() {
					result[0]= dialog.open();
				}
			};
			fParent.getDisplay().syncExec(r);
			return result[0] == IDialogConstants.OK_ID;
		}
		public void stopped(final RefactoringStatus status) {
			Runnable r= new Runnable() {
				public void run() {
					String message= status.getMessageMatchingSeverity(RefactoringStatus.FATAL);
					MessageDialog.openWarning(fParent, fTitle, getFullMessage(message));
				}
			};
			fParent.getDisplay().syncExec(r);
		}
		protected abstract String getFullMessage(String errorMessage);
	}

	protected UndoManagerAction() {
	}

	protected abstract IRunnableWithProgress createOperation(Shell parent);

	protected abstract UndoManagerAdapter createUndoManagerListener();

	protected abstract String getName();

	protected IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}

	protected IAction getAction() {
		return fAction;
	}

	protected boolean isHooked() {
		return fAction != null;
	}

	protected void hookListener(IAction action) {
		if (isHooked())
			return;
		fAction= action;
		fUndoManagerListener= createUndoManagerListener();
		RefactoringCore.getUndoManager().addListener(fUndoManagerListener);
	}

	protected String shortenText(String text, int patternLength) {
		int length= text.length();
		final int finalLength = MAX_LENGTH + patternLength;
		if (text.length() <= finalLength)
			return text;
		StringBuffer result= new StringBuffer();
		int mid= finalLength / 2;
		result.append(text.substring(0, mid));
		result.append("..."); //$NON-NLS-1$
		result.append(text.substring(length - mid));
		return result.toString();
	}

	/* (non-Javadoc)
	 * Method declared in IActionDelegate
	 */
	public void dispose() {
		if (fUndoManagerListener != null)
			RefactoringCore.getUndoManager().removeListener(fUndoManagerListener);
		fWorkbenchWindow= null;
		fAction= null;
		fUndoManagerListener= null;
	}

	/* (non-Javadoc)
	 * Method declared in IActionDelegate
	 */
	public void init(IWorkbenchWindow window) {
		fWorkbenchWindow= window;
	}

	/* (non-Javadoc)
	 * Method declared in IActionDelegate
	 */
	public void run(IAction action) {
		Shell parent= fWorkbenchWindow.getShell();
		IRunnableWithProgress op= createOperation(parent);
		try {
			PlatformUI.getWorkbench().getProgressService().runInUI(
				new ProgressMonitorDialog(fWorkbenchWindow.getShell()),
				op, ResourcesPlugin.getWorkspace().getRoot());
		} catch (InvocationTargetException e) {
			RefactoringCore.getUndoManager().flush();
			ExceptionHandler.handle(e,
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				RefactoringUIMessages.UndoManagerAction_internal_error_title,
				RefactoringUIMessages.UndoManagerAction_internal_error_message);
		} catch (InterruptedException e) {
			// Operation isn't cancelable.
		} catch (OperationCanceledException e) {
			// the waiting dialog got canceled.
		}
	}
}
