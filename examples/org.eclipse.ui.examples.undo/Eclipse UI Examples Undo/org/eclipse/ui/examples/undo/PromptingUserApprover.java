/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.undo;

import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.examples.undo.preferences.PreferenceConstants;

/**
 * An operation approver that prompts the user to see whether an undo or redo
 * should continue. An example preference is checked to determine if prompting
 * should occur.
 */
public final class PromptingUserApprover implements IOperationApprover {

	private IUndoContext context;

	/*
	 * Create the operation approver.
	 */
	public PromptingUserApprover(IUndoContext context) {
		super();
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedRedoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus proceedRedoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo) {

		// return immediately if the operation is not relevant
		if (!operation.hasContext(context))
			return Status.OK_STATUS;

		// allow the operation if we are not prompting
		boolean prompt = UndoPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.PREF_CONFIRMUNDO);
		if (!prompt)
			return Status.OK_STATUS;
		return prompt(false, operation, uiInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedUndoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus proceedUndoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable uiInfo) {

		// return immediately if the operation is not relevant
		if (!operation.hasContext(context))
			return Status.OK_STATUS;

		// allow the operation if we are not prompting
		boolean prompt = UndoPlugin.getDefault().getPreferenceStore()
				.getBoolean(PreferenceConstants.PREF_CONFIRMUNDO);
		if (!prompt)
			return Status.OK_STATUS;
		return prompt(true, operation, uiInfo);
	}

	/*
	 * Prompt the user as to whether to continue the undo or redo, and return an
	 * OK_STATUS if we should continue, or a CANCEL_STATUS if we should not.
	 */
	private IStatus prompt(boolean undoing, IUndoableOperation operation,
			IAdaptable uiInfo) {
		boolean createdShell = false;
		Shell shell = getShell(uiInfo);
		if (shell == null) {
			if (shell == null) {
				createdShell = true;
				shell = new Shell();
			}
		}
		String command = undoing ? UndoExampleMessages.BoxView_Undo
				: UndoExampleMessages.BoxView_Redo;
		String message = NLS.bind(UndoExampleMessages.BoxView_ConfirmUndo, command,
				operation.getLabel());
		MessageDialogWithToggle dialog = MessageDialogWithToggle
				.openOkCancelConfirm(shell, UndoExampleMessages.BoxView_Title,
						message, UndoExampleMessages.UndoPreferences_DoNotConfirm, false, null, null);
		UndoPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.PREF_CONFIRMUNDO, !dialog.getToggleState());

		if (createdShell)
			shell.dispose();
		if (dialog.getReturnCode() == Window.OK)
			return Status.OK_STATUS;
		return Status.CANCEL_STATUS;
	}

	/*
	 * Return the shell described by the supplied uiInfo, or null if no shell is
	 * described.
	 */
	Shell getShell(IAdaptable uiInfo) {
		if (uiInfo != null) {
			Shell shell = (Shell) uiInfo.getAdapter(Shell.class);
			if (shell != null)
				return shell;
		}
		return null;
	}
}
