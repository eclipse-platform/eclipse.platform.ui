package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.ui.wizards.CheckoutWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * CheckoutAction prompts the user for the location of an existing
 * project in a CVS repository, and then checks the project out of
 * the repository, placing it in the workspace.
 * 
 * This action provides temporary functionality until a full CVS
 * repository explorer view can be implemented.
 */
public class CheckoutAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;
	
	/*
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}
	
	/**
	 * Convenience method for getting the shell
	 * 
	 * @return the shell
	 */
	protected Shell getShell() {
		return window.getShell();
	}
	
	/*
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
	
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		CheckoutWizard wizard = new CheckoutWizard();
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		dialog.open();
	}

	/*
	 * @see IActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}

