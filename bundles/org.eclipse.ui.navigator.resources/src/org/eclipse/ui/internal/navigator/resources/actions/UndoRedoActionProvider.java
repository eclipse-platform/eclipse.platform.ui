/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *       (was originally RefactorActionProvider.java)
 *     Oakland Software (Francis Upton - francisu@ieee.org) 
 *        bug 214271 Undo/redo not enabled if nothing selected
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.operations.UndoRedoActionGroup;

/**
 * @since 3.4
 * 
 */
public class UndoRedoActionProvider extends CommonActionProvider {

	private UndoRedoActionGroup undoRedoGroup;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	public void init(ICommonActionExtensionSite anActionSite) {
		IUndoContext workspaceContext = (IUndoContext) ResourcesPlugin
				.getWorkspace().getAdapter(IUndoContext.class);
		undoRedoGroup = new UndoRedoActionGroup(((ICommonViewerWorkbenchSite) anActionSite.getViewSite()).getSite(),
				workspaceContext, true);
	}

	public void dispose() {
		undoRedoGroup.dispose();
	}

	public void fillActionBars(IActionBars actionBars) {
		undoRedoGroup.fillActionBars(actionBars);
	}

	public void fillContextMenu(IMenuManager menu) {
		undoRedoGroup.fillContextMenu(menu);
	}

	public void setContext(ActionContext context) {
		undoRedoGroup.setContext(context);
	}

	public void updateActionBars() {
		undoRedoGroup.updateActionBars();
	}

}
