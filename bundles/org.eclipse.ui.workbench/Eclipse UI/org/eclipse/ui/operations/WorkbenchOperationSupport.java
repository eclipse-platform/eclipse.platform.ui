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

package org.eclipse.ui.operations;

import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.ContextConsultingOperationApprover;
import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.LinearUndoEnforcer;
import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.ui.internal.WorkbenchUndoContext;

/**
 * <p>
 * Provides operation support for the workbench.
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
public class WorkbenchOperationSupport implements IWorkbenchOperationSupport {

	private WorkbenchUndoContext undoContext;
	private IOperationApprover approver;

	/**
	 * Disposes of anything created by the operation support.
	 */
	public void dispose() {
		/*
		 * uninstall the operation approver that we added to the operation history
		 */ 
		getOperationHistory().removeOperationApprover(approver);
		/*
		 * dispose of all operations using our context
		 */
		getOperationHistory().dispose(getUndoContext(), true, true);
	}

	/**
	 * Returns the undo context for workbench operations.
	 * 
	 * @return the workbench operation context.
	 * @since 3.1
	 */
	public UndoContext getUndoContext() {
		if (undoContext == null) {
			undoContext = new WorkbenchUndoContext(
					"Workbench Context"); //$NON-NLS-1$
			undoContext.setOperationApprover(new LinearUndoEnforcer());
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
		IOperationHistory history = OperationHistoryFactory.getOperationHistory();
		/*
		 * Set up the history if we have not done so before.
		 */
		if (approver == null) {
			/*
			 * install an operation approver that consults an operation's
			 * context prior to performing an operation
			 */
			approver = new ContextConsultingOperationApprover();
			history.addOperationApprover(new ContextConsultingOperationApprover());
			/*
			 * set a limit for the workbench undo context
			 */
			history.setLimit(getUndoContext(), 25);
		}
		return history;
	}

}
