package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog;

/**
 * Reset the layout within the active perspective.
 */
public class RemoveWorkingSetAction extends Action {
	private IWorkbenchWindow window;

/**
 *	Create an instance of this class
 */
public RemoveWorkingSetAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("RemoveWorkingSetAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("RemoveWorkingSetAction.toolTip")); //$NON-NLS-1$
	setEnabled(false);
//	WorkbenchHelp.setHelp(this, IHelpContextIds.RESET_PERSPECTIVE_ACTION);
	this.window = window;
}
/**
 *	The user has invoked this action
 */
public void run() {
	if (window != null) {
		IWorkbenchPage page = window.getActivePage();
		((WorkbenchPage) page).setWorkingSet(null);
	}
}
}
