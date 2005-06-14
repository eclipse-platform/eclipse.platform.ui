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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * <p>
 * RedoActionHandler provides common behavior for redoing an operation, as well
 * as labelling and enabling the menu item.  This class may be instantiated by 
 * clients.
 * </p>
 * 
 * @since 3.1
 */
public final class RedoActionHandler extends OperationHistoryActionHandler {

	/**
	 * Construct an action handler that handles the labelling and enabling of
	 * the redo action for the specified undo context.
	 * 
	 * @param site 
	 *            the workbench part site that created the action.
	 * @param context 
	 *            the undo context to be used for redoing.
	 */
	public RedoActionHandler(IWorkbenchPartSite site, IUndoContext context) {
		super(site, context);
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));

	}

	void flush() {
		getHistory().dispose(undoContext, false, true, false);
	}

	String getCommandString() {
		return WorkbenchMessages.Workbench_redo;
	}

	IUndoableOperation getOperation() {
		return getHistory().getRedoOperation(undoContext);
	}

	IStatus runCommand(IProgressMonitor pm) throws ExecutionException {
		return getHistory().redo(undoContext, pm, this);
	}

	boolean shouldBeEnabled() {
		// make sure a context is set. If a part doesn't provide
		// a context, then we should not enable.
		if (undoContext == null)
			return false;
		return getHistory().canRedo(undoContext);
	}
}
