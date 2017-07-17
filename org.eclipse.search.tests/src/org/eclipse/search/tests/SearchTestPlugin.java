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
package org.eclipse.search.tests;

import org.eclipse.core.resources.IFile;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search2.internal.ui.SearchView;


/**
 * Plugin class for search tests.
 */
public class SearchTestPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static SearchTestPlugin fgPlugin;

	public SearchTestPlugin() {
		fgPlugin = this;
	}

	public static SearchTestPlugin getDefault() {
		return fgPlugin;
	}

	public SearchView getSearchView() {
		return (SearchView) NewSearchUI.activateSearchResultView();
	}

	public static void ensureWelcomePageClosed() {
		IWorkbenchWindow activeWorkbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			return;

		IWorkbenchPage page= activeWorkbenchWindow.getActivePage();
		if (page == null)
			return;

		IWorkbenchPart part= page.getActivePart();
		if (part == null)
			return;

		if ("org.eclipse.ui.internal.introview".equals(part.getSite().getId()))
			page.hideView((IViewPart)part);
	}

	public static IEditorPart openTextEditor(IWorkbenchPage activePage, IFile openFile1) throws PartInitException {
		return IDE.openEditor(activePage, openFile1, EditorsUI.DEFAULT_TEXT_EDITOR_ID, true);
	}

}
