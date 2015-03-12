/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton - francisu@ieee.org)
 *        bug 214271 Undo/redo not enabled if nothing selected
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * @since 3.2
 *
 */
public class RefactorActionProvider extends CommonActionProvider {

	private RefactorActionGroup refactorGroup;

	@Override
	public void init(ICommonActionExtensionSite anActionSite) {
		refactorGroup = new RefactorActionGroup(anActionSite.getViewSite().getShell(), (Tree)anActionSite.getStructuredViewer().getControl());
	}

	@Override
	public void dispose() {
		refactorGroup.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		refactorGroup.fillActionBars(actionBars);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		refactorGroup.fillContextMenu(menu);
	}

	@Override
	public void setContext(ActionContext context) {
		refactorGroup.setContext(context);
	}

	@Override
	public void updateActionBars() {
		refactorGroup.updateActionBars();
	}

}
