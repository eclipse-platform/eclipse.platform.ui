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

/**
 * Opens the Search Dialog.
 */
public class OpenSearchDialogAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow fWindow;
	private String fPageId;

	public OpenSearchDialogAction() {
		super(SearchMessages.getString("OpenSearchDialogAction.label")); //$NON-NLS-1$
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_TOOL, SearchPluginImages.IMG_TOOL_SEARCH);
		setToolTipText(SearchMessages.getString("OpenSearchDialogAction.tooltip")); //$NON-NLS-1$
	}

	public OpenSearchDialogAction(IWorkbenchWindow window, String pageId) {
		this();
		fPageId= pageId;
		fWindow= window;
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
		SearchDialog dialog= new SearchDialog(
			getWindow().getShell(),
			SearchPlugin.getWorkspace(),
			getSelection(),
			getEditorPart(),
			fPageId);
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
