package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Configuration change listener.
 */
public interface IInstallConfigurationChangedListener {
	
	/**
	 * Indicates the specified site was added to the configuration
	 * 
	 * @param site the site
	 * @since 2.0 
	 */
	void installSiteAdded(IConfiguredSite site);
	
	/**
	 * Indicates the specified site was removed from the configuration
	 * 
	 * @param site the site
	 * @since 2.0 
	 */
	void installSiteRemoved(IConfiguredSite site);

}