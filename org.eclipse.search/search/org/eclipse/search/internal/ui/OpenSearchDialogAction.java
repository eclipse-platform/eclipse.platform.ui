package org.eclipse.search.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Opens the Search Dialog.
 */
public class OpenSearchDialogAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow fWindow;

	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		if (getWindow().getActivePage() == null) {
			SearchPlugin.beep();
			return;
		}
		SearchDialog dialog= new SearchDialog(
			getWindow().getShell(),
			SearchPlugin.getWorkspace(),
			getSelection(),
			getEditorPart());
		dialog.open();
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
