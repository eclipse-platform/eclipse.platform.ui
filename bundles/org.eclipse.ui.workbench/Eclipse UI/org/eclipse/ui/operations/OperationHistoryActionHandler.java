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
import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * <p>
 * OperationHistoryActionHandler provides common behavior for the undo and redo
 * action handlers. It supports filtering of undo or redo on a particular
 * context. A null context means that there is no filter on the redo history.
 * </p>
 * <p>
 * OperationHistoryActionHandler assumes a linear undo/redo model. When the
 * handler is run, the operation history is asked to perform the most recent
 * undo for the handler's context. Further, the handler proactively flushes the
 * operation undo or redo history when there is no valid operation for a given
 * context, to avoid keeping a stale history of invalid operations.
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
public abstract class OperationHistoryActionHandler extends Action implements
		ActionFactory.IWorkbenchAction {

	protected OperationContext fContext = null;

	protected OperationHistoryActionHandler(OperationContext context) {
		fContext = context;
	}

	public void dispose() {
		// nothing to dispose
	}

	protected abstract void flush();

	protected abstract String getCommandString();

	protected IOperationHistory getHistory() {
		return PlatformUI.getWorkbench().getOperationSupport()
				.getOperationHistory();
	}

	protected abstract String getOperationLabel();

	public abstract void run();

	/**
	 * Set the operation context that should be used to retrieve the undo and
	 * redo history. A null value is interpreted as showing all contexts.
	 * 
	 * @param context
	 */
	public void setContext(OperationContext context) {
		fContext = context;
		update();
	}

	protected abstract boolean shouldBeEnabled();

	/**
	 * Update enabling and labels according to the current status of the
	 * history.
	 */
	public void update() {
		boolean enabled = shouldBeEnabled();
		StringBuffer text = new StringBuffer(getCommandString());
		if (enabled) {
			text.append(" "); //$NON-NLS-1$
			text.append(getOperationLabel());
		} else {
			/*
			 * if there is nothing to do, ensure the history is flushed of this
			 * context
			 */
			flush();
		}
		setText(text.toString());
		setEnabled(enabled);
	}
}
