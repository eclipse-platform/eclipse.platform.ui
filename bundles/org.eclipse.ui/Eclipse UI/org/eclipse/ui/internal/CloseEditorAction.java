package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.internal.IHelpContextIds;

/**
 * Closes the active editor.
 */
public class CloseEditorAction extends ActiveEditorAction {
/**
 *	Create an instance of this class
 */
public CloseEditorAction(IWorkbenchWindow window) {
	super("&Close@Ctrl+F4", window);
	setToolTipText("Close the open editor");
	setId(IWorkbenchActionConstants.CLOSE);
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.CLOSE_PART_ACTION});
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
