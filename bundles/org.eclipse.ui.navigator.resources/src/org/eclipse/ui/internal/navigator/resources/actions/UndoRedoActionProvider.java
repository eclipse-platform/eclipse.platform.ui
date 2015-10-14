/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
import org.eclipse.core.runtime.Adapters;
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

	@Override
	public void init(ICommonActionExtensionSite anActionSite) {
		IUndoContext workspaceContext = Adapters.adapt(ResourcesPlugin.getWorkspace(), IUndoContext.class);
		undoRedoGroup = new UndoRedoActionGroup(((ICommonViewerWorkbenchSite) anActionSite.getViewSite()).getSite(),
				workspaceContext, true);
	}

	@Override
	public void dispose() {
		undoRedoGroup.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		undoRedoGroup.fillActionBars(actionBars);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		undoRedoGroup.fillContextMenu(menu);
	}

	@Override
	public void setContext(ActionContext context) {
		undoRedoGroup.setContext(context);
	}

	@Override
	public void updateActionBars() {
		undoRedoGroup.updateActionBars();
	}

}
