package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.update.core.model.ConfigurationActivityModel;
import org.eclipse.update.core.model.InstallConfigurationModel;

/**
 * 
 */

public class BaseSiteLocalFactory {


	/*
	 * @see SiteModelFactory#createSiteMapModel()
	 */
	public InstallConfigurationModel createInstallConfigurationModel() {
		return new InstallConfiguration();
	}
	/*
	 * @see SiteModelFactory#createFeatureReferenceModel()
	 */
	public ConfigurationActivityModel createConfigurationAcivityModel() {
		return new ConfigurationActivity();
	}

}
