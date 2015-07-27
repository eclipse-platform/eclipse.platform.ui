/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.commands.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * IUndoableOperation defines an operation that can be executed, undone, and
 * redone. Operations typically have fully defined parameters. That is, they are
 * usually created after the user has been queried for any input needed to
 * define the operation.
 * </p>
 * <p>
 * Operations determine their ability to execute, undo, or redo according to the
 * current state of the application. They do not make decisions about their
 * validity based on where they occur in the operation history. That is left to
 * the particular operation history.
 * </p>
 *
 * @since 3.1
 */
public interface IUndoableOperation {

	/**
	 * <p>
	 * Add the specified context to the operation. If a context equal to the
	 * specified context is already present, do not add it again. Note that
	 * determining whether a context is already present is based on equality,
	 * not whether the context matches ({@link IUndoContext#matches(IUndoContext)})
	 * another context.
	 * </p>
	 *
	 * @param context
	 *            the context to be added
	 */
	void addContext(IUndoContext context);

	/**
	 * <p>
	 * Returns whether the operation can be executed in its current state.
	 * </p>
	 *
	 * <p>
	 * Note: The computation for this method must be fast, as it is called
	 * frequently. If necessary, this method can be optimistic in its
	 * computation (returning true) and later perform more time-consuming
	 * computations during the actual execution of the operation, returning the
	 * appropriate status if the operation cannot actually execute at that time.
	 * </p>
	 *
	 * @return <code>true</code> if the operation can be executed;
	 *         <code>false</code> otherwise.
	 */
	boolean canExecute();

	/**
	 * <p>
	 * Returns whether the operation can be redone in its current state.
	 * </p>
	 *
	 * <p>
	 * Note: The computation for this method must be fast, as it is called
	 * frequently. If necessary, this method can be optimistic in its
	 * computation (returning true) and later perform more time-consuming
	 * computations during the actual redo of the operation, returning the
	 * appropriate status if the operation cannot actually be redone at that
	 * time.
	 * </p>
	 *
	 * @return <code>true</code> if the operation can be redone;
	 *         <code>false</code> otherwise.
	 */
	boolean canRedo();

	/**
	 * <p>
	 * Returns whether the operation can be undone in its current state.
	 * </p>
	 *
	 * <p>
	 * Note: The computation for this method must be fast, as it is called
	 * frequently. If necessary, this method can be optimistic in its
	 * computation (returning true) and later perform more time-consuming
	 * computations during the actual undo of the operation, returning the
	 * appropriate status if the operation cannot actually be undone at that
	 * time.
	 * </p>
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
	 *            the progress monitor (or <code>null</code>) to use for
	 *            reporting progress to the user.
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it should minimally contain an adapter
	 *            for the org.eclipse.swt.widgets.Shell.class.
	 *
	 * @return the IStatus of the execution. The status severity should be set
	 *         to <code>OK</code> if the operation was successful, and
	 *         <code>ERROR</code> if it was not. Any other status is assumed
	 *         to represent an incompletion of the execution.
	 * @throws ExecutionException
	 *             if an exception occurred during execution.
	 */
	IStatus execute(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException;

	/**
	 * <p>
	 * Returns the array of contexts that have been assigned to the operation.
	 * </p>
	 * <p>
	 * This method may be called by the operation history from inside a
	 * synchronized block. To avoid deadlock conditions, implementers of this
	 * method must avoid dispatching and waiting on threads that modify the
	 * operation history during this method.
	 * </p>
	 *
	 * @return the array of contexts
	 */
	IUndoContext[] getContexts();

	/**
	 * Return the label that should be used to show the name of the operation to
	 * the user. This label is typically combined with the command strings shown
	 * to the user in "Undo" and "Redo" user interfaces.
	 *
	 * @return the String label.  Should never be <code>null</code>.
	 */
	String getLabel();

	/**
	 * <p>
	 * Returns whether the operation has a matching context for the specified
	 * context.
	 * </p>
	 * <p>
	 * This method may be called by the operation history from inside a
	 * synchronized block. To avoid deadlock conditions, implementers of this
	 * method must avoid dispatching and waiting on threads that modify the
	 * operation history during this method.
	 * </p>
	 *
	 * @see IUndoContext#matches(IUndoContext)
	 *
	 * @param context
	 *            the context in question
	 * @return <code>true</code> if the context is present, <code>false</code>
	 *         if it is not.
	 */
	boolean hasContext(IUndoContext context);

	/**
	 * Redo the operation. This method should only be called after an operation
	 * has been undone.
	 *
	 * @param monitor
	 *            the progress monitor (or <code>null</code>) to use for
	 *            reporting progress to the user.
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it should minimally contain an adapter
	 *            for the org.eclipse.swt.widgets.Shell.class.
	 * @return the IStatus of the redo. The status severity should be set to
	 *         <code>OK</code> if the redo was successful, and
	 *         <code>ERROR</code> if it was not. Any other status is assumed
	 *         to represent an incompletion of the redo.
	 * @throws ExecutionException
	 *             if an exception occurred during redo.
	 */

	IStatus redo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException;

	/**
	 * Remove the specified context from the operation. This method has no
	 * effect if the context is not equal to another context in the context
	 * list. Note that determining whether a context is present when removing it
	 * is based on equality, not whether the context matches ({@link
	 * IUndoContext#matches(IUndoContext)}) another context.
	 *
	 * @param context
	 *            the context to be removed
	 */
	void removeContext(IUndoContext context);

	/**
	 * Undo the operation. This method should only be called after an operation
	 * has been executed.
	 *
	 * @param monitor
	 *            the progress monitor (or <code>null</code>) to use for
	 *            reporting progress to the user.
	 * @param info
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it should minimally contain an adapter
	 *            for the org.eclipse.swt.widgets.Shell.class.
	 * @return the IStatus of the undo. The status severity should be set to
	 *         <code>OK</code> if the redo was successful, and
	 *         <code>ERROR</code> if it was not. Any other status is assumed
	 *         to represent an incompletion of the undo.
	 * @throws ExecutionException
	 *             if an exception occurred during undo.
	 */
	IStatus undo(IProgressMonitor monitor, IAdaptable info)
			throws ExecutionException;

}
