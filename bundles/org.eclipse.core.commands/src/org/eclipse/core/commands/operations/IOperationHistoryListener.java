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

/**
 * <p>
 * This interface is used to listen to notifications from an IOperationHistory.
 * The supplied OperationHistoryEvent describes the particular notification.
 * </p>
 * <p>
 * Operation history listeners must be prepared to receive notifications from a
 * background thread. Any UI access occurring inside the implementation must be
 * properly synchronized using the techniques specified by the client's widget
 * library.
 * </p>
 *
 * @since 3.1
 */
public interface IOperationHistoryListener {
	/**
	 * Something of note has happened in the IOperationHistory. Listeners should
	 * check the supplied event for details.
	 *
	 * @param event
	 *            the OperationHistoryEvent that describes the particular
	 *            notification.
	 */
	void historyNotification(OperationHistoryEvent event);

}
