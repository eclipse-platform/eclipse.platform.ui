/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
 * This class is used to maintain the instance of the operation history that
 * should be used by classes that create UndoableOperations. 
 * 
 * It is intended that an application can create an operation history appropriate
 * for its needs and set it into this class.  Otherwise, a default operation history
 * will be created.  All classes that access an operations history use this class to
 * obtain the correct instance.
 * 
 * @since 3.1
 */
public class OperationHistoryFactory {

	private static IOperationHistory fOperationHistory;

	/**
	 * Return the operation history to be used for managing undoable operations.
	 * 
	 * @return the operation history to be used for executing, undoing, and
	 *         redoing operations.
	 */
	public static IOperationHistory getOperationHistory() {
		if (fOperationHistory == null) {
			fOperationHistory = new DefaultOperationHistory();
		}
		return fOperationHistory;
	}

	/**
	 * Set the operation history to be used for managing undoable operations.
	 * 
	 * @param history -
	 *            the operation history to be used for executing, undoing, and
	 *            redoing operations.
	 */
	public static final void setOperationHistory(IOperationHistory history) {
		fOperationHistory = history;
	}

}
