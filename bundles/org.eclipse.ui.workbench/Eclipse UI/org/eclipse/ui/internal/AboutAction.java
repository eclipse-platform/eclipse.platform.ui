package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.Action;

import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.dialogs.AboutDialog;

/**
 * Creates an About dialog and opens it.
 */
public class AboutAction extends Action {
	private IWorkbenchWindow workbenchWindow;
	
/**
 * Creates a new <code>AboutAction</code> with the given label
 */
public AboutAction(IWorkbenchWindow window) {
	this.workbenchWindow = window;
	AboutInfo aboutInfo = ((Workbench) PlatformUI.getWorkbench()).getConfigurationInfo().getAboutInfo();
	String productName = aboutInfo.getProductName();
	if (productName == null) {
		productName = ""; //$NON-NLS-1$
	}
	setText(WorkbenchMessages.format("AboutAction.text", new Object[] { productName })); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.format("AboutAction.toolTip", new Object[] { productName})); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.ABOUT);
	WorkbenchHelp.setHelp(this, IHelpContextIds.ABOUT_ACTION);
}

/**
 * Perform the action: show about dialog.
 */
public void run() {
	new AboutDialog(workbenchWindow.getShell()).open();
}
}
