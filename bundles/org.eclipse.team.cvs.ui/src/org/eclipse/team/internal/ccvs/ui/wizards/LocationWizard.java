package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * This wizard helps the user to add a new repository location
 * to the repositories view
 */
public class LocationWizard extends ConnectionWizard {
	protected String getMainPageDescription() {
		return Policy.bind("LocationWizard.description");
	}
	protected String getMainPageTitle() {
		return Policy.bind("LocationWizard.title");
	}
	protected int getStyle() {
		return ConfigurationWizardMainPage.CONNECTION_METHOD |
			ConfigurationWizardMainPage.USER |
			ConfigurationWizardMainPage.PASSWORD |
			ConfigurationWizardMainPage.PORT |
			ConfigurationWizardMainPage.HOST |
			ConfigurationWizardMainPage.REPOSITORY_PATH;
	}
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		getMainPage().finish(new NullProgressMonitor());
		return true;	
	}
}

