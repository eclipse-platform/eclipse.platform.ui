/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton - francisu@ieee.org)
 *        bug 214271 Undo/redo not enabled if nothing selected
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * @since 3.3
 */
public class GotoActionProvider extends CommonActionProvider {

	private GotoResourceAction gotoAction;

	@Override
	public void init(ICommonActionExtensionSite anActionSite) {
		gotoAction = new GotoResourceAction(anActionSite.getViewSite().getShell(), (CommonViewer)anActionSite.getStructuredViewer());
	}


	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(
				IWorkbenchActionConstants.GO_TO_RESOURCE, gotoAction);
		updateActionBars();
	}

}
