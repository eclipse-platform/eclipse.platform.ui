package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Creates an About dialog and opens it.
 */
public class ClonePerspectiveAction extends Action {
	private IWorkbenchWindow workbenchWindow;
/**
 * Creates a new <code>AboutAction</code> with the given label
 */
public ClonePerspectiveAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("ClonePerspective.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ClonePerspective.toolTip")); //$NON-NLS-1$
	this.workbenchWindow = window;
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.ABOUT_ACTION});
}
/**
 * Perform the action: show about dialog.
 */
public void run() {
	try {
		IWorkbenchPage page = workbenchWindow.getActivePage();
		if (page != null) {
			workbenchWindow.getWorkbench().clonePage(page);
		}
	} catch (WorkbenchException e) {
		MessageDialog.openError(
			workbenchWindow.getShell(),
			WorkbenchMessages.getString("ClonePerspective.errorTitle"), //$NON-NLS-1$,
			e.getMessage());
	}
}
}
