package org.eclipse.update.core;

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
	 */
	public boolean isCurrent();
	
	/**
	 * Returns an array of features accessible through this configuration.
	 * 
	 * @return IFeatureReference[] accessible features. Returns an empty array
	 * if there are no accessible features
	 */
	public IFeatureReference[] getFeatures();
	
	/**
	 * Returns an array of local install sites that can be used as 
	 * targets for installing local features. The sites
	 * must be read-write accessible from the current client, otherwise
	 * subsequent installation attampts will fail.
	 * 
	 * @return ISite[] local install sites. Returns an empty array
	 * if there are no local install sites
	 */
	public ISite[] getInstallSites();
	
	/**
	 * Adds an additional local install site to this configuration.
	 * The site must be read-write accessible from the current
	 * client, otherwise subsequent installation
	 * attempts will fail. Note, that this method does not verify
	 * the site is read-write accessible.
	 * 
	 * @param site local install site
	 */
	public void addInstallSite(ISite site);
	
	/**
	 * Removes a local install site from this configuration.
	 * 
	 * @param site local install site
	 */
	public void removeInstallSite(ISite site);
	
	/**
	 * Returns an array of sites (generally read-only) used for accessing
	 * additional features
	 * 
	 * @return ISite[] array of linked sites. Returns an empty array
	 * if there are no linked sites
	 */
	public ISite[] getLinkedSites();
	
	/**
	 * Adds an additional linked site to this configuration
	 * 
	 * @param site linked site
	 */
	public void addLinkedSite(ISite site);
	
	/**
	 * Removes a linked site from this configuration
	 * 
	 * @param site linked site
	 */
	public void removeLinkedSite(ISite site);	
	

}

