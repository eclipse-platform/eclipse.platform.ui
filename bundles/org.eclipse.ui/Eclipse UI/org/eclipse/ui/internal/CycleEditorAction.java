package org.eclipse.ui.internal;

import org.eclipse.ui.*;

/**
 * This is the abstract superclass for NextEditorAction and PrevEditorAction.
 */
public class CycleEditorAction extends PageEventAction {
	private boolean forward;
	
/**
 * Creates a CycleEditorAction.
 */
protected CycleEditorAction(IWorkbenchWindow window, boolean forward) {
	super("", window); //$NON-NLS-1$
	this.forward = forward;
	// TBD: Remove text and tooltip when this becomes an invisible action.
	if (forward) {
		setText(WorkbenchMessages.getString("NextEditorAction.text"));
		setToolTipText(WorkbenchMessages.getString("NextEditorAction.toolTip"));
	}
	else {
		setText(WorkbenchMessages.getString("PrevEditorAction.text"));
		setToolTipText(WorkbenchMessages.getString("PrevEditorAction.toolTip"));
	}
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
		((WorkbenchPage) page).cycleEditors(forward);
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
	setEnabled(page.getEditors().length >= 2);
}
 
}

