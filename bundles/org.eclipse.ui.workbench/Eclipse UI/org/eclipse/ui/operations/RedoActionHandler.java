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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * <p>
 * RedoActionHandler provides common behavior for redoing an operation, as well
 * as labeling and enabling the menu item.
 * </p>
 * 
 * @since 3.1
 */
public class RedoActionHandler extends OperationHistoryActionHandler {

	/**
	 * Construct an action handler that handles the labelling and enabling of
	 * the redo action for a specified operation context.
	 * 
	 * @param site -
	 *            the workbench part site that created the action.
	 * @param context -
	 *            the context to be used for redoing.
	 */
	public RedoActionHandler(IWorkbenchPartSite site, IUndoContext context) {
		super(site, context);
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));

	}

	protected void flush() {
		getHistory().dispose(undoContext, false, true, false);
	}

	protected String getCommandString() {
		return WorkbenchMessages.Workbench_redo;
	}

	protected IUndoableOperation getOperation() {
		return getHistory().getRedoOperation(undoContext);
	}

	IStatus runCommand() throws ExecutionException {
		return getHistory().redo(undoContext, getProgressMonitor(), this);
	}

	protected boolean shouldBeEnabled() {
		// make sure a context is set. If a part doesn't provide
		// a context, then we should not enable.
		if (undoContext == null)
			return false;
		return getHistory().canRedo(undoContext);
	}
}
