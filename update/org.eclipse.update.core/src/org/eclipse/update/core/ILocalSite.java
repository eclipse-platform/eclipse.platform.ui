package org.eclipse.update.core;

import java.io.File;

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
	 * return the label of teh local site
	 * 
	 * @return teh label
	 */
	String getLabel();
	

	/**
	 * Return the current configuration object
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
	 * Reverts the Current Configuration to the 
	 * 
	 * @param IInstallConfiguration the configuration to use
	 */
	void setCurrentConfiguration(IInstallConfiguration configuration);
	
	/**
	 * Creates a configuration from a File
	 */
	IInstallConfiguration importConfiguration(File importFile);
	
	void addLocalSiteChangedListener(ISiteLocalChangedListener listener);
	void removeLocalSiteChangedListener(ISiteLocalChangedListener listener);

}

