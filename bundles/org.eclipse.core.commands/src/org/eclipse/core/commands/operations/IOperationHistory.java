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
 * IOperationHistory tracks a history of operations that can be undone or
 * redone. Operations are added to the history once they have been initially
 * executed. Clients may choose whether to have the operations history perform
 * the initial execution or simply add the operation to the history.
 * </p>
 * <p>
 * Once operations are added to the history, the methods <code>canRedo()</code>
 * and <code>canUndo()</code> are used to determine whether there is an
 * operation available for undo and redo in a given operation context. The
 * context-based protocol implies that there is only one operation that can be
 * undone or redone at a given time in a given context. This is typical of a
 * linear undo model, when only the most recently executed operation is
 * available for undo. When this protocol is used, a linear model is enforced by
 * the history. It is up to clients to determine how to maintain a history that
 * is invalid or stale. For example, when the most recent operation for a
 * context cannot be performed, clients may wish to flush the history for that
 * context.
 * </p>
 * <p>
 * Additional protocol allows direct undo and redo of a specified operation,
 * regardless of its position in the history. When a more flexible undo model is
 * supported, these methods can be implemented to undo and redo directly
 * specified operations. If an implementer of IOperationHistory does not allow
 * direct undo and redo, these methods can return a status indicating that it is
 * not allowed.
 * </p>
 * <p>
 * Listeners (IOperationHistoryListener) can listen for notifications about
 * changes in the history (operations added or removed), and for notification
 * before and after any operation is executed, undone or redone. Notification of
 * operation execution only occurs when clients direct the history to execute
 * the operation. If the operation is added after it is executed, there can be
 * no notification of its execution.
 * </p>
 * <p>
 * IOperationApprover defines an interface for approving an undo or redo before
 * it occurs. This is useful for injecting policy-decisions into the model -
 * whether direct undo and redo are supported, or warning the user about certain
 * kinds of operations. It can also be used when objects have state related to
 * the operation and need to determine whether an undo or redo will cause any
 * conflicts with their local state.
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
public interface IOperationHistory {

	/**
	 * Add the specified operation to the history without executing it. The
	 * operation should have already been executed by the time it is added to
	 * the history. Listeners will be notified that the operation was added to
	 * the history.
	 * 
	 * @param operation -
	 *            the operation to be added to the history
	 */
	void add(IOperation operation);

	/**
	 * Add the specified approver to the operation history.
	 * 
	 * @param approver -
	 *            the IOperationApprover that will be consulted before any
	 *            operation in the history is undone or redone
	 */
	void addOperationApprover(IOperationApprover approver);

	/**
	 * Add the specified listener to the operation history.
	 * 
	 * @param listener -
	 *            the IOperationHistoryListener to receive notifications about
	 *            changes in the history or operations that are executed,
	 *            undone, or redone
	 */
	void addOperationHistoryListener(IOperationHistoryListener listener);

	/**
	 * Return whether there is a redoable operation available in the given
	 * context.
	 * 
	 * @param context -
	 *            the context to be checked, or null for any context
	 * @return <code>true</code> if there is a redoable operation,
	 *         <code>false</code> otherwise.
	 */
	boolean canRedo(OperationContext context);

	/**
	 * Return whether there is an undoable operation available in the given
	 * context
	 * 
	 * @param context -
	 *            the context to be checked, or null to represent any context
	 * @return <code>true</code> if there is an undoable operation,
	 *         <code>false</code> otherwise.
	 */
	boolean canUndo(OperationContext context);

	/**
	 * Dispose of the specified context in the history. All operations that have
	 * only the given context will be disposed. References to the context in
	 * operations that have more than one context will also be removed.
	 * 
	 * @param context -
	 *            the context to be disposed, or null if all contexts are to be
	 *            disposed
	 * @param flushUndo -
	 *            <code>true</code> if the context should be flushed from the
	 *            undo history, <code>false</code> if it should not
	 * @param flushRedo -
	 *            <code>true</code> if the context should be flushed from the
	 *            redo history, <code>false</code> if it should not.
	 * 
	 */
	void dispose(OperationContext context, boolean flushUndo, boolean flushRedo);

