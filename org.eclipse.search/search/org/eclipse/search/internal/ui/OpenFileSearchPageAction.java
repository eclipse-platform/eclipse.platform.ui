/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.search.ui.SearchUI;

/**
 * Opens the Search Dialog.
 */
public class OpenFileSearchPageAction extends Action implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow fWindow;

	public OpenFileSearchPageAction() {
	}

	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		run();
	}

	public void run() {
		if (getWindow().getActivePage() == null) {
			SearchPlugin.beep();
			return;
		}
		SearchUI.openSearchDialog(getWindow(), "org.eclipse.search.internal.ui.text.TextSearchPage"); //$NON-NLS-1$
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	private ISelection getSelection() {
		return getWindow().getSelectionService().getSelection();
	}
	
	private IEditorPart getEditorPart() {
		return getWindow().getActivePage().getActiveEditor();
	}

	private IWorkbenchWindow getWindow() {
		if (fWindow == null)
			fWindow= SearchPlugin.getActiveWorkbenchWindow();
		return fWindow;
	}

	public void dispose() {
		fWindow= null;
	}
}
