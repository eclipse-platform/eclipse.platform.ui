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

package org.eclipse.ui.operations;

import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.misc.Policy;

/**
 * <p>
 * Provides undoable operation support for the workbench. This includes
 * providing access to the default operation history and creating a
 * workbench-wide undo context.
 * </p>
 * 
 * @since 3.1
 */
public class WorkbenchOperationSupport implements IWorkbenchOperationSupport {

	private ObjectUndoContext undoContext;

	private IOperationHistory history;

	// initialize debug options
	static {
		DefaultOperationHistory.DEBUG_OPERATION_HISTORY_UNEXPECTED = Policy.DEBUG_OPERATIONS;
		DefaultOperationHistory.DEBUG_OPERATION_HISTORY_OPENOPERATION = Policy.DEBUG_OPERATIONS;
		DefaultOperationHistory.DEBUG_OPERATION_HISTORY_APPROVAL = Policy.DEBUG_OPERATIONS;
		DefaultOperationHistory.DEBUG_OPERATION_HISTORY_NOTIFICATION = Policy.DEBUG_OPERATIONS
				&& Policy.DEBUG_OPERATIONS_VERBOSE;
		DefaultOperationHistory.DEBUG_OPERATION_HISTORY_DISPOSE = Policy.DEBUG_OPERATIONS
				&& Policy.DEBUG_OPERATIONS_VERBOSE;
	}

	/**
	 * Disposes of anything created by the operation support.
	 */
	public void dispose() {
		/*
		 * dispose of all operations using our context
		 */
		getOperationHistory().dispose(getUndoContext(), true, true, true);
	}

	/**
	 * Returns the undo context for workbench operations. The workbench
	 * configures an undo context with the appropriate policies for the
	 * workbench undo model.
	 * 
	 * @return the workbench operation context.
	 * @since 3.1
	 */
	public IUndoContext getUndoContext() {
		if (undoContext == null) {
			undoContext = new ObjectUndoContext(PlatformUI.getWorkbench(),
					"Workbench Context"); //$NON-NLS-1$
		}
		return undoContext;
	}

	/**
	 * Returns the workbench operation history.
	 * 
	 * @return the operation history for workbench operations.
	 * @since 3.1
	 */
	public IOperationHistory getOperationHistory() {
		if (history == null) {
			history = OperationHistoryFactory.getOperationHistory();
			/*
			 * set a limit for the workbench undo context
			 */
			history.setLimit(getUndoContext(), 25);
		}
		return history;
	}

}
