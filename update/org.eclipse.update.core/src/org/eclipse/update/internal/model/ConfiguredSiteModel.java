package org.eclipse.update.internal.model;
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
import org.eclipse.update.core.model.*;

/**
 * 
 */
public class ConfiguredSiteModel extends ModelObject {
	private String[] previousPluginPath;

	private SiteModel site;
	private String platformURLString;
	private ConfigurationPolicyModel policy;
	private InstallConfigurationModel installConfiguration;
	private boolean installable = false;

	/**
	 * Constructor
	 */
	public ConfiguredSiteModel() {
		super();
	}

	/**
	 * returns the site
	 * @return The ISite 
	 * @since 2.0
	 */
	public SiteModel getSiteModel() {
		return site;
	}

	/**
	 * Sets the site.
	 * @param site The site to set
	 */
	public void setSiteModel(SiteModel site) {
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
	public boolean isUpdatable() {
		return installable;
	}

	/**
	 * @since 2.0
	 */
	public void isUpdatable(boolean installable) {
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
	 * Gets the previousPluginPath. The list of plugins the platform had.
	 * @return Returns a String[]
	 */
	public String[] getPreviousPluginPath() {
		if (previousPluginPath == null)
			previousPluginPath = new String[0];
		return previousPluginPath;
	}

	/**
	 * Sets the previousPluginPath.
	 * @param previousPluginPath The previousPluginPath to set
	 */
	public void setPreviousPluginPath(String[] previousPluginPath) {
		this.previousPluginPath = new String[previousPluginPath.length];
		System.arraycopy(previousPluginPath, 0, this.previousPluginPath, 0, previousPluginPath.length);
	}

}