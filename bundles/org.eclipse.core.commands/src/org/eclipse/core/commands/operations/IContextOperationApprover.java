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
package org.eclipse.core.commands.operations;

import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * IContextOperationApprover defines an interface for approving the undo or redo
 * of a particular operation within an operation history given a particular
 * operation context. Operations that are candidates for undo or redo have
 * already been validated against their current state.
 * </p>
 * <p>
 * By the time an IContextOperationApprover is consulted, the undo has already
 * been requested. Approvers should return an IStatus with severity
 * <code>OK</code> if the operation should proceed, and any other status if it
 * should not. The status should further describe the rejection. When an
 * operation is rejected, it is expected that the object rejecting the operation
 * has already consulted the user if necessary or otherwise provided any
 * necessary information to the user about the rejection.
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
public interface IContextOperationApprover {

	/**
	 * Return a status indicating whether the specified operation should be
	 * redone. Any status that does not have severity <code>IStatus.OK</code>
	 * will not be approved. Implementers should not assume that the redo will
	 * be performed when the status is <code>OK</code>, since other operation
	 * approvers may veto the redo.
	 * 
	 * @param operation -
	 *            the operation to be redone
	 * @param context -
	 *            the context for which the operation is to be approved
	 * @param history -
	 *            the history undoing the operation
	 * @return the IStatus describing whether the operation is approved. The
	 *         redo will not proceed if the status severity is no
	 *         <code>OK</code>, and the caller requesting the redo will be
	 *         returned the status that caused the rejection. Any other status
	 *         severities will not be interpreted by the history.
	 */
	IStatus proceedRedoing(IOperation operation, OperationContext context,
			IOperationHistory history);

	/**
	 * Return a status indicating whether the specified operation should be
	 * undone. Any status that does not have severity <code>IStatus.OK</code>
	 * will not be approved. Implementers should not assume that the undo will
	 * be performed when the status is <code>OK</code>, since other operation
	 * approvers can veto the undo.
	 * 
	 * @param operation -
	 *            the operation to be undone
	 * @param context -
	 *            the context for which the operation is to be approved
	 * @param history -
	 *            the history undoing the operation
	 * @return the IStatus describing whether the operation is approved. The
	 *         undo will not proceed if the status severity is not
	 *         <code>OK</code>, and the caller requesting the undo will be
	 *         returned the status that caused the rejection. Any other status
	 *         severities will not be interpreted by the history.
	 */
	IStatus proceedUndoing(IOperation operation, OperationContext context,
			IOperationHistory history);
}
