/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;


/**
 * <p>
 * UndoActionHandler provides common behavior for performing an undo, as
 * well as labelling and enabling the undo menu item.  This class may be
 * instantiated by clients.
 * </p>
 *
 * @since 3.1
 */
public final class UndoActionHandler extends OperationHistoryActionHandler {

	/**
	 * Construct an action handler that handles the labelling and enabling of
	 * the undo action for the specified undo context.
	 *
	 * @param site
	 *            the workbench part site that created the action.
	 * @param context
	 *            the undo context to be used for the undo
	 */
	public UndoActionHandler(IWorkbenchPartSite site, IUndoContext context) {
		super(site, context);
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
        setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_UNDO_DISABLED));
		setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);
	}

	@Override
	void flush() {
		getHistory().dispose(getUndoContext(), true, false, false);
	}

	@Override
	String getCommandString() {
		return WorkbenchMessages.Operations_undoCommand;
	}

	@Override
	String getTooltipString() {
		return WorkbenchMessages.Operations_undoTooltipCommand;
	}

	@Override
	String getSimpleCommandString() {
		return WorkbenchMessages.Workbench_undo;
	}

	@Override
	String getSimpleTooltipString() {
		return WorkbenchMessages.Workbench_undoToolTip;
	}

	@Override
	IUndoableOperation getOperation() {
		return getHistory().getUndoOperation(getUndoContext());

	}

	@Override
	IStatus runCommand(IProgressMonitor pm) throws ExecutionException  {
		return getHistory().undo(getUndoContext(), pm, this);
	}

	@Override
	boolean shouldBeEnabled() {
		return getHistory().canUndo(getUndoContext());
	}
}
