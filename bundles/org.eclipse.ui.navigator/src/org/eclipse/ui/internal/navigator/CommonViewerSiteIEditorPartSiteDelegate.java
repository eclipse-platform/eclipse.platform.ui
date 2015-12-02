/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	@Override
	public String getId() {
		return editorSite.getId();
	}

	@Override
	public IActionBars getActionBars() {
		return editorSite.getActionBars();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return Adapters.adapt(editorSite, adapter);
	}

	@Override
	public IWorkbenchPage getPage() {
		return editorSite.getPage();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return editorSite.getSelectionProvider();
	}

	@Override
	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		editorSite.setSelectionProvider(aSelectionProvider);
	}

	@Override
	public Shell getShell() {
		return editorSite.getShell();
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return editorSite.getWorkbenchWindow();
	}

	@Override
	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		editorSite.registerContextMenu(menuId, menuManager, selectionProvider);
	}

	@Override
	public IWorkbenchPart getPart() {
		return editorSite.getPart();
	}

	@Override
	public IWorkbenchPartSite getSite() {
		return editorSite;
	}

}
