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
 * IHistoryNotificationAwareOperation defines an interface for an undoable
 * operation to receive notifications from its containing operation history
 * before the history sends any notifications involving the receiver to its
 * listeners.
 * </p>
 * <p>
 * This interface is intended to be used by legacy frameworks that are adapting
 * their old undo and redo support to this framework. Operations that previously
 * relied on notification from their containing history or stack before its
 * listeners are notified about changes to the operation should implement this
 * interface. Cooperating operation history implementations should check for the
 * implementation of this interface and send notifications when it is present.
 * </p>
 * 
 * @since 3.1
 * 
 */
public interface IHistoryNotificationAwareOperation {

	/**
	 * <p>
	 * An operation history notification about this operation is about to be
	 * sent to operation history listeners. Any preparation needed before
	 * listeners are notified about this operation should be performed here.
	 * 
	 * <p>
	 * This method has been added to support legacy undo frameworks that are
	 * adapting to IUndoableOperation. Operations that previously relied on
	 * notification from their containing history or stack before any listeners
	 * are notified about changes to the operation should implement this
	 * interface.
	 * 
	 * @param event -
	 *            the event that is about to be sent with the pending
	 *            notification
	 * 
	 */
	void aboutToNotify(OperationHistoryEvent event);

}
