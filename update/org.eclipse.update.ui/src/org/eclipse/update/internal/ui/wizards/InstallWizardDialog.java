package org.eclipse.update.internal.ui.wizards;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.wizard.*;

public class InstallWizardDialog extends WizardDialog {
	
	public InstallWizardDialog(Shell shell, IWizard wizard) {
		super(shell, wizard);
	}
	
	public void cancel() {
		cancelPressed();
	}
}

