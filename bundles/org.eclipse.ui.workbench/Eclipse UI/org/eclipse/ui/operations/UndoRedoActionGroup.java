/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.operations;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionGroup;

/**
 * <p>
 * UndoRedoActionGroup provides standard undo and redo action handlers for a
 * workbench part site. It supports filtering of undo or redo on a particular
 * undo context. The undo context can be optionally pruned, which means the
 * context will be flushed actively whenever an invalid operation is found on
 * top of its history. This class may be instantiated by clients.
 * </p>
 * 
 * @since 3.1
 */
public final class UndoRedoActionGroup extends ActionGroup {

	private UndoActionHandler undoActionHandler;

	private RedoActionHandler redoActionHandler;

	/**
	 * Construct an undo redo action group for the specified workbench part
	 * site, using the specified undo context.
	 * 
	 * @param site
	 *            the workbench part site that is creating the action group
	 * @param undoContext
	 *            the undo context to be used for filtering the operation
	 *            history
	 * @param pruneHistory
	 *            a boolean that indicates whether the history for the specified
	 *            context should be pruned whenever an invalid operation is
	 *            encountered.
	 */
	public UndoRedoActionGroup(IWorkbenchPartSite site,
			IUndoContext undoContext, boolean pruneHistory) {

		// create the undo action handler
		undoActionHandler = new UndoActionHandler(site, undoContext);
		undoActionHandler.setPruneHistory(pruneHistory);

		// create the redo action handler
		redoActionHandler = new RedoActionHandler(site, undoContext);
		redoActionHandler.setPruneHistory(pruneHistory);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		if (undoActionHandler != null) {
			actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
					undoActionHandler);
			actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
					redoActionHandler);
		}
	}
}
