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

import org.eclipse.core.commands.operations.UndoContext;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * <p>
 * RedoActionHandler provides common behavior for labeling and enabling the menu
 * item for redo.
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
public class RedoActionHandler extends OperationHistoryActionHandler {

	/**
	 * Construct an action handler that handles the labelling and enabling of
	 * the redo action for the specified operation context.
	 * 
	 * @param context -
	 *            the UndoContext to be used to filter the operations
	 *            history.
	 */
	public RedoActionHandler(UndoContext context) {
		super(context);
		setId("OperationHistoryRedoHandler");//$NON-NLS-1$
	}

	protected void flush() {
		getHistory().dispose(fContext, false, true);
	}

	protected String getCommandString() {
		return WorkbenchMessages.getString("Workbench.redo"); //$NON-NLS-1$
	}

	protected String getOperationLabel() {
		return getHistory().getRedoOperation(fContext).getLabel();

	}

	public void run() {
		getHistory().redo(fContext, null);
	}

	protected boolean shouldBeEnabled() {
		return getHistory().canRedo(fContext);
	}
}
