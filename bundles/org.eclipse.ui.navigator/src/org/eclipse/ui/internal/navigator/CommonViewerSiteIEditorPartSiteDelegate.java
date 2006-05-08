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
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Provides a delegate implementation of {@link ICommonViewerWorkbenchSite}.
 * @since 3.2
 *
 */
public class CommonViewerSiteIEditorPartSiteDelegate implements
		ICommonViewerWorkbenchSite {

	private IEditorSite editorSite;  

	/**
	 * 
	 * @param anEditorSite
	 */
	public CommonViewerSiteIEditorPartSiteDelegate(IEditorSite anEditorSite) {
		editorSite = anEditorSite; 
	}

	public String getId() {
		return editorSite.getId();
	}

	public IActionBars getActionBars() {
		return editorSite.getActionBars();
	}

	public Object getAdapter(Class adapter) {
		return editorSite.getAdapter(adapter);
	} 

	public IWorkbenchPage getPage() {
		return editorSite.getPage();
	}

	public ISelectionProvider getSelectionProvider() {
		return editorSite.getSelectionProvider();
	}

	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		editorSite.setSelectionProvider(aSelectionProvider);
	}

	public Shell getShell() {
		return editorSite.getShell();
	}

	public IWorkbenchWindow getWorkbenchWindow() {
		return editorSite.getWorkbenchWindow();
	}

	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		editorSite.registerContextMenu(menuId, menuManager, selectionProvider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ICommonViewerWorkbenchSite#getViewPart()
	 */
	public IWorkbenchPart getPart() { 
		return editorSite.getPart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ICommonViewerWorkbenchSite#getSite()
	 */
	public IWorkbenchPartSite getSite() {
		return editorSite;
	}

}
