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

import org.eclipse.core.commands.operations.ContextConsultingOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.LinearUndoEnforcer;
import org.eclipse.core.commands.operations.OperationContext;
import org.eclipse.core.commands.operations.internal.OperationHistory;
import org.eclipse.ui.internal.WorkbenchOperationContext;

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

	private WorkbenchOperationContext operationContext;

	private IOperationHistory operationHistory;

	/**
	 * Disposes of the history.
	 */
	public void dispose() {
		if (operationHistory != null) {
			operationHistory.dispose(null, true, true);
		}
		operationHistory = null;
	}

	/**
	 * Returns the operation context for workbench operations.
	 * 
	 * @return the workbench operation context.
	 * @since 3.1
	 */
	public OperationContext getOperationContext() {
		if (operationContext == null) {
			operationContext = new WorkbenchOperationContext(
					"Workbench Context"); //$NON-NLS-1$
			operationContext.setOperationApprover(new LinearUndoEnforcer());
		}
		return operationContext;
	}

	/**
	 * Returns the workbench operation history.
	 * 
	 * @return the operation history for workbench operations.
	 * @since 3.1
	 */
	public IOperationHistory getOperationHistory() {
		if (operationHistory == null) {
			// create the operation history
			operationHistory = new OperationHistory();
			/*
			 * install an operation approver that consults an operation's
			 * context prior to performing an operation
			 */
			operationHistory
					.addOperationApprover(new ContextConsultingOperationApprover());
			// set a limit on the history
			operationHistory.setLimit(25);

		}
		return operationHistory;
	}

}
