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
public class UndoContext implements IUndoContext {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoContext#getLabel()
	 */
	public String getLabel() {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoContext#getOperationApprover()
	 */
	public IContextOperationApprover getOperationApprover() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.operations.IUndoContext#matches(IUndoContext context)
	 */
	public boolean matches(IUndoContext context) {
		return context == this;
	}
}
