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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.UndoManagerAdapter;

public class UndoRefactoringAction extends UndoManagerAction {

	private int fPatternLength;

	public UndoRefactoringAction() {
	}

	/* (non-Javadoc)
	 * Method declared in UndoManagerAction
	 */
	protected String getName() {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		return RefactoringUIMessages.getString("UndoRefactoringAction.name"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared in UndoManagerAction
	 */
	protected IRunnableWithProgress createOperation(Shell parent) {
		final IValidationCheckResultQuery query= new Query(parent, RefactoringUIMessages.getString("UndoRefactoringAction.error.title")) { //$NON-NLS-1$
			protected String getFullMessage(String errorMessage) {
				return RefactoringUIMessages.getFormattedString(
					"UndoRefactoringAction.error.message",  //$NON-NLS-1$
					errorMessage);
			}
		};
		return new IRunnableWithProgress(){
			public void run(IProgressMonitor pm) throws InvocationTargetException {
				try {
					RefactoringCore.getUndoManager().performUndo(query, pm);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);			
				}
			}
		};
	}
	
	/* (non-Javadoc)
	 * Method declared in UndoManagerAction
	 */
	protected UndoManagerAdapter createUndoManagerListener() {
		return new UndoManagerAdapter() {
			public void undoStackChanged(IUndoManager manager) {
				IAction action= getAction();
				if (action == null)
					return;
				boolean enabled= false;
				String text= null;
				if (manager.anythingToUndo()) {
					enabled= true;
					text= getActionText();
				} else {
					text= RefactoringUIMessages.getString("UndoRefactoringAction.label"); //$NON-NLS-1$
				}
				action.setEnabled(enabled);
				action.setText(text);
			}
		};
	}	
	
	/* (non-Javadoc)
	 * Method declared in IActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection s) {
		if (!isHooked()) {
			hookListener(action);
			fPatternLength= RefactoringUIMessages.getString("UndoRefactoringAction.extendedLabel").length(); //$NON-NLS-1$
			IUndoManager undoManager = RefactoringCore.getUndoManager();
			if (undoManager.anythingToUndo()) {
				if (undoManager.peekUndoName() != null)
					action.setText(getActionText());
				action.setEnabled(true);
			} else {
				action.setEnabled(false);
			}
		}
	}	
	
	private String getActionText() {
		return shortenText(RefactoringUIMessages.getFormattedString(
			"UndoRefactoringAction.extendedLabel", //$NON-NLS-1$
			RefactoringCore.getUndoManager().peekUndoName()), fPatternLength);
	}	
}
