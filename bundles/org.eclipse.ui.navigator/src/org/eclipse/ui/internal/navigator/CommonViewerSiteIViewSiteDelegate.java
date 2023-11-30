/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Provides a delegate implementation of {@link ICommonViewerWorkbenchSite}.
 *
 * @since 3.2
 */
public class CommonViewerSiteIViewSiteDelegate implements ICommonViewerWorkbenchSite {

	private IViewSite viewSite;

	public CommonViewerSiteIViewSiteDelegate(IViewSite aViewSite) {
		viewSite = aViewSite;
	}

	@Override
	public String getId() {
		return viewSite.getId();
	}

	@Override
	public IActionBars getActionBars() {
		return viewSite.getActionBars();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return Adapters.adapt(viewSite, adapter);
	}

	@Override
	public IWorkbenchPage getPage() {
		return viewSite.getPage();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return viewSite.getSelectionProvider();
	}

	@Override
	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		viewSite.setSelectionProvider(aSelectionProvider);
	}

	@Override
	public Shell getShell() {
		return viewSite.getShell();
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return viewSite.getWorkbenchWindow();
	}

	@Override
	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		viewSite.registerContextMenu(menuId, menuManager, selectionProvider);
	}

	@Override
	public IWorkbenchPart getPart() {
		return viewSite.getPart();
	}

	@Override
	public IWorkbenchPartSite getSite() {
		return viewSite;
	}

}
