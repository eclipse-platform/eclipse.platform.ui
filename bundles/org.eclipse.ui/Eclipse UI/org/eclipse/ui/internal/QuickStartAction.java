package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.ui.internal.dialogs.WelcomeDialog;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.swt.widgets.*;

/**
 * Launch the quick start action.
 * 1FVKH62: ITPUI:WINNT - quick start should be available on file menu
 */
public class QuickStartAction extends PartEventAction {
	private IWorkbench workbench;
	
/**
 *	Create an instance of this class
 */
public QuickStartAction(IWorkbench aWorkbench) {
	super("&Quick Start");
	setToolTipText("Open the Quick Start dialog");
	this.workbench = aWorkbench;
}
/**
 *	Open the quick start window.
 */
public void openQuickStart(Shell parent, boolean force) {

	// get a vector of QuickStartElements from the extension point
	WizardsRegistryReader reader = new WizardsRegistryReader(IWorkbenchConstants.PL_WELCOME);
	AdaptableList wizardElements = reader.getWizards();		

	// if the vector is not empty (i.e. there are elements registered), open the dialog
	if (force || wizardElements.getChildren().length > 0) {
		WelcomeDialog welcome = new WelcomeDialog(parent, this.workbench, wizardElements);
		welcome.setBlockOnOpen(true);
		welcome.open();
	}
}
/**
 *	The user has invoked this action
 */
public void run() {
	openQuickStart(this.workbench.getActiveWorkbenchWindow().getShell(), true);
}
}
