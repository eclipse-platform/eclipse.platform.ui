package org.eclipse.update.configuration;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.net.URL;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.*;


 
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
public interface ILocalSite extends IAdaptable {
	

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
	 * creates a new currentConfiguration based on the current configuration
	 * The newly created configuration is NOT added to the local site
	 * 
	 * ILocalSite site = SiteManager.getLocalSite();
	 * 
	 * The following line creates a new current configuration in the local site
	 * IInstallConfiguration currentConfig = site.createNewCurrentConfiguration(null,"new Label"); 
	 * IConfiguredSite configSite = -obtain a configuration site from the InstallConfigurationModel-
	 * configSite.install(IFeature,IProgressMonitor);
	 * 
	 * the following line saves the state of the configuration
	 * currentConfig.save();
	 * 
	 * @since 2.0 
	 */

	IInstallConfiguration cloneCurrentConfiguration() throws CoreException;

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
	 * returns the maximum number of InstallConfigurationModel in teh history
	 * @since 2.0 
	 */

	int getMaximumHistoryCount();
	
	/**
	 * sets the maximum InstallConfigurationModel of the history
	 * @since 2.0 
	 */

	void setMaximumHistoryCount(int history);
	
	
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
	IInstallConfiguration findPreservedConfigurationFor(IInstallConfiguration configuration);

	
}

