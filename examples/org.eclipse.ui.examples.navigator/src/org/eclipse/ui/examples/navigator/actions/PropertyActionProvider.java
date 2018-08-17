/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.examples.navigator.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * @since 3.2
 *
 */
public class PropertyActionProvider extends CommonActionProvider {

	private OpenPropertyAction openAction;

	/**
	 * Construct Property Action provider.
	 */
	public PropertyActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite aSite) {

		ICommonViewerSite viewSite = aSite.getViewSite();
		if(viewSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite workbenchSite =
				(ICommonViewerWorkbenchSite) viewSite;
			openAction =
				new OpenPropertyAction(workbenchSite.getPage(),
										workbenchSite.getSelectionProvider());
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		/* Set up the property open action when enabled. */
		if(openAction.isEnabled())
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		if(openAction.isEnabled())
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
	}


}
