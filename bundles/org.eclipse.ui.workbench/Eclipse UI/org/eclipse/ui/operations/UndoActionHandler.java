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

import org.eclipse.core.commands.operations.OperationContext;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * <p>
 * UndoActionHandler provides common behavior for labeling and enabling the menu
 * item for undo.
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
public class UndoActionHandler extends OperationHistoryActionHandler {

	public UndoActionHandler(OperationContext context) {
		super(context);
		setId("OperationHistoryUndoHandler");//$NON-NLS-1$
	}

	protected void flush() {
		getHistory().dispose(fContext, true, false);
	}

	protected String getCommandString() {
		return WorkbenchMessages.getString("Workbench.undo"); //$NON-NLS-1$
	}

	protected String getOperationLabel() {
		return getHistory().getUndoOperation(fContext).getLabel();

	}

	public void run() {
		getHistory().undo(fContext, null);
	}

	protected boolean shouldBeEnabled() {
		return getHistory().canUndo(fContext);
	}
}
