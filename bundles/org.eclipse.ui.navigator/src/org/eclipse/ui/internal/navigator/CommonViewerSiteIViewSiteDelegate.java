/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

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
 * 
 */
public class CommonViewerSiteIViewSiteDelegate implements ICommonViewerWorkbenchSite {

	private IViewSite viewSite; 

	/**
	 * 
	 * @param aViewSite
	 */
	public CommonViewerSiteIViewSiteDelegate(IViewSite aViewSite) {
		viewSite = aViewSite; 
	}

	public String getId() {
		return viewSite.getId();
	}

	public IActionBars getActionBars() {
		return viewSite.getActionBars();
	}

	public Object getAdapter(Class adapter) {
		return viewSite.getAdapter(adapter);
	}

	public IWorkbenchPage getPage() {
		return viewSite.getPage();
	}

	public ISelectionProvider getSelectionProvider() {
		return viewSite.getSelectionProvider();
	}

	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		viewSite.setSelectionProvider(aSelectionProvider);
	}

	public Shell getShell() {
		return viewSite.getShell();
	}

	public IWorkbenchWindow getWorkbenchWindow() {
		return viewSite.getWorkbenchWindow();
	}

	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		viewSite.registerContextMenu(menuId, menuManager, selectionProvider);
	}
 
	public IWorkbenchPart getPart() { 
		return viewSite.getPart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ICommonViewerWorkbenchSite#getSite()
	 */
	public IWorkbenchPartSite getSite() { 
		return viewSite;
	}

}
