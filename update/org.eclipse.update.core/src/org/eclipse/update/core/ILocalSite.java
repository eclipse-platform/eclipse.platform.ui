package org.eclipse.update.core;

import java.io.File;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Interface defining the behavior of a local site. Local site is
 * a reflection of a user installation configuration. It is a collection of
 * <ul>
 * <li>zero or more local installation directories. These can be used
 * as sites to locally install additional features
 * <li>zero or more linked installation directories. These are used
 * as sites to access additional features. In general they are read-only
 * <li>configuration information specifying which features are actually
 * configured for use on this local site (a subset of features found
 * on local installation sites and linked sites)
 * </ul>
 */ 
public interface ILocalSite {
	
	public static final int DEFAULT_HISTORY = 5;
	
	
	/**
	 * return the label of the local site
	 * 
	 * @return teh label
	 * @since 2.0 
	 */

	String getLabel();
	

	/**
	 * Return the current configuration object.
	 * This is the Configuration that will be saved.
	 * 
	 * @return IInstallConfiguration
	 * @since 2.0 
	 */
	
	IInstallConfiguration getCurrentConfiguration();
	
	/**
	 * Returns an array of configuration objects representing the local
	 * site change history. The current configuration is part of the history.
	 * 
	 * @return IInstallConfiguration[] configuration history. Returns
	 * an empty array is there is no history
	 * @since 2.0 
	 */

	IInstallConfiguration [] getConfigurationHistory();
	
	/**
	 * Reverts the Current Configuration to an old configuration.
	 * 
	 * Creates a new configuration based on the old one
	 * and calculate the delta between the old configuration and the current one
	 * Then set the newly created configuration as the current one
	 * 
	 * @param IInstallConfiguration the configuration to use
	 * @since 2.0 
	 */

	void revertTo(IInstallConfiguration configuration, IProgressMonitor monitor,IProblemHandler handler) throws CoreException;
	
	/**
	 * Creates a configuration from a URL.
	 * The configuration is not added to the LocalSite
	 * @since 2.0 
	 */

	IInstallConfiguration importConfiguration(URL importURL,String label) throws CoreException;


	/**
	 * creates a new currentConfiguration based on the current configuration
	 * The newly created configuration is NOT added to the local site
	 * 
	 * ILocalSite site = SiteManager.getLocalSite();
	 * 
	 * The following line creates a new current configuration in the local site
	 * IInstallConfiguration currentConfig = site.createNewCurrentConfiguration(null,"new Label"); 
	 * IConfigurationSite configSite = -obtain a configuration site from the InstallConfiguration-
	 * configSite.install(IFeature,IProgressMonitor);
	 * 
	 * the following line saves the state of the configuration
	 * currentConfig.save();
	 * 
	 * If <code>name</code> is <code>null</code> we'll create a name based on the creation date
	 * if <code>newFile</code> is <code>null</code> we'll create a new file based on the creation date 
	 * @since 2.0 
	 */

	IInstallConfiguration cloneCurrentConfiguration(URL newFile,String name) throws CoreException;

	/**
	 * Adds a new configuration to the LocalSite
	 * The new configuration becomes the current one
	 * @since 2.0 
	 */

	void addConfiguration(IInstallConfiguration config);
	
	/**
	 * Saves and persists the localSite. Also saves and persists the current Configuration
	 * @since 2.0 
	 */

	void save() throws CoreException;
	
	/**
	 * returns the maximum number of InstallConfiguration in teh history
	 * @since 2.0 
	 */

	int getMaximumHistory();
	
	/**
	 * sets the maximum InstallConfiguration of the history
	 * @since 2.0 
	 */

	void setMaximumHistory(int history);
	
	
	/**
	 * @since 2.0 
	 */
	void addLocalSiteChangedListener(ILocalSiteChangedListener listener);

	/**
	 * @since 2.0 
	 */
	void removeLocalSiteChangedListener(ILocalSiteChangedListener listener);
	

	/**
	 * @since 2.0 
	 */
	void addToPreservedConfigurations(IInstallConfiguration configuration) throws CoreException;

	/**
	 * @since 2.0 
	 */
	void removeFromPreservedConfigurations(IInstallConfiguration configuration);

	/**
	 * @since 2.0 
	 */	
	IInstallConfiguration[] getPreservedConfigurations();
	
	/**
	 * @since 2.0 
	 */	
	IInstallConfiguration getPreservedConfigurationFor(IInstallConfiguration configuration);

	/**
	 * Returns a list of PluginEntries that are not used by any other configured feature
	 * @since 2.0
	 */
	public IPluginEntry[] getUnusedPluginEntries(IFeature feature) throws CoreException;

}

