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

/**
 * <p>
 * A simple, lightweight operation context that can be used to tag any
 * operation. It does not provided a specialized label or operation approval
 * sequence.
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
public class OperationContext {

	/**
	 * Return a boolean that indicates whether this context should be assigned
	 * to the specified operation. When an operations client decides to assign a
	 * context to an operation, it consults the context itself to help determine
	 * whether the assignment is appropriate for the particular operation. This
	 * method should be overridden by contexts that have complex rules for
	 * whether they should be assigned to a particular operation.
	 * 
	 * @return a boolean indicating whether this context should be assigned to
	 *         the operation.
	 */
	public boolean acceptOperation(IOperation operation) {
		return true;
	}

	/**
	 * Get the label that should be used to describe the context in any views.
	 * Contexts may be shown when filtered operation histories are shown to the
	 * user.
	 * 
	 * @return the label for the context.
	 */
	public String getLabel() {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Get the operation approver that is used to approve undo or redo
	 * operations involving this context.
	 * 
	 * @return the operation approver for the context. A <code>null</code>
	 *         return value indicates that no special approval is necessary.
	 */
	public IContextOperationApprover getOperationApprover() {
		return null;
	}
}
