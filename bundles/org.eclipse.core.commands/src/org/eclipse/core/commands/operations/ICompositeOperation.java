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
 * ICompositeOperation defines an undoable operation that is composed of
 * child operations.  Requests to execute, undo, or redo result in the
 * operation taking place on the composite as a whole.
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
public interface ICompositeOperation extends IUndoableOperation {
	
	/**
	 * Add the specified operation as a child of this operation.
	 * 
	 * @param operation - the operation to be added
	 */
	void add(IUndoableOperation operation);

	
	/**
	 * Remove the specified operation from this operation.
	 * 
	 * @param operation - the operation to be removed
	 */
	void remove(IUndoableOperation operation);


}
