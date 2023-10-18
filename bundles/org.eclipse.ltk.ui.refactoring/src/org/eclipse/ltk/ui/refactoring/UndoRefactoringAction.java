/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.IValidationCheckResultQuery;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.UndoManagerAdapter;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
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
 * @deprecated This action is now longer needed. Undo is now performed via the
 *  global undo/redo stack provided by <code>org.eclipse.core.commands</code>.
 *
 * @since 3.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
@Deprecated
public class UndoRefactoringAction extends UndoManagerAction {

	private int fPatternLength;

	/**
	 * Creates a new undo refactoring action.
	 */
	public UndoRefactoringAction() {
	}

	@Override
	protected String getName() {
		// PR: 1GEWDUH: ITPJCORE:WINNT - Refactoring - Unable to undo refactoring change
		return RefactoringUIMessages.UndoRefactoringAction_name;
	}

	@Override
	protected IRunnableWithProgress createOperation(Shell parent) {
		final IValidationCheckResultQuery query= new Query(parent, RefactoringUIMessages.UndoRefactoringAction_error_title) {
			@Override
			protected String getFullMessage(String errorMessage) {
				return Messages.format(
					RefactoringUIMessages.UndoRefactoringAction_error_message,
					errorMessage);
			}
		};
		return pm -> {
			try {
				RefactoringCore.getUndoManager().performUndo(query, pm);
			} catch (CoreException e) {
				throw new InvocationTargetException(e);
			}
		};
	}

	@Override
	protected UndoManagerAdapter createUndoManagerListener() {
		return new UndoManagerAdapter() {
			@Override
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
					text= RefactoringUIMessages.UndoRefactoringAction_label;
				}
				action.setEnabled(enabled);
				action.setText(text);
			}
		};
	}

	@Override
	public void selectionChanged(IAction action, ISelection s) {
		if (!isHooked()) {
			hookListener(action);
			fPatternLength= RefactoringUIMessages.UndoRefactoringAction_extendedLabel.length();
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
		return shortenText(Messages.format(
			RefactoringUIMessages.UndoRefactoringAction_extendedLabel,
			RefactoringCore.getUndoManager().peekUndoName()), fPatternLength);
	}
}
