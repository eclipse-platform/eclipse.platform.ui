package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * This wizard helps the user to add selected projects
 * to the workspace.
 */
public class AddWizard extends ConnectionWizard {
	protected String getMainPageDescription() {
		return Policy.bind("AddWizard.description"); //$NON-NLS-1$
	}
	protected String getMainPageTitle() {
		return Policy.bind("AddWizard.title"); //$NON-NLS-1$
	}
	protected int getStyle() {
		return ConfigurationWizardMainPage.PROJECT_NAME |
			ConfigurationWizardMainPage.TAG;
	}
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		getMainPage().finish(new NullProgressMonitor());
		return true;	
	}
}

