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
 * OperationHistoryEvent is used to communicate changes to an OperationHistory,
 * including operations added and removed from the history, and the execution,
 * undo, and redo of operations.
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
public final class OperationHistoryEvent {

	/**
	 * ABOUT_TO_EXECUTE indicates that an operation is about to execute.
	 * Listeners should prepare for the execution as appropriate. Listeners will
	 * receive a done notification if the operation is successful, or an
	 * operationNotCompleted notification if the execution is cancelled or
	 * otherwise fails. This notification is only received for those operations
	 * executed by the operation history. Operations that are added to the
	 * history after execution do not trigger these notifications.
	 * 
	 * If the operation successfully executes, clients will also receive a
	 * notification that it has been added to the history.
	 */

	public static final int ABOUT_TO_EXECUTE = 0x0004;

	/**
	 * ABOUT_TO_REDO indicates that an operation is about to be redone.
	 * Listeners should prepare for the redo as appropriate. Listeners will
	 * receive a redone notification if the operation is successful, or an
	 * operationNotCompleted notification if the redo is cancelled or otherwise
	 * fails.
	 */
	public static final int ABOUT_TO_REDO = 0x0010;

	/**
	 * ABOUT_TO_UNDO indicates that an operation is about to be undone.
	 * Listeners should prepare for the undo as appropriate. Listeners will
	 * receive an undone notification if the operation is successful, or an
	 * operationNotCompleted notification if the undo is cancelled or otherwise
	 * fails.
	 */
	public static final int ABOUT_TO_UNDO = 0x0008;

	/**
	 * DONE indicates that operation has been executed. Listeners can take
	 * appropriate action, such as revealing any relevant state in the UI. This
	 * notification is only received for those operations executed by the
	 * operation history. Operations that are added to the history after
	 * execution do not trigger this notification.
	 * 
	 * Clients will also receive a notification that the operation has been
	 * added to the history.
	 */
	public static final int DONE = 0x0020;

	// constants are bit masked in case there are overlapping events in the
	// future

	/**
	 * OPERATION_ADDED indicates an operation was added to the history.
	 * Listeners typically use this to add their context to a new operation as
	 * appropriate or otherwise record the operation.
	 */
	public static final int OPERATION_ADDED = 0x0001;

	/**
	 * OPERATION_NOT_OK indicates that an operation was attempted and not
	 * successful. Listeners typically use this when they have prepared for an
	 * execute, undo, or redo, and need to know that the operation did not
	 * successfully complete. For example, listeners that turn redraw off before
	 * an operation is undone would turn redraw on when the operation completes,
	 * or when this notification is received, since there will be no
	 * notification of the completion.
	 */
	public static final int OPERATION_NOT_OK = 0x0100;

	/**
	 * OPERATION_REMOVED indicates an operation was removed from the history.
	 * Listeners typically remove any record of the operation that they may have
	 * kept in their own state.
	 */
	public static final int OPERATION_REMOVED = 0x0002;

	/**
	 * REDONE indicates that an operation was redone. Listeners can take
	 * appropriate action, such as revealing any relevant state in the UI.
	 */
	public static final int REDONE = 0x0080;

	/**
	 * UNDONE indicates that an operation was undone. Listeners can take
	 * appropriate action, such as revealing any relevant state in the UI.
	 */
	public static final int UNDONE = 0x0040;

	private int code = 0;

	private IOperationHistory history;

	private IOperation operation;

	public OperationHistoryEvent(int code, IOperationHistory history,
			IOperation operation) {
		if (history == null)
			throw new NullPointerException();
		if (operation == null)
			throw new NullPointerException();
		this.code = code;
		this.history = history;
		this.operation = operation;
	}

	/**
	 * Return the type of event that is occurring.
	 * 
	 * @return the type code indicating the type of event.
	 */
	public int getEventType() {
		return code;
	}

	/**
	 * Return the operation history that triggered this event.
	 * 
	 * @return the operation history
	 */

	public IOperationHistory getHistory() {
		return history;
	}

	/**
	 * Return the operation associated with this event.
	 * 
	 * @return the operation
	 */

	public IOperation getOperation() {
		return operation;
	}

}
