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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * IOperation defines an operation that can be executed, undone, and redone.
 * Operations typically have fully defined parameters. That is, they are usually
 * created after the user has been queried for any input needed to define the
 * operation.
 * </p>
 * <p>
 * Operations determine their ability to execute, undo, or redo according to the
 * current state of the application. They do not make decisions about their
 * validity based on where they occur in the operation history. That is left to
 * the particular operation history.
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
public interface IOperation {

	/**
	 * Add the specified context to the operation. If the context is already
	 * present, do not add it again.
	 * 
	 * @param context -
	 *            the context to be added
	 */
	void addContext(OperationContext context);

	/**
	 * Returns whether the operation can be executed in its current state.
	 * 
	 * @return <code>true</code> if the operation can be executed;
	 *         <code>false</code> otherwise.
	 */
	boolean canExecute();

	/**
	 * Returns whether the operation can be redone in its current state.
	 * 
	 * @return <code>true</code> if the operation can be redone;
	 *         <code>false</code> otherwise.
	 */
	boolean canRedo();

	/**
	 * Returns whether the operation can be undone in its current state.
	 * 
	 * @return <code>true</code> if the operation can be undone;
	 *         <code>false</code> otherwise.
	 */
	boolean canUndo();

	/**
	 * Dispose of the operation. This method is used when the operation is no
	 * longer kept in the history. Implementers of this method typically
	 * unregister any listeners.
	 * 
	 */
	void dispose();

	/**
	 * Execute the operation. This method should only be called the first time
	 * that an operation is executed.
	 * 
	 * @param monitor
	 * @return the IStatus of the execution. The status severity should be set
	 *         to <code>OK</code> if the operation was successful, and
	 *         <code>ERROR</code> if it was not. Any other status is assumed
	 *         to represent an incompletion of the execution.
	 */
	IStatus execute(IProgressMonitor monitor);

	/**
	 * Returns the array of contexts that have been assigned to the operation.
	 * 
	 * @return the array of contexts
	 */
	OperationContext[] getContexts();

	/**
	 * Return the description that should be used to further describe this
	 * operation to the user. The description is used in history lists when the
	 * user requests more information about an operation.
	 * 
	 * @return the description
	 */
	String getDescription();

	/**
	 * Return the label that should be used to show the name of the operation to
	 * the user. This label is typically appended to the "Undo" or "Redo" menu
	 * entry.
	 * 
	 * @return the label
	 */
	String getLabel();

	/**
	 * Returns whether the operation has the specified context.
	 * 
	 * @param context -
	 *            the context in question
	 * @return <code>true</code> if the context is present, <code>false</code>
	 *         if it is not.
	 */
	boolean hasContext(OperationContext context);

	/**
	 * Redo the operation. This method should only be called after an operation
	 * has been undone.
	 * 
	 * @param monitor
	 * @return the IStatus of the redo. The status severity should be set to
	 *         <code>OK</code> if the redo was successful, and
	 *         <code>ERROR</code> if it was not. Any other status is assumed
	 *         to represent an incompletion of the redo.
	 */
	IStatus redo(IProgressMonitor monitor);

	/**
	 * Remove the specified context from the operation. This method has no
	 * effect if the context is not present.
	 * 
	 * @param context -
	 *            the context to be removed
	 */
	void removeContext(OperationContext context);

	/**
	 * Undo the operation. This method should only be called after an operation
	 * has been executed.
	 * 
	 * @param monitor
	 * @return the IStatus of the undo. The status severity should be set to
	 *         <code>OK</code> if the redo was successful, and
	 *         <code>ERROR</code> if it was not. Any other status is assumed
	 *         to represent an incompletion of the undo.
	 */
	IStatus undo(IProgressMonitor monitor);

}
