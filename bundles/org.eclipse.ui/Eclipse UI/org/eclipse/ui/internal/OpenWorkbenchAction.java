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
public class OpenWorkbenchAction extends Action {
	private IWorkbenchWindow workbenchWindow;
/**
 * Creates a new <code>AboutAction</code> with the given label
 */
public OpenWorkbenchAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("OpenWorkbench.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("OpenWorkbench.toolTip")); //$NON-NLS-1$
	this.workbenchWindow = window;
	WorkbenchHelp.setHelp(this, IHelpContextIds.ABOUT_ACTION);
}
/**
 * Perform the action: show about dialog.
 */
public void run() {
	try {
		workbenchWindow.getWorkbench().openPage(
			ResourcesPlugin.getWorkspace().getRoot());
	} catch (WorkbenchException e) {
		MessageDialog.openError(
			workbenchWindow.getShell(),
			WorkbenchMessages.getString("OpenWorkbench.errorTitle"), //$NON-NLS-1$,
			e.getMessage());
	}
}
}
