/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.ui.navigator.resources;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.internal.navigator.framelist.GoIntoAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * Provides the Go Into action for the {@link ProjectExplorer}
 *
 * @since 3.4
 *
 */
public class GoIntoActionProvider extends CommonActionProvider {

	private GoIntoAction goIntoAction;

	@Override
	public void init(ICommonActionExtensionSite anActionSite) {
		anActionSite.getViewSite().getShell();
		CommonViewer viewer = (CommonViewer) anActionSite.getStructuredViewer();
		goIntoAction = new GoIntoAction(viewer.getFrameList());
	}

	@Override
	public void dispose() {
		goIntoAction.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.GO_INTO, goIntoAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		menu.appendToGroup("group.new", goIntoAction); //$NON-NLS-1$
	}

	@Override
	public void updateActionBars() {
		goIntoAction.update();
	}

}