	/**
	 * Execute the specified operation and add it to the operations history if
	 * successful. This method is used by clients who wish operation history
	 * listeners to receive notifications before and after the execution of the
	 * operation. Listeners will be notified before (
	 * <code>aboutToExecute</code>) and after (<code>done</code> or
	 * <code>operationNotOK</code>).
	 * 
	 * If the operation successfully executes, an additional notification that
	 * the operation has been added to the history (<code>operationAdded</code>)
	 * will be sent.
	 * 
	 * @param operation
	 *            the operation to be executed and then added to the history
	 * 
	 * @return the IStatus indicating whether the execution succeeded.
	 * 
	 * The severity code in the returned status describes whether the operation
	 * succeeded and whether it was added to the history. <code>OK</code>
	 * severity indicates that the execute operation was successful and that the
	 * operation has been added to the history. Listeners will receive the
	 * <code>done</code> notification.
	 * 
	 * <code>CANCEL</code> severity indicates that the user cancelled the
	 * operation and that the operation was not added to the history.
	 * <code>ERROR</code> severity indicates that the operation did not
	 * successfully execute and that it was not added to the history. Any other
	 * severity code is not specifically interpreted by the history, and the
	 * operation will not be added to the history. For all severities other than
	 * <code>OK</code>, listeners will receive the
	 * <code>operationNotOK</code> notification instead of the
	 * <code>done</code> notification.
	 * 
	 */
	IStatus execute(IOperation operation, IProgressMonitor monitor);

	/**
	 * Return the specified limit on the undo and redo history.
	 * 
	 * @return limit
	 */
	int getLimit();

	/**
	 * Get the array of operations in the redo history for a given context. The
	 * operations are in the order that they would be redone if successive
	 * "Redo" commands were invoked.
	 * 
	 * @param context -
	 *            the context for the redo, or null if the entire history is
	 *            requested
	 * @return the array of operations in the history
	 */
	IOperation[] getRedoHistory(OperationContext context);

	/**
	 * Get the operation that will next be redone in the given context. This
	 * method is used to retrieve the label or description as needed for the
	 * "Redo" menu.
	 * 
	 * @param context -
	 *            the context for the redo, or null if the context does not
	 *            matter
	 * @return the operation to be redone or <code>null</code> if there is no
	 *         operation available. There is no guarantee that the returned
	 *         operation is valid for redo.
	 */
	IOperation getRedoOperation(OperationContext context);

	/**
	 * Get the array of operations that can be undone in the specified context.
	 * The operations are in the order that they would be undone if successive
	 * "Undo" commands were invoked.
	 * 
	 * @param context -
	 *            the context for the undo, or null if the entire history is
	 *            requested
	 * @return the array of operations in the history
	 */
	IOperation[] getUndoHistory(OperationContext context);

	/**
	 * Get the operation that will next be undone in the given context. This
	 * method is used to retrieve the label or description as needed for the
	 * "Undo" menu.
	 * 
	 * @param context -
	 *            the context for the undo, or null if the context does not
	 *            matter
	 * @return the operation to be undone or <code>null</code> if there is no
	 *         operation available. There is no guarantee that the available
	 *         operation is valid for the undo.
	 */
	IOperation getUndoOperation(OperationContext context);

	/**
	 * Redo the most recently undone operation in the given context
	 * 
	 * @param context -
	 *            the context to be redone, or null if the context does not
	 *            matter
	 * @param monitor -
	 *            the progress monitor to be used for the redo, or null if no
	 *            progress monitor is provided.
	 * 
	 * @return the IStatus indicating whether the redo succeeded.
	 * 
	 * The severity code in the returned status describes whether the operation
	 * succeeded and whether it remains in the history. <code>OK</code>
	 * severity indicates that the redo operation was successful and that the
	 * operation has been placed on the undo history. Listeners will receive the
	 * <code>redone</code> notification.
	 * 
	 * <code>CANCEL</code> severity indicates that the user cancelled the
	 * operation and that the operation remains in the redo history.
	 * <code>ERROR</code> severity indicates that the operation could not
	 * successfully be redone and that it has been removed from the history.
	 * Listeners will also be notified that the operation was removed. Any other
	 * severity code is not specifically interpreted by the history, and is
	 * simply passed back to the caller. For all severities other than
	 * <code>OK</code>, listeners will receive the
	 * <code>operationNotOK</code> notification instead of the
	 * <code>redone</code> notification.
	 * 
	 */
	IStatus redo(OperationContext context, IProgressMonitor monitor);

