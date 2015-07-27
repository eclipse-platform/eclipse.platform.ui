/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

/**
 * Extends {@link IOperationApprover} to approve the execution of a particular
 * operation within an operation history. Operations that are candidates for
 * execution have already been validated against their current state and
 * according to the rules of the history. Prior to 3.2, an operation approver
 * was only consulted for undo and redo of an operation, not its initial
 * execution.
 * <p>
 * By the time an IOperationApprover2 is consulted, the execution has already
 * been requested and it has been determined that the operation is valid.
 * Approvers should return an <code>IStatus</code> object with severity
 * <code>OK</code> if the operation should proceed, and any other severity if
 * it should not. When an operation is not approved, it is expected that the
 * object not allowing the operation has already consulted the user if necessary
 * or otherwise provided any necessary information to the user about the fact
 * that the operation is not approved.
 * </p>
 * <p>
 * Like {@link IOperationApprover}, implementers of this extension must be
 * prepared to receive the approval messages from a background thread. Any UI
 * access occurring inside the implementation must be properly synchronized
 * using the techniques specified by the client's widget library.
 * </p>
 *
 * @since 3.2
 */
public interface IOperationApprover2 extends IOperationApprover {
	/**
	 * Return a status indicating whether the specified operation should be
	 * executed. Any status that does not have severity <code>IStatus.OK</code>
	 * will not be approved. Implementers should not assume that the execution
	 * will be performed when the status is <code>OK</code>, since other
	 * operation approvers may veto the execution.
	 *
	 * @param operation
	 *            the operation to be executed
	 * @param history
	 *            the history performing the execution of the operation
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it should minimally contain an adapter
	 *            for the org.eclipse.swt.widgets.Shell.class. Even if UI
	 *            information is provided, the implementation of this method
	 *            must be prepared for being called from a background thread.
	 *            Any UI access must be properly synchronized using the
	 *            techniques specified by the client's widget library.
	 * @return the IStatus describing whether the operation is approved. The
	 *         execution will not proceed if the status severity is not
	 *         <code>OK</code>, and the caller requesting the execution will
	 *         be returned the status that caused the rejection. Any other
	 *         status severities will not be interpreted by the history.
	 */
	IStatus proceedExecuting(IUndoableOperation operation,
			IOperationHistory history, IAdaptable info);
}