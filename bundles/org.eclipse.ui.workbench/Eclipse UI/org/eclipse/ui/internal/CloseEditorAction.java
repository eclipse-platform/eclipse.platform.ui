package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.SWT;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Closes the active editor.
 */
public class CloseEditorAction extends ActiveEditorAction {
/**
 *	Create an instance of this class
 */
public CloseEditorAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("CloseEditorAction.text"), window); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("CloseEditorAction.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.CLOSE);
	WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_PART_ACTION);
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
