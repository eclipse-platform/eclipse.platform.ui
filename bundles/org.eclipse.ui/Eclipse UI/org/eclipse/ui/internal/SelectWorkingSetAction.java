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
public class SelectWorkingSetAction extends Action {
	private IWorkbenchWindow window;

/**
 *	Create an instance of this class
 */
public SelectWorkingSetAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("SelectWorkingSetAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("SelectWorkingSetAction.toolTip")); //$NON-NLS-1$
//	setEnabled(true);
//	WorkbenchHelp.setHelp(this, IHelpContextIds.RESET_PERSPECTIVE_ACTION);
	this.window = window;
}
/**
 *	The user has invoked this action
 */
public void run() {
	if (window != null) {
		IWorkbenchPage page = window.getActivePage();
		WorkingSetSelectionDialog dialog = new WorkingSetSelectionDialog(window.getShell());
		IWorkingSet workingSet= page.getWorkingSet();
		if (workingSet != null)
			dialog.setInitialSelections(new IWorkingSet[] {workingSet});
		if (dialog.open() == dialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1 && page instanceof WorkbenchPage) {
//				window.openPage
				((WorkbenchPage) page).setWorkingSet((IWorkingSet) result[0]);
			}
		}
	}
}
}
