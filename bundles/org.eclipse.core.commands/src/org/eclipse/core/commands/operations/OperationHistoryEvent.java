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

import org.eclipse.core.runtime.IStatus;

/**
 * <p>
 * OperationHistoryEvent is used to communicate changes that occur in a
 * DefaultOperationHistory, including the addition or removal of operations, and
 * the execution, undo, and redo of operations.
 * </p>
 * <p>
 * Operation history listeners must be prepared to receive notifications from a
 * background thread. Any UI access occurring inside the implementation must be
 * properly synchronized using the techniques specified by the client's widget
 * library.
 * </p>
 *
 *
 * @since 3.1
 */
public final class OperationHistoryEvent {

	/**
	 * ABOUT_TO_EXECUTE indicates that an operation is about to execute.
	 * Listeners should prepare for the execution as appropriate. Listeners will
	 * receive a DONE notification if the operation is successful, or an
	 * OPERATION_NOT_OK notification if the execution is cancelled or otherwise
	 * fails. This notification is only received for those operations executed
	 * by the operation history. Operations that are added to the history after
	 * execution do not trigger these notifications.
	 *
	 * If the operation successfully executes, clients will also receive a
	 * notification that it has been added to the history.
	 *
	 * (value is 1).
	 */

	public static final int ABOUT_TO_EXECUTE = 1;

	/**
	 * ABOUT_TO_REDO indicates that an operation is about to be redone.
	 * Listeners should prepare for the redo as appropriate. Listeners will
	 * receive a REDONE notification if the operation is successful, or an
	 * OPERATION_NOT_OK notification if the redo is cancelled or otherwise
	 * fails.
	 *
	 * (value is 2).
	 */
	public static final int ABOUT_TO_REDO = 2;

	/**
	 * ABOUT_TO_UNDO indicates that an operation is about to be undone.
	 * Listeners should prepare for the undo as appropriate. Listeners will
	 * receive an UNDONE notification if the operation is successful, or an
	 * OPERATION_NOT_OK notification if the undo is cancelled or otherwise
	 * fails.
	 *
	 * (value is 3).
	 */
	public static final int ABOUT_TO_UNDO = 3;

	/**
	 * DONE indicates that an operation has been executed. Listeners can take
	 * appropriate action, such as revealing any relevant state in the UI. This
	 * notification is only received for those operations executed by the
	 * operation history. Operations that are added to the history after
	 * execution do not trigger this notification.
	 *
	 * Clients will also receive a notification that the operation has been
	 * added to the history.
	 *
	 * (value is 4).
	 */
	public static final int DONE = 4;

	/**
	 * OPERATION_ADDED indicates that an operation was added to the history.
	 * Listeners can use this notification to add their undo context to a new
	 * operation as appropriate or otherwise record the operation.
	 *
	 * (value is 5).
	 */
	public static final int OPERATION_ADDED = 5;

	/**
	 * OPERATION_CHANGED indicates that an operation has changed in some way
	 * since it was added to the operations history.
	 *
	 * (value is 6).
	 */
	public static final int OPERATION_CHANGED = 6;

	/**
	 * OPERATION_NOT_OK indicates that an operation was attempted and not
	 * successful. Listeners typically use this when they have prepared for an
	 * execute, undo, or redo, and need to know that the operation did not
	 * successfully complete. For example, listeners that turn redraw off before
	 * an operation is undone would turn redraw on when the operation completes,
	 * or when this notification is received, since there will be no
	 * notification of the completion.
	 *
	 * (value is 7).
	 */
	public static final int OPERATION_NOT_OK = 7;

	/**
	 * OPERATION_REMOVED indicates an operation was removed from the history.
	 * Listeners typically remove any record of the operation that they may have
	 * kept in their own state. The operation has been disposed by the time
	 * listeners receive this notification.
	 *
	 * (value is 8).
	 */
	public static final int OPERATION_REMOVED = 8;

	/**
	 * REDONE indicates that an operation was redone. Listeners can take
	 * appropriate action, such as revealing any relevant state in the UI.
	 *
	 * (value is 9).
	 */
	public static final int REDONE = 9;

	/**
	 * UNDONE indicates that an operation was undone. Listeners can take
	 * appropriate action, such as revealing any relevant state in the UI.
	 *
	 * (value is 10).
	 */
	public static final int UNDONE = 10;

	private int code = 0;

	private IOperationHistory history;

	private IUndoableOperation operation;

	/* @since 3.2 */
	private IStatus status;

	/**
	 * Construct an event for the specified operation history.
	 *
	 * @param code
	 *            the event code to be used.
	 * @param history
	 *            the history triggering the event.
	 * @param operation
	 *            the operation involved in the event.
	 */
	public OperationHistoryEvent(int code, IOperationHistory history,
			IUndoableOperation operation) {
		this(code, history, operation, null);
	}

	/**
	 * Construct an event for the specified operation history.
	 *
	 * @param code
	 *            the event code to be used.
	 * @param history
	 *            the history triggering the event.
	 * @param operation
	 *            the operation involved in the event.
	 * @param status
	 *            the status associated with the event, or null if no status is
	 *            available.
	 *
	 * @since 3.2
	 */
	public OperationHistoryEvent(int code, IOperationHistory history,
			IUndoableOperation operation, IStatus status) {
		if (history == null) {
			throw new NullPointerException();
		}
		if (operation == null) {
			throw new NullPointerException();
		}
		this.code = code;
		this.history = history;
		this.operation = operation;
		this.status = status;
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

	public IUndoableOperation getOperation() {
		return operation;
	}

	/**
	 * Return the status associated with this event.
	 *
	 * @return the status associated with this event. The status may be null.
	 *
	 * @since 3.2
	 */

	public IStatus getStatus() {
		return status;
	}

}
