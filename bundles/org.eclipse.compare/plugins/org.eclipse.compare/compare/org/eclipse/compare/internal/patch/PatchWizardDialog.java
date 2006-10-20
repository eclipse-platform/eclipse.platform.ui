package org.eclipse.compare.internal.patch;

import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class PatchWizardDialog extends WizardDialog {
	private static final String PATCH_WIZARD_SETTINGS_SECTION = "PatchWizard"; //$NON-NLS-1$

	public PatchWizardDialog(Shell parent, IWizard wizard) {
		super(parent, wizard);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setMinimumPageSize(700, 500);
	}
	
	protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings settings = CompareUIPlugin.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(PATCH_WIZARD_SETTINGS_SECTION);
        if (section == null) {
            section = settings.addNewSection(PATCH_WIZARD_SETTINGS_SECTION);
        } 
        return section;
	}
}
