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

	public IStatus proceedRedoing(IOperation operation,
			IOperationHistory history) {
		OperationContext[] contexts = operation.getContexts();
		for (int i = 0; i < contexts.length; i++) {
			OperationContext context = contexts[i];
			IContextOperationApprover approver = context.getOperationApprover();
			if (approver != null) {
				IStatus approval = approver.proceedRedoing(operation, context, history);
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
			IContextOperationApprover approver = context.getOperationApprover();
			if (approver != null) {
				IStatus approval = approver.proceedUndoing(operation, context, history);
				if (!approval.isOK())
					return approval;
			}
		}
		return Status.OK_STATUS;
	}
}
