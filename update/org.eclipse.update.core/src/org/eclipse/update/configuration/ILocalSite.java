package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;

/**
 * Local Site.
 * Represents the local installation. It consists of the current
 * configuration and the configuration history. A local site
 * manages the number of configuration histories kept. It also allows
 * specific configuration histories to be saved.
 * 
 * @since 2.0
 */
public interface ILocalSite extends IAdaptable {

	/**
	 * Return the current configuration.
	 * 
	 * @return current configuration
	 * @since 2.0 
	 */
	IInstallConfiguration getCurrentConfiguration();

	/**
	 * Return configuration history.
	 * 
	 * @return an array of configurations, or an empty array.
	 * @since 2.0 
	 */
	public IInstallConfiguration[] getConfigurationHistory();

	/**
	 * Reverts the local site to use the specified configuration.
	 * The result of this operation is a new configuration that
	 * contains the same configured features as the specified configuration.
	 * The new configuration becomes the current configuration.
	 * 
	 * @param configuration configuration state to revert to
	 * @param monitor progress monitor
	 * @param handler problem handler
	 * @exception CoreException
	 * @since 2.0 
	 */
	public void revertTo(
		IInstallConfiguration configuration,
		IProgressMonitor monitor,
		IProblemHandler handler)
		throws CoreException;

	/**
	 * Creates a new configuration containing the same state as the 
	 * specified configuration. The new configuration is not added to
	 * this lical site.
	 * 
	 * @return cloned configuration
	 * @exception CoreException
	 * @since 2.0 
	 */
	public IInstallConfiguration cloneCurrentConfiguration() throws CoreException;

	/**
	 * Adds the specified configuration to this local site.
	 * The new configuration becomes the current one.
	 * 
	 * @param config the configuration
	 * @since 2.0 
	 */
	public void addConfiguration(IInstallConfiguration config);

	/**
	 * Saves the local site state
	 * 
	 * @exception CoreException
	 * @since 2.0 
	 */
	public void save() throws CoreException;

	/**
	 * Indicates how many configuration histories should be maintained.
	 * Histories beyond the specified count are automatically deleted.
	 * 
	 * @return number of past configurations to keep as history
	 * @since 2.0 
	 */
	public int getMaximumHistoryCount();

	/**
	 * Sets the number of past configurations to keep in history
	 * 
	 * @param history number of configuration to keep
	 * @since 2.0 
	 */
	public void setMaximumHistoryCount(int history);

	/**
	 * Adds a site change listener
	 * 
	 * @param listener the listener
	 * @since 2.0 
	 */
	public void addLocalSiteChangedListener(ILocalSiteChangedListener listener);

	/**
	 * Removes a site listener
	 * 
	 * @param listener the listener
	 * @since 2.0 
	 */
	public void removeLocalSiteChangedListener(ILocalSiteChangedListener listener);

	/**
	 * Save the specified configuration. Saved configurations are 
	 * not deleted based on the history count. They must be explicitly
	 * removed.
	 * 
	 * @param configuration the configuration to save
	 * @exception CoreException
	 * @since 2.0 
	 */
	public void addToPreservedConfigurations(IInstallConfiguration configuration)
		throws CoreException;

	/**
	 * Removes the specified configuration from the list of previously
	 * saved configurations.
	 * 
	 * @param configuration the configuration to remove
	 * @since 2.0 
	 */
	public void removeFromPreservedConfigurations(IInstallConfiguration configuration);

	/**
	 * Return the list of saved configurations
	 * 
	 * @return an array of configurations, or an empty array.
	 * @since 2.0 
	 */
	public IInstallConfiguration[] getPreservedConfigurations();
	
	
	/**
	 * Prompt the user to configure or unconfigure
	 * newly discoverd features.
	 * @throws CoreException if an error occurs.
	 * @since 2.0
	 */
	public void displayUpdateChange() throws CoreException;
}