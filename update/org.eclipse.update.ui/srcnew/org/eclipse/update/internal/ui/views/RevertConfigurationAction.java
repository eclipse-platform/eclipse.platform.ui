package org.eclipse.update.internal.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.wizards.RevertConfigurationWizard;

public class RevertConfigurationAction extends Action {
	public RevertConfigurationAction(String text) {
		super(text);
	}
	
	public void run() {
		RevertConfigurationWizard wizard = new RevertConfigurationWizard();
		WizardDialog dialog = new WizardDialog(UpdateUI.getActiveWorkbenchShell(), wizard);
		dialog.create();
		dialog.getShell().setText(UpdateUI.getActiveWorkbenchShell().getText());
		dialog.getShell().setSize(500,500);
		dialog.open();
	}
}
