/********************************************************************************
 * Copyright (c) 2019 Lakshminarayana Nekkanti(narayana.nekkanti@gmail.com)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 3
 *
 * Contributor
 * Lakshminarayana Nekkanti - initial API and implementation
 ********************************************************************************/
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class ShowInActionProvider extends CommonActionProvider {

	@Override
	public void fillContextMenu(IMenuManager menu) {
		ICommonViewerSite viewerSite = getActionSite().getViewSite();
		if (viewerSite instanceof ICommonViewerWorkbenchSite) {
			IContributionItem showInAction = ContributionItemFactory.VIEWS_SHOW_IN
					.create(((ICommonViewerWorkbenchSite) viewerSite).getWorkbenchWindow());
			MenuManager showInSubMenu = new MenuManager(getShowInMenuLabel());
			showInSubMenu.add(showInAction);
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, showInSubMenu);
		}
	}

	private String getShowInMenuLabel() {
		IBindingService bindingService = PlatformUI.getWorkbench().getAdapter(IBindingService.class);
		String keyBinding = bindingService != null
				? bindingService
						.getBestActiveBindingFormattedFor(IWorkbenchCommandConstants.NAVIGATE_SHOW_IN_QUICK_MENU)
				: ""; //$NON-NLS-1$
		return WorkbenchNavigatorMessages.ShowInActionProvider_showInAction_label + '\t' + keyBinding;
	}
}