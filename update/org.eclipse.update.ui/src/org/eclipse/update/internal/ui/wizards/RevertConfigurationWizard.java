/*
 * Created on May 21, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.wizard.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.UpdateUI;

/**
 * @author Wassim Melhem
 */
public class RevertConfigurationWizard extends Wizard {
	
	RevertConfigurationWizardPage page;

	public RevertConfigurationWizard() {
		super();
		setWindowTitle(UpdateUI.getString("RevertConfigurationWizard.wtitle")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(UpdateUIImages.DESC_CONFIG_WIZ);
	}
	
	public void addPages() {
		page = new RevertConfigurationWizardPage();
		addPage(page);
	}

	public boolean canFinish() {
		return page.isPageComplete();
	}

	public boolean performFinish() {
		return page.performFinish();
	}

}
