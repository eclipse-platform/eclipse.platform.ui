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
 * An operation context that can be used to represent any given object. The
 * operation contexts are equal if they both represent the same object.
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
public class ObjectOperationContext extends OperationContext {

	private IContextOperationApprover fApprover = null;

	private Object fObject;

	/**
	 * Construct an operation context that represents the given object.
	 * 
	 * @param object -
	 *            the object to be represented.
	 */
	public ObjectOperationContext(Object object) {
		super();
		fObject = object;
	}

	public String getLabel() {
		return fObject.toString();
	}

	/**
	 * Return the object that is represented by this context.
	 * 
	 * @return - the object represented by this context.
	 */
	public Object getObject() {
		return fObject;
	}

	public IContextOperationApprover getOperationApprover() {
		return fApprover;
	}

	/**
	 * Set the IContextOperationApprover that approves the undo or redo of
	 * operations that have this context.
	 * 
	 * @param approver -
	 *            the operation approver to be used for this context
	 */
	public void setOperationApprover(IContextOperationApprover approver) {
		fApprover = approver;
	}

}
