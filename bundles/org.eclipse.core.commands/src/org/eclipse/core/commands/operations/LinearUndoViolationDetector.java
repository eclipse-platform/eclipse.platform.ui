/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
 * behavior for allowing whether or not the undo/redo should proceed.
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
public abstract class LinearUndoViolationDetector implements
		IContextOperationApprover {

	/*
	 * Return whether a linear redo violation is allowable.  A linear redo violation
	 * is defined as a request to redo a particular operation even if it is not the most
	 * recently added operation to the redo history.
	 */
	protected abstract IStatus allowLinearRedoViolation(IUndoableOperation operation,
			IUndoContext context, IOperationHistory history, IAdaptable uiInfo);

	/*
	 * Return whether a linear undo violation is allowable.  A linear undo violation
	 * is defined as a request to undo a particular operation even if it is not the most
	 * recently added operation to the undo history.
	 */
	protected abstract IStatus allowLinearUndoViolation(IUndoableOperation operation,
			IUndoContext context, IOperationHistory history, IAdaptable uiInfo);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedRedoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IUndoContext, org.eclipse.core.commands.operations.IOperationHistory, org.eclipse.core.runtime.IAdaptable)
	 */
	public IStatus proceedRedoing(IUndoableOperation operation,
			IUndoContext context, IOperationHistory history, IAdaptable info) {
		if (history.getRedoOperation(context) != operation)
			return allowLinearRedoViolation(operation, context, history, info);

		return Status.OK_STATUS;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.IOperationApprover#proceedUndoing(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IUndoContext, org.eclipse.core.commands.operations.IOperationHistory, org.eclipse.core.runtime.IAdaptable)
	 */

	public IStatus proceedUndoing(IUndoableOperation operation,
			IUndoContext context, IOperationHistory history, IAdaptable info) {
		if (history.getUndoOperation(context) != operation)
			return allowLinearUndoViolation(operation, context, history, info);

		return Status.OK_STATUS;
	}

}
