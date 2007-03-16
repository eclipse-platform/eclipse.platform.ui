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
package org.eclipse.core.commands.operations;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * <p>
 * An abstract class for detecting violations in a strict linear undo/redo
 * model. Once a violation is detected, subclasses implement the specific
 * behavior for indicating whether or not the undo/redo should proceed.
 * </p>
 * 
 * @since 3.1
 */
public abstract class LinearUndoViolationDetector implements IOperationApprover {

	/**
	 * Create an instance of LinearUndoViolationDetector.
	 */
	public LinearUndoViolationDetector() {
		super();
	}

	/**
	 * Return a status indicating whether a linear redo violation is allowable.
	 * A linear redo violation is defined as a request to redo a particular
	 * operation even if it is not the most recently added operation to the redo
	 * history.
	 * 
	 * @param operation
	 *            the operation for which a linear redo violation has been
	 *            detected.
	 * @param context
	 *            the undo context in which the linear redo violation exists
	 * @param history
	 *            the operation history containing the operation
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it should minimally contain an adapter
	 *            for the org.eclipse.swt.widgets.Shell.class.
	 * 
	 * @return the IStatus describing whether the redo violation is allowed. The
	 *         redo will not proceed if the status severity is not
	 *         <code>OK</code>, and the caller requesting the redo will be
	 *         returned the status that caused the rejection. Specific status
	 *         severities will not be interpreted by the history.
	 */

	protected abstract IStatus allowLinearRedoViolation(
			IUndoableOperation operation, IUndoContext context,
			IOperationHistory history, IAdaptable info);

	/**
	 * Return a status indicating whether a linear undo violation is allowable.
	 * A linear undo violation is defined as a request to undo a particular
	 * operation even if it is not the most recently added operation to the undo
	 * history.
	 * 
	 * @param operation
	 *            the operation for which a linear undo violation has been
	 *            detected.
	 * @param context
	 *            the undo context in which the linear undo violation exists
	 * @param history
	 *            the operation history containing the operation
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it should minimally contain an adapter
	 *            for the org.eclipse.swt.widgets.Shell.class.
	 * 
	 * @return the IStatus describing whether the undo violation is allowed. The
	 *         undo will not proceed if the status severity is not
	 *         <code>OK</code>, and the caller requesting the undo will be
	 *         returned the status that caused the rejection. Specific status
	 *         severities will not be interpreted by the history.
	 */
	protected abstract IStatus allowLinearUndoViolation(
			IUndoableOperation operation, IUndoContext context,
			IOperationHistory history, IAdaptable info);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedRedoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	public final IStatus proceedRedoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable info) {
		IUndoContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			IUndoContext context = contexts[i];
			if (history.getRedoOperation(context) != operation) {
				IStatus status = allowLinearRedoViolation(operation, context,
						history, info);
				if (!status.isOK()) {
					return status;
				}
			}
		}
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedUndoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */

	public final IStatus proceedUndoing(IUndoableOperation operation,
			IOperationHistory history, IAdaptable info) {
		IUndoContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			IUndoContext context = contexts[i];
			if (history.getUndoOperation(context) != operation) {
				IStatus status = allowLinearUndoViolation(operation, context,
						history, info);
				if (!status.isOK()) {
					return status;
				}
			}
		}
		return Status.OK_STATUS;
	}
}
