package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Installation configuration.
 * Represents a specific configuration of a number of sites as a point
 * in time. Maintains a record of the specific activities that resulted
 * in this configuration. Current installation configuration is
 * the configuration the platform was started with.
 */
public interface IInstallConfiguration extends IAdaptable {

	/**
	 * Indicates if this is the current configuration
	 * 
	 * @return <code>true</code> if this is the current configuration,
	 * <code>false</code> otherwise
	 * @since 2.0 
	 */
	public boolean isCurrent();

	/**
	 * Return the sites that are part of this configuration.
	 * 
	 * @return an array of configured sites, or an empty array.
	 * @since 2.0 
	 */
	public IConfiguredSite[] getConfiguredSites();

	/**
	 * Create a new installation site, based on a local file 
	 * system directory. Note, the site is not added to the
	 * configuration as a result of this call.
	 * 
	 * @param directory file directory
	 * @return new site
	 * @exception CoreException
	 * @since 2.0 
	 */
	public IConfiguredSite createConfiguredSite(File directory)
		throws CoreException;

	/**
	 * Create a new linked site, based on a local file 
	 * system directory. Note, the site is not added to the
	 * configuration as a result of this call.
	 * The linked site is only created if the directory is an
	 * already existing extension site and if it is not already
	 * natively linked to teh local site.
	 * 
	 * @param directory file directory
	 * @return new linked site
	 * @exception CoreException
	 * @since 2.0 
	 */
	public IConfiguredSite createLinkedConfiguredSite(File directory)
		throws CoreException;

	/**
	 * Adds the specified site to this configuration.
	 * 
	 * @param site new site
	 * @since 2.0 
	 */
	public void addConfiguredSite(IConfiguredSite site);

	/**
	 * Removes the specified site from this configuration.
	 * 
	 * @param site site to remove
	 * @since 2.0 
	 */
	public void removeConfiguredSite(IConfiguredSite site);

	/**
	 * Adds a configuration change listener.
	 * 
	 * @param listener the listener
	 * @since 2.0 
	 */
	public void addInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener);

	/**
	 * Removes a configuration change listener.
	 * 
	 * @param listener the listener
	 * @since 2.0 
	 */
	public void removeInstallConfigurationChangedListener(IInstallConfigurationChangedListener listener);

	/**
	 * Return the list of activities that resulted in this configuration.
	 * There is always at least one activity
	 * 
	 * @return an array of activities
	 * @since 2.0 
	 */
	public IActivity[] getActivities();

	/**
	 * Retrun the date the configuration was created.
	 * 
	 * @return create date
	 * @since 2.0 
	 */
	public Date getCreationDate();

	/**
	 * Return the configuration label.
	 * 
	 * @return the configuration label. If the configuration label was not
	 * explicitly set, a default label is generated based on the creation
	 * date
	 * @since 2.0 
	 */
	public String getLabel();

	/**
	 * Sets the configuration label.
	 * 
	 * @param label the label
	 * @since 2.0 
	 */
	public void setLabel(String label);

}