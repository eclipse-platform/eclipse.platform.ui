package org.eclipse.update.core;

import java.io.File;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;

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
	
	
	/**
	 * return the label of the local site
	 * 
	 * @return teh label
	 */
	String getLabel();
	

	/**
	 * Return the current configuration object.
	 * This is the Configuration that will be saved.
	 * 
	 * @return IInstallConfiguration
	 */	
	IInstallConfiguration getCurrentConfiguration();
	
	/**
	 * Returns an array of configuration objects representing the local
	 * site change history. The current configuration is part of the history.
	 * 
	 * @return IInstallConfiguration[] configuration history. Returns
	 * an empty array is there is no history
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
	 */
	void revertTo(IInstallConfiguration configuration) throws CoreException;
	
	/**
	 * Creates a configuration from a URL.
	 * The configuration is not added to the LocalSite
	 */
	IInstallConfiguration importConfiguration(URL importURL,String label) throws CoreException;
	
	/**
	 * @deprecated use createNewCurrentConfiguration(URL,String);
	 */
	IInstallConfiguration createConfiguration(URL newFile,String name) throws CoreException;

	/**
	 * creates a new currentConfiguration based on the current configuration
	 * The newly created configuration is added to the local site
	 * 
	 * If <code>name</code> is <code>null</code> we'll create a name based on the creation date
	 * if <code>newFile</code> is <code>null</code> we'll create a new file based on the creation date 
	 */
	IInstallConfiguration createNewCurrentConfiguration(URL newFile,String name) throws CoreException;
	

	/**
	 * Adds a new configuration to the LocalSite
	 * The new configuration becomes the current one
	 */
	void addConfiguration(IInstallConfiguration config);
	
	/**
	 * Saves and persists the localSite. Also saves and persists the current Configuration
	 */
	void save() throws CoreException;
	
	
	void addLocalSiteChangedListener(ISiteLocalChangedListener listener);
	void removeLocalSiteChangedListener(ISiteLocalChangedListener listener);

}

