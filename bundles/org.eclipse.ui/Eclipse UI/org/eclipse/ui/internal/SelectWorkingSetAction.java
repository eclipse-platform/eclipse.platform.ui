package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Displays an IWorkingSetSelectionDialog and sets the selected 
 * working set in the active workbench page.
 * 
 * @since 2.0
 */
public class SelectWorkingSetAction extends Action {
	private IWorkbenchWindow window;

/**
 * Creates an instance of this class.
 * 
 * @param window the workbench window to use to determine 
 * 	the active workbench page
 */
public SelectWorkingSetAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("SelectWorkingSetAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("SelectWorkingSetAction.toolTip")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.SELECT_WORKING_SET_ACTION);
	this.window = window;
}
/**
 * The user has invoked this action.
 * Display the working set selection dialog and set the selected 
 * working set in the active workbench page.
 */
public void run() {
	if (window != null) {
		IWorkbenchPage page = window.getActivePage();
		IWorkingSetSelectionDialog dialog = WorkbenchPlugin.getDefault().getWorkingSetManager().createWorkingSetSelectionDialog(window.getShell());
		IWorkingSet workingSet = page.getWorkingSet();
		if (workingSet != null) {
			dialog.setSelection(new IWorkingSet[]{workingSet});
		}
		if (dialog.open() == Window.OK && page instanceof WorkbenchPage) {
			WorkbenchPage workbenchPage = (WorkbenchPage) page;
			IWorkingSet[] result = dialog.getSelection();
			if (result.length > 0) {
				workbenchPage.setWorkingSet(result[0]);
			}
			else {
				workbenchPage.setWorkingSet(null);
			}
		}
	}
}
}
