package org.eclipse.update.core;

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
public interface ILocalSite extends ISite {
	// FIXME: VK: post pass 1 ILocalSite will not extend ISite. Instead,
	//            the caller will need to get the actual r/w ISite(s)
	//            by calling getInstallSites()
	
	/**
	 * Return the current configuration object
	 * 
	 * @return IInstallConfiguration
	 */	
	public IInstallConfiguration getCurrentConfiguration();
	
	/**
	 * Returns an array of configuration objects representing the local
	 * site change history
	 * 
	 * @return IInstallConfiguration[] configuration history. Returns
	 * an empty array is there is no history
	 */
	public IInstallConfiguration [] getConfigurationHistory();
	
	/**
	 * Returns an array of features accessible through the local site. This
	 * is a composite operation across the components of the local site
	 * 
	 * @return IFeature[] accessible features. Returns an empty array
	 * if there are no accessible features
	 */
	public IFeature[] getFeatures();
	
	/**
	 * Returns an array of local install sites that can be used as 
	 * targets for installing additional local features. The sites
	 * must be read-write accessible from the current client, otherwise
	 * subsequent installation attampts will fail.
	 * 
	 * @return ISite[] local install sites. Returns an empty array
	 * if there are no local install sites
	 */
	public ISite[] getInstallSites();
	
	/**
	 * Adds an additional local install site. The site must be read-write
	 * accessible from the current client, otherwise subsequent installation
	 * attempts will fail. Note, that this method does not verify
	 * the site is read-write accessible.
	 * 
	 * @param site local install site
	 */
	public void addInstallSite(ISite site);
	
	/**
	 * Removes a local install site if it is configured.
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
	 * Adds an additional linked site
	 * 
	 * @param site linked site
	 */
	public void addLinkedSite(ISite site);
	
	/**
	 * Removes a linked site if it is configured.
	 * 
	 * @param site linked site
	 */
	public void removeLinkedSite(ISite site);	
	
}

