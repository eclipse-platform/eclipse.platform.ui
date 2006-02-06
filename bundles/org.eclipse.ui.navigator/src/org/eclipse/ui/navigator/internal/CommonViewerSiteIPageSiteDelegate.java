/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator.internal;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.part.IPageSite;

/**
 * @since 3.2
 * 
 */
public class CommonViewerSiteIPageSiteDelegate implements
		ICommonViewerSite {

	private IPageSite pageSite;

	private String viewerId;

	public CommonViewerSiteIPageSiteDelegate(String aViewerId,
			IPageSite aPageSite) {
		viewerId = aViewerId;
		pageSite = aPageSite;
	}

	public String getId() {
		return viewerId;
	}

	public IActionBars getActionBars() {
		return pageSite.getActionBars();
	}

	public Object getAdapter(Class adapter) {
		return pageSite.getAdapter(adapter);
	}

	public IWorkbenchPage getPage() {
		return pageSite.getPage();
	}

	public ISelectionProvider getSelectionProvider() {
		return pageSite.getSelectionProvider();
	}

	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		pageSite.setSelectionProvider(aSelectionProvider);
	}

	public Shell getShell() {
		return pageSite.getShell();
	}

	public IWorkbenchWindow getWorkbenchWindow() {
		return pageSite.getWorkbenchWindow();
	}

	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		pageSite.registerContextMenu(menuId, menuManager, selectionProvider);
	}
 
}
