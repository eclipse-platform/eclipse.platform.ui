package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.wizards.NewLocationWizard;
import org.eclipse.ui.IWorkbenchWindow;

public class NewCVSRepositoryAction extends Action {
	public void run() {
		Shell shell;
		IWorkbenchWindow window = CVSUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			shell = window.getShell();
		} else {
			Display display = Display.getCurrent();
			shell = new Shell(display);
		}
		NewLocationWizard wizard = new NewLocationWizard();
		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.open();
	}
}
