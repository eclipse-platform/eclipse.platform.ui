package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Closes the active editor.
 */
public class CloseEditorAction extends ActiveEditorAction {
/**
 *	Create an instance of this class
 */
public CloseEditorAction(IWorkbenchWindow window,  String id) {
	super("", window); //$NON-NLS-1$
	initializeFromRegistry(id);
	setId(IWorkbenchActionConstants.CLOSE);
	setAccelerator(SWT.CTRL | SWT.F4);
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	IEditorPart part = getActiveEditor();
	if (part != null)
		getActivePage().closeEditor(part, true);
}
}
