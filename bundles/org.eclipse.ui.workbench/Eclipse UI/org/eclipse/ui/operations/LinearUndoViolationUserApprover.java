/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.operations;

import org.eclipse.core.commands.operations.IOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.LinearUndoViolationDetector;
import org.eclipse.core.commands.operations.OperationContext;
import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.part.WorkbenchPart;

/**
 * <p>
 * An operation approver that prompts the user to see if linear undo violations
 * are permitted.
 * </p>
 * <p>
 * Note: This class/interface is part of a new API under development. It has
 * been added to builds so that clients can start using the new features.
 * However, it may change significantly before reaching stability. It is being
 * made available at this early stage to solicit feedback with the understanding
 * that any code that uses this API may be broken as the API evolves.
 * </p>
 * 
 * @since 3.1
 * @experimental
 */
public class LinearUndoViolationUserApprover extends
		LinearUndoViolationDetector {

	private boolean fFlushConflictingChangesOnProceed = true;

	private WorkbenchPart fPart;

	public LinearUndoViolationUserApprover(WorkbenchPart part) {
		super();
		fPart = part;
	}

	protected IStatus allowLinearRedoViolation(IOperation operation,
			OperationContext context, IOperationHistory history) {

		String message = WorkbenchMessages
				.format(
						"LinearUndoViolationUserApprover.allowLinearRedoViolation.message", new Object[] { operation.getLabel() }); //$NON-NLS-1$
		// Show a dialog.
		String[] buttons = new String[] { IDialogConstants.YES_LABEL,
				IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
		fPart.setFocus();
		MessageDialog d = new MessageDialog(fPart.getSite().getShell(), fPart
				.getPartName(), null, message, MessageDialog.QUESTION, buttons,
				0);
		int choice = d.open();
		// Branch on the user choice.
		// The choice id is based on the order of button labels above.
		switch (choice) {
		case 0: // redo the local changes first
			while (operation != history.getRedoOperation(context)) {
				history.redo(context, null);
			}
			return Status.OK_STATUS;
		case 1: // don't redo the other changes, and flush them if requested
			if (fFlushConflictingChangesOnProceed) {
				IOperation opToRemove;
				while (operation != (opToRemove = history
						.getRedoOperation(context))) {
					history.remove(opToRemove);
				}
			}
			return Status.OK_STATUS;
		case 2: // cancel
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.operations.LinearUndoViolationDetector#allowLinearRedoConflict(org.eclipse.core.operations.IOperation,
	 *      org.eclipse.core.operations.OperationContext,
	 *      org.eclipse.core.operations.IOperationHistory)
	 */
	protected IStatus allowLinearUndoViolation(IOperation operation,
			OperationContext context, IOperationHistory history) {

		String message = WorkbenchMessages
				.format(
						"There have been local changes in this editor since {0} was performed.  Undo those changes before undoing {0}?", new Object[] { operation.getLabel() }); //$NON-NLS-1$
		// Show a dialog.
		String[] buttons = new String[] { IDialogConstants.YES_LABEL,
				IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
		fPart.setFocus();
		MessageDialog d = new MessageDialog(fPart.getSite().getShell(), fPart
				.getPartName(), null, message, MessageDialog.QUESTION, buttons,
				0);
		int choice = d.open();
		// Branch on the user choice.
		// The choice id is based on the order of button labels above.
		switch (choice) {
		case 0: // redo the local changes first
			while (operation != history.getUndoOperation(context)) {
				history.undo(context, null);
			}
			return Status.OK_STATUS;
		case 1: // don't undo the other changes, and flush them if requested
			if (fFlushConflictingChangesOnProceed) {
				IOperation opToRemove;
				while (operation != (opToRemove = history
						.getUndoOperation(context))) {
					history.remove(opToRemove);
				}
			}
			return Status.OK_STATUS;
		case 2: // cancel
			return OperationStatus.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}

}
