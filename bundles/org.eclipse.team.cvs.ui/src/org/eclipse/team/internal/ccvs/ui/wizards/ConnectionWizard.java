package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Properties;
import org.eclipse.team.internal.ccvs.ui.wizards.*;
import org.eclipse.team.internal.ccvs.ui.*;

/**
 * Abstract wizard which displays a configuration wizard main page
 * in some configuration, and performs some action on finish.
 */
public abstract class ConnectionWizard extends CVSWizard {
	// The main page.
	private ConfigurationWizardMainPage mainPage;

	private Properties properties;
	
	/**
	 * Creates the wizard pages
	 */
	public void addPages() {
		mainPage = new ConfigurationWizardMainPage("repositoryPage1", getMainPageTitle(), null);
		if (properties != null) {
			mainPage.setProperties(properties);
		}
		mainPage.setDescription(getMainPageDescription());
		mainPage.setStyle(getStyle());
		addPage(mainPage);
	}
	protected ConfigurationWizardMainPage getMainPage() {
		return mainPage;
	}
	protected abstract String getMainPageDescription();
	protected abstract String getMainPageTitle();
	protected abstract int getStyle();
	public Properties getProperties() {
		return mainPage.getProperties();
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}

