package org.eclipse.update.core;

import java.io.File;
import java.net.URL;
import java.util.Date;
import org.eclipse.core.runtime.CoreException;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Installation configuration object.
 */
public interface IInstallConfiguration {
		
	/**
	 * Returns <code>true</code> is this is the current configuration
	 * 
	 * @return boolean
	 */
	public boolean isCurrent();
	
	/**
	 *  Change the 
	 * 
	 */
	//FIXME : javadoc
	void setCurrent(boolean isCurrent);
	
	
	/**
	 * Returns an array of features configured through this configuration.
	 * 
	 * @return IFeatureReference[] configured features. Returns an empty array
	 * if there are no configured features
	 */
	public IFeatureReference[] getConfiguredFeatures();
	
	/**
	 * Returns an array of unconfigured features  through this configuration.
	 * 
	 * unconfigured Features are accessible by the user but will not be executed.
	 * 
	 * @return IFeatureReference[] unconfigured features. Returns an empty array
	 * if there are no unconfigured features
	 */
	public IFeatureReference[] getUnconfiguredFeatures();
	
	
	/**
	 * Returns an array of local configuration sites that the sites
	 * configured.
	 * 
	 * The sites can be pure link sites or install sites.
	 * 
	 *  The install sites
	 * must be read-write accessible from the current client, otherwise
	 * subsequent installation attampts will fail.
	 * 
	 * @return IConfigurationSite[] local install sites. Returns an empty array
	 * if there are no local install sites
	 */
	public IConfigurationSite[] getConfigurationSites();
	
	/**
	 * Adds an additional configuration site to this configuration.
	 * 
	 * @param site configuration site
	 */
	public void addConfigurationSite(IConfigurationSite site);
	
	/**
	 * Removes a configuration site from this configuration.
	 * 
	 * @param site configuration site
	 */
	public void removeConfigurationSite(IConfigurationSite site);
	
	
	void addInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener);
	void removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener);
	
	/**
	 * Export the configuration to a file
	 */
	void export(File exportFile) throws CoreException;
	
	/**
	 * Returns the Activities that were performed to get this InstallConfiguration.
	 * 
	 * There is always at least one Activity
	 * 
	 * 
	 */
	IActivity[] getActivities();
	
	/**
	 * retruns the Date at which the Configuration was created
	 * The date is the local date from the machine that created the Configuration.
	 */
	Date getCreationDate();
	
	/**
	 * returns the URL of where the configuration is declared
	 * The URL points to teh exact XML file
	 */
	URL getURL();
	
	/**
	 * returns the label of the configuration
	 */
	String getLabel();

}

