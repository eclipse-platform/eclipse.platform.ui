package org.eclipse.update.configuration;

import java.io.File;
import java.net.URL;
import java.util.Date;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.update.configuration.*;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Installation configuration object.
 */
public interface IInstallConfiguration extends IAdaptable {
		
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
	 * @return IConfiguredSite[] local install sites. Returns an empty array
	 * if there are no local install sites
	 * @since 2.0 
	 */

	public IConfiguredSite[] getConfiguredSites();
	
	/**
	 * Creates a new site, based on a local file system directory,
	 * as a potential target for installation actions.
	 * 
	 * @exception CoreException
	 * @since 2.0 
	 */
	public IConfiguredSite createConfiguredSite(File directory) throws CoreException;
		
	/**
	 * Adds an additional configuration site to this configuration.
	 * 
	 * @param site configuration site
	 * @since 2.0 
	 */

	public void addConfiguredSite(IConfiguredSite site);
	
	/**
	 * Removes a configuration site from this configuration.
	 * 
	 * @param site configuration site
	 * @since 2.0 
	 */

	public void removeConfiguredSite(IConfiguredSite site);
	
	
	/**
	 * @since 2.0 
	 */
	void addInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener);
	/**
	 * @since 2.0 
	 */
	void removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener);
	
	
	/**
	 * Returns the Activities that were performed to get this InstallConfigurationModel.
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

