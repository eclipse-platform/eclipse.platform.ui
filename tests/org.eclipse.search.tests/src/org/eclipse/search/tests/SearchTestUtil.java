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

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * Util class for search tests.
 */
public class SearchTestUtil {


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
