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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.UndoManagerAdapter;

abstract class UndoManagerAction implements IWorkbenchWindowActionDelegate {

	private static final int MAX_LENGTH= 30;

	private RefactoringStatus fPreflightStatus;
	private IAction fAction;
	private IWorkbenchWindow fWorkbenchWindow;
	private UndoManagerAdapter fUndoManagerListener;

	public UndoManagerAction() {
	}
	
	protected abstract IRunnableWithProgress createOperation();
	
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
		IRunnableWithProgress op= createOperation();
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
		
		if (fPreflightStatus != null && fPreflightStatus.hasError()) {
			String name= getName();
			MultiStatus status = createMultiStatus();
			String message= RefactoringUIMessages.getFormattedString("UndoManagerAction.cannot_be_executed", name); //$NON-NLS-1$
			ErrorDialog error= new ErrorDialog(parent, name, message, status, IStatus.ERROR) {
				public void create() {
					super.create();
					buttonPressed(IDialogConstants.DETAILS_ID);
				}
			};
			error.open();
		}
		fPreflightStatus= null;
	}
	
	/* package */ void setPreflightStatus(RefactoringStatus status) {
		fPreflightStatus= status;
	}
	
	private MultiStatus createMultiStatus() {
		MultiStatus status= new MultiStatus(
			RefactoringUIPlugin.getPluginId(), 
			IStatus.ERROR,
			RefactoringUIMessages.getString("UndoManagerAction.validation_failed"), //$NON-NLS-1$
			null);
		RefactoringStatusEntry[] entries= fPreflightStatus.getEntries();
		for (int i= 0; i < entries.length; i++) {
			String pluginId= entries[i].getPluginId();
			status.merge(new Status(
				IStatus.ERROR,
				pluginId != null ? pluginId : RefactoringUIPlugin.getPluginId(),
				IStatus.ERROR,
				entries[i].getMessage(),
				null));
		}
		return status;
	}
}
