package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.ResourceListSelectionDialog;

/**
 * Shows a list of resources to the user with a text entry field
 * for a string pattern used to filter the list of resources.
 *
 */
/*package*/    class GotoResourceDialog extends ResourceListSelectionDialog {

/**
 * Creates a new instance of the class.
 */
protected GotoResourceDialog(Shell parentShell, IResource resources[]) {
	super(parentShell, resources);
	setTitle(ResourceNavigatorMessages.getString("Goto.title")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(parentShell, INavigatorHelpContextIds.GOTO_RESOURCE_DIALOG);
}
}
