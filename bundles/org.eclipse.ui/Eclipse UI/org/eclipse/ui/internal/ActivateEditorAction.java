package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Activates the most recently used editor in the current window.
 */
public class ActivateEditorAction extends PageEventAction {

/**
 * Creates an ActivateEditorAction.
 */
protected ActivateEditorAction(IWorkbenchWindow window, String id) {
	super("", window); //$NON-NLS-1$
	initializeFromRegistry(id);
	window.getPartService().addPartListener(this);
	updateState();
}

public void pageActivated(IWorkbenchPage page) {
	super.pageActivated(page);
	updateState();
}

public void pageClosed(IWorkbenchPage page) {
	super.pageClosed(page);
	updateState();
}

public void partOpened(IWorkbenchPart part) {
	super.partOpened(part);
	if (part instanceof IEditorPart) {
		updateState();
	}
}

public void partClosed(IWorkbenchPart part) {
	super.partClosed(part);
	if (part instanceof IEditorPart) {
		updateState();
	}
}

/**
 * @see Action#run()
 */
public void run() {
	IWorkbenchPage page = getActivePage();
	if (page != null) {
		IEditorPart editor = page.getActiveEditor(); // may not actually be active
		if (editor != null) {
			page.activate(editor);
		}
	}
}

/**
 * Updates the enabled state.
 */
public void updateState() {
	IWorkbenchPage page = getActivePage();
	if (page == null) {
		setEnabled(false);
		return;
	}
	setEnabled(page.getEditors().length >= 1);
}
 
}

