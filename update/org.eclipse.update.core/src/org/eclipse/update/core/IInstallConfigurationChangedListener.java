package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IInstallConfigurationChangedListener {
	/**
	 * @since 2.0 
	 */
	void installSiteAdded(IConfigurationSite site);
	/**
	 * @since 2.0 
	 */
	void installSiteRemoved(IConfigurationSite site);
	/**
	 * @since 2.0 
	 */
	void linkedSiteAdded(IConfigurationSite site);
	/**
	 * @since 2.0 
	 */
	void linkedSiteRemoved(IConfigurationSite site);
}	

