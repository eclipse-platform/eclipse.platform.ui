/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.search.ui.NewSearchUI;

/**
 * Opens the Search Dialog.
 */
public class OpenFileSearchPageAction implements IWorkbenchWindowActionDelegate {

	private static final String TEXT_SEARCH_PAGE_ID= "org.eclipse.search.internal.ui.text.TextSearchPage"; //$NON-NLS-1$

	private IWorkbenchWindow fWindow;

	public OpenFileSearchPageAction() {
	}

	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		if (fWindow == null || fWindow.getActivePage() == null) {
			SearchPlugin.beep();
			logErrorMessage("Could not open the search dialog - for some reason the window handle was null"); //$NON-NLS-1$
			return;
		}
		NewSearchUI.openSearchDialog(fWindow, TEXT_SEARCH_PAGE_ID);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	public void dispose() {
		fWindow= null;
	}

	public static void logErrorMessage(String message) {
		IStatus status= new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, message, null);
		SearchPlugin.log(status);
	}
}
