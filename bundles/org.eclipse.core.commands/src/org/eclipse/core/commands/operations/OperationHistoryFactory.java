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
 * This class is used to maintain the instance of the operation history that
 * should be used by classes that access the undo or redo history and add
 * undoable operations to the history.
 *
 * <p>
 * It is intended that an application can create an operation history appropriate
 * for its needs and set it into this class.  Otherwise, a default operation history
 * will be created.  The operation history may only be set one time.  All classes that
 * access an operations history use this class to obtain the correct instance.
 *
 * @since 3.1
 */
public final class OperationHistoryFactory {

	private static IOperationHistory operationHistory;

	/**
	 * Return the operation history to be used for managing undoable operations.
	 *
	 * @return the operation history to be used for executing, undoing, and
	 *         redoing operations.
	 */
	public static IOperationHistory getOperationHistory() {
		if (operationHistory == null) {
			operationHistory = new DefaultOperationHistory();
		}
		return operationHistory;
	}

	/**
	 * Set the operation history to be used for managing undoable operations.
	 * This method may only be called one time, and must be called before any
	 * request to get the history.  Attempts to set the operation history will
	 * be ignored after it has been already set, or after a default one has
	 * been created.
	 *
	 * @param history
	 *            the operation history to be used for executing, undoing, and
	 *            redoing operations.
	 */
	public static void setOperationHistory(IOperationHistory history) {
		// If one has already been set or created, ignore this request.
		if (operationHistory == null) {
			operationHistory = history;
		}
	}

	private OperationHistoryFactory() {
		// may not be instantiated
	}

}
