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
package org.eclipse.ltk.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.UndoManagerAdapter;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.UndoManagerAction;

/**
 * The refactoring undo action. When executed the action performs 
 * the top most change from the refactoring undo manager's undo
 * stack.
 * <p>
 * The action is typically added to a global refactoring menu via
 * the <code>org.eclipse.ui.actionSets</code> extension point.
 * </p>
 * <p>
 * Note: this class isn't intended to be subclassed. Clients are only
 * allowed to instantiate the class or to reference it from an action 
 * set.
 * </p>
 *  
 * @since 3.0
 */
public class UndoRefactoringAction extends UndoManagerAction implements IWorkbenchWindowActionDelegate {

	private int fPatternLength;

	/**
	 * Creates a new undo refactoring action.
	 */
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
