package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.PrintWriter;
import java.util.Date;

import java.util.*;

import org.eclipse.core.boot.IPlatformConfiguration;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IFeatureReference;

/**
 * 
 */
public class ConfigurationSiteModel extends ModelObject {

	private SiteMapModel site;
	private ConfigurationPolicyModel policy;
	private boolean installable = false;

	/**
	 * Constructor
	 */
	public ConfigurationSiteModel() {
		super();
	}

	/**
	 * Copy Constructor
	 */
	public ConfigurationSiteModel(ConfigurationSiteModel configSite) {
		this.site = configSite.getSiteModel();
		this.policy = new ConfigurationPolicyModel(configSite.getConfigurationPolicyModel());
		this.installable = configSite.isInstallSite();
	}

	/**
	 * returns the site
	 * @return The ISite 
	 * @since 2.0
	 */
	public SiteMapModel getSiteModel() {
		return site;
	}

	/**
	 * Sets the site.
	 * @param site The site to set
	 */
	public void setSiteModel(SiteMapModel site) {
		assertIsWriteable();
		this.site = site;
	}


	/**
	 * returns the policy
	 */
	public ConfigurationPolicyModel getConfigurationPolicyModel() {
		return policy;
	}

	/**
	 * 
	 * @since 2.0
	 */
	public void setConfigurationPolicyModel(ConfigurationPolicyModel policy) {
		assertIsWriteable();
		this.policy = policy;
	}

	/**
	 * @since
	 */
	public boolean isInstallSite() {
		return installable;
	}

	/**
	 * @since 2.0
	 */
	public void setInstallSite(boolean installable) {
		assertIsWriteable();
		this.installable = installable;
	}

	
}