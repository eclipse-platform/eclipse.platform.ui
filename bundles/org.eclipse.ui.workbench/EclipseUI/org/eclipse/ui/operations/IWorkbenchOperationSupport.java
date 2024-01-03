/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.operations;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;

/**
 * An instance of this interface provides support for managing a a shared
 * operations history and an shared undo context at the <code>IWorkbench</code>
 * level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 *
 * @since 3.1
 *
 * @see org.eclipse.ui.IWorkbench#getOperationSupport()
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchOperationSupport {

	/**
	 * Returns the undo context for workbench-wide operations.
	 *
	 * @return the workbench operation context
	 */
	IUndoContext getUndoContext();

	/**
	 * Returns the operation history for the workbench.
	 *
	 * @return the workbench operation history
	 */
	IOperationHistory getOperationHistory();

}
