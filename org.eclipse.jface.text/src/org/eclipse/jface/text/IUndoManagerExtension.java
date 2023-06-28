/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.jface.text;

import org.eclipse.core.commands.operations.IUndoContext;


/**
 * Extension interface for {@link org.eclipse.jface.text.IUndoManager}.
 * Introduces access to the undo context.
 *
 * @see org.eclipse.jface.text.IUndoManager
 * @since 3.1
 */
public interface IUndoManagerExtension {

	/**
	 * Returns this undo manager's undo context.
	 *
	 * @return the undo context or <code>null</code> if the undo manager is not connected
	 * @see org.eclipse.core.commands.operations.IUndoContext
	 */
	IUndoContext getUndoContext();

}
