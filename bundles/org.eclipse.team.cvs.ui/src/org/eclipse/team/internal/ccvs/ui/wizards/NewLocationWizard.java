package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSProvider;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class NewLocationWizard extends Wizard {
	private ConfigurationWizardMainPage mainPage;

	private Properties properties;
	
	public NewLocationWizard() {
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("NewLocationWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("NewLocationWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}

	/**
	 * Creates the wizard pages
	 */
	public void addPages() {
		mainPage = new ConfigurationWizardMainPage("repositoryPage1", Policy.bind("NewLocationWizard.title"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_SHARE));
		if (properties != null) {
			mainPage.setProperties(properties);
		}
		mainPage.setShowValidate(true);
		mainPage.setDescription(Policy.bind("NewLocationWizard.description"));
		mainPage.setDialogSettings(getDialogSettings());
		addPage(mainPage);
	}
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		mainPage.finish(new NullProgressMonitor());
		Properties properties = mainPage.getProperties();
		ICVSRepositoryLocation root = null;
		ICVSProvider provider = CVSProviderPlugin.getProvider();
		try {
			root = provider.createRepository(properties);
			if (mainPage.getValidate()) {
				root.validateConnection(new NullProgressMonitor());
			}
		} catch (TeamException e) {
			IStatus error = e.getStatus();
			if (root == null) {
				// Exception creating the root, we cannot continue
				ErrorDialog.openError(getContainer().getShell(), Policy.bind("NewLocationWizard.exception"), null, error);
				return false;
			} else {
				// Exception validating. We can continue if the user wishes.
				boolean keep = MessageDialog.openQuestion(getContainer().getShell(),
					Policy.bind("NewLocationWizard.validationFailedTitle"),
					Policy.bind("NewLocationWizard.validationFailedText", new Object[] {e.getStatus().getMessage()}));
				if (!keep) {
					// Remove the root
					try {
						provider.disposeRepository(root);
					} catch (TeamException e1) {
						ErrorDialog.openError(getContainer().getShell(), Policy.bind("exception"), null, e1.getStatus());
						return false;
					}
				}
				return keep;
			}
		}
		return true;	
	}
}
