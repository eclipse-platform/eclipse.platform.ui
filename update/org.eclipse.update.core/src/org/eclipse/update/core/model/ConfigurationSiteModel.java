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

	private boolean broken=false;
	private SiteMapModel site;
	private String platformURLString;
	private ConfigurationPolicyModel policy;
	private InstallConfigurationModel installConfiguration;
	private boolean installable = false;

	/**
	 * Constructor
	 */
	public ConfigurationSiteModel() {
		super();
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

	
	/**
	 * Gets the installConfiguration.
	 * @return Returns a InstallConfigurationModel
	 */
	public InstallConfigurationModel getInstallConfigurationModel() {
		return installConfiguration;
	}

	/**
	 * Sets the installConfiguration.
	 * @param installConfiguration The installConfiguration to set
	 */
	public void setInstallConfigurationModel(InstallConfigurationModel installConfiguration) {
		assertIsWriteable();
		this.installConfiguration = installConfiguration;
	}

	/**
	 * Gets the platformURLString.
	 * @return Returns a String
	 */
	public String getPlatformURLString() {
		return platformURLString;
	}

	/**
	 * Sets the platformURLString.
	 * @param platformURLString The platformURLString to set
	 */
	public void setPlatformURLString(String platformURLString) {
		this.platformURLString = platformURLString;
	}

	/**
	 * returns true if the Site is not accessible at this time.
	 * @return Returns a boolean
	 */
	public boolean isBroken() {
		return broken;
	}

	/**
	 * Sets the broken.
	 * @param broken The broken to set
	 */
	public void setBroken(boolean broken) {
		this.broken = broken;
	}

}