package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 */
public class SaveAction extends BaseSaveAction {

/**
 *	Create an instance of this class
 */
public SaveAction(IWorkbenchWindow window, String id) {
	super("", window); //$NON-NLS-1$
	initializeFromRegistry(id);
	setId(IWorkbenchActionConstants.SAVE);
	setAccelerator(SWT.CTRL |'s');
}
/**
 * Performs the save.
 */
public void run() {
	IEditorPart part = getActiveEditor();
	IWorkbenchPage page = part.getSite().getPage();
	page.saveEditor(part, false);
}
/* (non-Javadoc)
 * Method declared on ActiveEditorAction.
 */
protected void updateState() {
	IEditorPart editor = getActiveEditor();
	if (editor != null) {
		String title = editor.getTitle();
		String label = WorkbenchMessages.format("SaveAction.textOneArg", new Object[] {title}); //$NON-NLS-1$
		setText(label);
		setEnabled(editor.isDirty());
	} else {
		setText(WorkbenchMessages.getString("SaveAction.text")); //$NON-NLS-1$
		setEnabled(false);
	}
}
}
