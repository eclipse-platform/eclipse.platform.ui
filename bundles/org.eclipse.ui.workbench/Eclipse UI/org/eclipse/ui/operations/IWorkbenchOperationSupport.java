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

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.OperationContext;

/**
 * An instance of this interface provides support for managing operations and a
 * shared operations history at the <code>IWorkbench</code> level.
 * <p>
 * This interface is not intended to be extended or implemented by clients.
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
public interface IWorkbenchOperationSupport {

	/**
	 * Dispose of any state kept by the operations support, including the
	 * history and contained operations.
	 */
	public void dispose();

	/**
	 * Returns the operation context for workbench-wide operations.
	 * 
	 * @return the workbench operation context
	 */
	public OperationContext getOperationContext();

	/**
	 * Returns the operation history for the workbench.
	 * 
	 * @return the workbench operation history
	 */
	public IOperationHistory getOperationHistory();

}
