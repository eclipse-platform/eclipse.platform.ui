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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.UndoManagerAdapter;

public class RedoRefactoringAction extends UndoManagerAction {

	private int fPatternLength;

	public RedoRefactoringAction() {
	}

	/* (non-Javadoc)
	 * Method declared in UndoManagerAction
	 */
	protected String getName() {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		return RefactoringUIMessages.getString("RedoRefactoringAction.name"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared in UndoManagerAction
	 */
	protected IRunnableWithProgress createOperation() {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactor change
		return new IRunnableWithProgress(){
			public void run(IProgressMonitor pm) throws InvocationTargetException {
				try {
					setPreflightStatus(RefactoringCore.getUndoManager().performRedo(pm));
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
			public void redoStackChanged(IUndoManager manager) {
				IAction action= getAction();
				if (action == null)
					return;
				boolean enabled= false;
				String text= null;
				if (manager.anythingToRedo()) {
					enabled= true;
					text= getActionText();
				} else {
					text= RefactoringUIMessages.getString("RedoRefactoringAction.label"); //$NON-NLS-1$
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
			fPatternLength= RefactoringUIMessages.getString("RedoRefactoringAction.extendedLabel").length(); //$NON-NLS-1$
			IUndoManager undoManager = RefactoringCore.getUndoManager();
			if (undoManager.anythingToRedo()) {
				if (undoManager.peekRedoName() != null)
					action.setText(getActionText());
				action.setEnabled(true);
			} else {
				action.setEnabled(false);
			}
		}
	}	
	
	private String getActionText() {
		return shortenText(RefactoringUIMessages.getFormattedString(
			"RedoRefactoringAction.extendedLabel", //$NON-NLS-1$
			RefactoringCore.getUndoManager().peekRedoName()), fPatternLength);
	}
}
