package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Edit the action sets.
 */
public class EditActionSetsAction  extends Action {
	private IWorkbenchWindow window;
/**
 * This default constructor allows the the action to be called from the welcome page.
 */
public EditActionSetsAction() {
	this(((Workbench)PlatformUI.getWorkbench()).getActiveWorkbenchWindow());
}
/**
 * 
 */
public EditActionSetsAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("EditActionSetsAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("EditActionSetsAction.toolTip")); //$NON-NLS-1$
	setEnabled(false);
	this.window = window;
	WorkbenchHelp.setHelp(this, IHelpContextIds.EDIT_ACTION_SETS_ACTION);
}
/**
 * Open the selected resource in the default page.
 */
public void run() {
	WorkbenchPage page = (WorkbenchPage)window.getActivePage();
	if (page == null)
		return;
	page.editActionSets();
}
}