	/**
	 * Redo the specified operation
	 * 
	 * @param operation -
	 *            the operation to be redone
	 * @param monitor -
	 *            the progress monitor to be used for the redo, or null if no
	 *            progress monitor is provided
	 * @return the IStatus indicating whether the redo succeeded.
	 * 
	 * The severity code in the returned status describes whether the operation
	 * succeeded and whether it remains in the history. <code>OK</code>
	 * severity indicates that the redo operation was successful and that the
	 * operation has been placed on the undo history. Listeners will receive the
	 * <code>redone</code> notification.
	 * 
	 * <code>CANCEL</code> severity indicates that the user cancelled the
	 * operation and that the operation remains in the redo history.
	 * <code>ERROR</code> severity indicates that the operation could not
	 * successfully be redone. The operation will remain at its current location
	 * in the history, and callers must explicitly remove it if desired. Any
	 * other severity code is not interpreted by the history, and is simply
	 * passed back to the caller. For all severities other than <code>OK</code>,
	 * listeners will receive the <code>operationNotOK</code> notification
	 * instead of the <code>redone</code> notification.
	 */
	IStatus redoOperation(IOperation operation, IProgressMonitor monitor);

	/**
	 * Remove the specified operation from the history. Listeners will be
	 * notified of the removal of the operation. This method is used by clients
	 * who want to flush a particular subset of the history.
	 * 
	 * @param operation -
	 *            the operation to be removed from the history
	 */
	void remove(IOperation operation);

	/**
	 * Remove the specified operation approver from the operation history.
	 * 
	 * @param approver -
	 *            the IOperationApprover to be removed
	 */
	void removeOperationApprover(IOperationApprover approver);

	/**
	 * Remove the specified listener from the operation history.
	 * 
	 * @param listener -
	 *            The IOperationHistoryListener to be removed
	 */
	void removeOperationHistoryListener(IOperationHistoryListener listener);

	/**
	 * Set the limit on the undo and redo history.
	 * 
	 * @param limit -
	 *            the maximum number of operations that should be kept in the
	 *            history
	 */
	void setLimit(int limit);

	/**
	 * Undo the most recently undone operation in the given context
	 * 
	 * @param context -
	 *            the context to be undone, or null if the context does not
	 *            matter
	 * @param monitor -
	 *            the progress monitor to be used for the undo, or null if no
	 *            progress monitor is provided.
	 * 
	 * @return the IStatus indicating whether the undo succeeded.
	 * 
	 * The severity code in the returned status describes whether the operation
	 * succeeded and whether it remains in the history. <code>OK</code>
	 * severity indicates that the undo operation was successful and that the
	 * operation has been placed on the redo history. Listeners will receive the
	 * <code>undone</code> notification.
	 * 
	 * <code>CANCEL</code> severity indicates that the user cancelled the
	 * operation and that the operation remains in the undo history.
	 * <code>ERROR</code> severity indicates that the operation could not
	 * successfully be undone and that it has been removed from the history.
	 * Listeners will be notified that the operation was removed. Any other
	 * severity code is not interpreted by the history, and is simply passed
	 * back to the caller. For all severities other than <code>OK</code>,
	 * listeners will receive the <code>operationNotOK</code> notification
	 * instead of the <code>undone</code> notification.
	 */

	IStatus undo(OperationContext context, IProgressMonitor monitor);

	/**
	 * Undo the specified operation
	 * 
	 * @param operation -
	 *            the operation to be undone
	 * @param monitor -
	 *            the progress monitor to be used for the undo, or null if no
	 *            progress monitor is provided
	 * @return the IStatus indicating whether the undo succeeded.
	 * 
	 * The severity code in the returned status describes whether the operation
	 * succeeded and whether it remains in the history. <code>OK</code>
	 * severity indicates that the undo operation was successful and that the
	 * operation has been placed on the redo history. Listeners will receive the
	 * <code>undone</code> notification.
	 * 
	 * <code>CANCEL</code> severity indicates that the user cancelled the
	 * operation and that the operation remains in the undo history.
	 * <code>ERROR</code> severity indicates that the operation could not
	 * successfully be undone. The operation will remain at its current location
	 * in the history, and callers must explicitly remove it if desired. Any
	 * other severity code is not interpreted by the history, and is simply
	 * passed back to the caller. For all severities other than <code>OK</code>,
	 * listeners will receive the <code>operationNotOK</code> notification
	 * instead of the <code>undone</code> notification.
	 */
	IStatus undoOperation(IOperation operation, IProgressMonitor monitor);

}
