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
	 * @since 2.0 
	 */

	public boolean isCurrent();
	
	/**
	 *  Change the 
	 * 
	 * @since 2.0 
	 */
	//FIXME : javadoc
	void setCurrent(boolean isCurrent);
	
	
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
	 * @since 2.0 
	 */

	public IConfigurationSite[] getConfigurationSites();
	
	/**
	 * Adds an additional configuration site to this configuration.
	 * 
	 * @param site configuration site
	 * @since 2.0 
	 */

	public void addConfigurationSite(IConfigurationSite site);
	
	/**
	 * Removes a configuration site from this configuration.
	 * 
	 * @param site configuration site
	 * @since 2.0 
	 */

	public void removeConfigurationSite(IConfigurationSite site);
	
	
	/**
	 * @since 2.0 
	 */
	void addInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener);
	/**
	 * @since 2.0 
	 */
	void removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener);
	
	/**
	 * Export the configuration to a file
	 * @since 2.0 
	 */

	void export(File exportFile) throws CoreException;
	
	/**
	 * Returns the Activities that were performed to get this InstallConfiguration.
	 * 
	 * There is always at least one Activity
	 * 
	 * 
	 * @since 2.0 
	 */

	IActivity[] getActivities();
	
	/**
	 * retruns the Date at which the Configuration was created
	 * The date is the local date from the machine that created the Configuration.
	 * @since 2.0 
	 */

	Date getCreationDate();
	
	/**
	 * returns the URL of where the configuration is declared
	 * The URL points to teh exact XML file
	 * @since 2.0 
	 */

	URL getURL();
	
	/**
	 * returns the label of the configuration
	 * @since 2.0 
	 */

	String getLabel();

	/**
	 * sets the label of the configuration
	 * @since 2.0 
	 */

	void setLabel(String label);

}

