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
import org.eclipse.core.runtime.Status;

/**
 * <p>
 * An operation approver that consults the contexts of an operation to determine
 * whether it should proceed.
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
public class ContextConsultingOperationApprover implements IOperationApprover {

	/**
	 * Return true if the specified context should be consulted as part of
	 * approving the operation redo. The default is to always consult the
	 * context if it has installed an operation approver.
	 */
	protected boolean consultContextForRedo(OperationContext context,
			IOperationHistory history, IOperation operation) {
		return context.getOperationApprover() != null;
	}

	/**
	 * Return true if the specified context should be consulted as part of
	 * approving the operation undo. The default is to always consult the
	 * context if it has installed an operation approver.
	 */
	protected boolean consultContextForUndo(OperationContext context,
			IOperationHistory history, IOperation operation) {
		return context.getOperationApprover() != null;
	}

	public IStatus proceedRedoing(IOperation operation,
			IOperationHistory history) {
		OperationContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			OperationContext context = contexts[i];
			if (consultContextForRedo(context, history, operation)) {
				IStatus approval = context.getOperationApprover()
						.proceedRedoing(operation, context, history);
				if (!approval.isOK())
					return approval;
			}
		}
		return Status.OK_STATUS;
	}

	public IStatus proceedUndoing(IOperation operation,
			IOperationHistory history) {
		OperationContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			OperationContext context = contexts[i];
			if (consultContextForUndo(context, history, operation)) {
				IStatus approval = context.getOperationApprover()
						.proceedUndoing(operation, context, history);
				if (!approval.isOK())
					return approval;
			}
		}
		return Status.OK_STATUS;
	}
}
