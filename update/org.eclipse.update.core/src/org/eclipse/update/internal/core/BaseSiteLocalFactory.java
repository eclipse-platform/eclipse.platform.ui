package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.update.core.model.*;
import org.eclipse.update.core.model.ConfigurationActivityModel;
import org.eclipse.update.core.model.InstallConfigurationModel;

/**
 * 
 */

public class BaseSiteLocalFactory {


	/*
	 * 
	 */
	public InstallConfigurationModel createInstallConfigurationModel() {
		return new InstallConfiguration();
	}
	/*
	 * 
	 */
	public ConfigurationActivityModel createConfigurationAcivityModel() {
		return new ConfigurationActivity();
	}

	/*
	 * 
	 */
	public ConfigurationSiteModel createConfigurationSiteModel() {
		return new ConfigurationSite();
	}

	/*
	 * 
	 */
	public ConfigurationPolicyModel createConfigurationPolicyModel() {
		return new ConfigurationPolicy();
	}


	/**
	 * 
	 */
	public ConfigurationSiteModel createConfigurationSiteModel(SiteMapModel site, int policy){
		//create config site
		ConfigurationSiteModel configSite = this.createConfigurationSiteModel();
		configSite.setSiteModel(site);
				
		ConfigurationPolicyModel policyModel = this.createConfigurationPolicyModel(); 
		policyModel.setPolicy(policy);
		configSite.setConfigurationPolicyModel(policyModel);		
		
		return configSite;
	}

}
