package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Clears the working set from the the active workbench page.
 * 
 * @since 2.0
 */
public class ClearWorkingSetAction extends Action {
	private IWorkbenchWindow window;

/**
 * Create an instance of this class.
 *
 * @param window the workbench window to use to determine 
 * 	the active workbench page
 */
public ClearWorkingSetAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("ClearWorkingSetAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ClearWorkingSetAction.toolTip")); //$NON-NLS-1$
	setEnabled(false);
	WorkbenchHelp.setHelp(this, IHelpContextIds.CLEAR_WORKING_SET_ACTION);
	this.window = window;
}
/**
 * The user has invoked this action.
 * Clear the working set from the the active workbench page.
 */
public void run() {
	if (window != null) {
		IWorkbenchPage page = window.getActivePage();
		((WorkbenchPage) page).setWorkingSet(null);
	}
}
}
