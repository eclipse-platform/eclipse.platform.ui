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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * <p>
 * RedoActionHandler provides common behavior for redoing an operation,
 * as well as labeling and enabling the menu item.
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
	 * the redo action for a specified operation context.
	 * 
	 * @param window -
	 *            the workbench window that created the action.
	 * @param context -
	 *            the context to be used for redoing.
	 */
	public RedoActionHandler(IWorkbenchWindow window, IUndoContext context) {
		super(window, context);
	    setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_REDO));  

	}

	protected void flush() {
		getHistory().dispose(undoContext, false, true);
	}

	protected String getCommandString() {
		return WorkbenchMessages.Workbench_redo;
	}

	protected IUndoableOperation getOperation() {
		return getHistory().getRedoOperation(undoContext);
	}

	public void run() {
		try {
			getHistory().redo(undoContext, null, this);
		}
		catch (ExecutionException e) {
			reportException(e);
		}
	}

	protected boolean shouldBeEnabled() {
		// make sure a context is set. If a part doesn't return
		// a context, then we should not enable.
		if (undoContext == null)
			return false;
		return getHistory().canRedo(undoContext);
	}
}
