package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Shows a list of resources to the user with a text entry field
 * for a string pattern used to filter the list of resources.
 *
 * @since 2.1
 */
public class OpenResourceDialog extends ResourceListSelectionDialog {

/**
 * Creates a new instance of the class.
 */
public OpenResourceDialog(Shell parentShell, IResource resources[]) {
	super(parentShell, resources);
	setTitle(WorkbenchMessages.getString("OpenResourceDialog.title")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(parentShell, IHelpContextIds.OPEN_RESOURCE_DIALOG);
}
}
