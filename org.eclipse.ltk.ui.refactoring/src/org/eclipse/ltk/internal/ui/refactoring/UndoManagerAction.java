/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.UndoManagerAdapter;

abstract class UndoManagerAction implements IWorkbenchWindowActionDelegate {

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
			return true;
		}
		public void stopped(final RefactoringStatus status) {
			Display display= fParent.getDisplay();
			Runnable r= new Runnable() {
				public void run() {
					String message= status.getMessageMatchingSeverity(RefactoringStatus.FATAL);
					MessageDialog.openWarning(fParent, fTitle, getFullMessage(message));
				}
			};
			display.syncExec(r);
		}
		protected abstract String getFullMessage(String errorMessage);
	}

	public UndoManagerAction() {
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
			// Don't execute in separate thread since it updates the UI.
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false, false, op);
		} catch (InvocationTargetException e) {
			RefactoringCore.getUndoManager().flush();
			ExceptionHandler.handle(e,
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				RefactoringUIMessages.getString("UndoManagerAction.internal_error.title"),  //$NON-NLS-1$
				RefactoringUIMessages.getString("UndoManagerAction.internal_error.message")); //$NON-NLS-1$
		} catch (InterruptedException e) {
			// Opertation isn't cancelable.
		}
	}
}
