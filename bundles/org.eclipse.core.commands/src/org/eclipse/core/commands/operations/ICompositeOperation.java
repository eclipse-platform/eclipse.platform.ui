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
 * ICompositeOperation defines an undoable operation that is composed of child
 * operations. Requests to execute, undo, or redo result in the operation taking
 * place on the composite as a whole.
 * </p>
 * 
 * @since 3.1
 */
public interface ICompositeOperation extends IUndoableOperation {

	/**
	 * Add the specified operation as a child of this operation.
	 * 
	 * @param operation
	 *            the operation to be added. If the operation instance has
	 *            already been added, this method will have no effect.
	 */
	void add(IUndoableOperation operation);

	/**
	 * Remove the specified operation from this operation.
	 * 
	 * @param operation
	 *            the operation to be removed. This method will have no effect
	 *            if the operation instance is not already a child.
	 */
	void remove(IUndoableOperation operation);
}
