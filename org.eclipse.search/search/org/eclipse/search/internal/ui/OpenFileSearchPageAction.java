/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.search.ui.SearchUI;

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
		SearchUI.openSearchDialog(fWindow, TEXT_SEARCH_PAGE_ID); //$NON-NLS-1$
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	public void dispose() {
		fWindow= null;
	}

	public static void logErrorMessage(String message) {
		IStatus status= new Status(IStatus.ERROR, SearchUI.PLUGIN_ID, IStatus.ERROR, message, null);
		SearchPlugin.getDefault().log(status);
	}
}
