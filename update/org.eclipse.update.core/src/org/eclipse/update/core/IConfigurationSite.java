package org.eclipse.update.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;



/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Interface defining the configuration of a site.
 * 
 * The SiteConfguration reflects the policy used on a site.
 * It also returns if you can write in this site or not
 */ 
public interface IConfigurationSite {
	
	/**
	 * Returns the Site 
	 * @since 2.0 
	 */

	ISite getSite();

	/**
	 * Returns true if features can be installed in this Site
	 * @since 2.0 
	 */

	boolean isInstallSite();

	/**
	 * sets if the site is an installable site
	 * @since 2.0 
	 */

	void setInstallSite(boolean installable);


	
	/**
	 * returns the policy for this configuration Site
	 * @since 2.0 
	 */

	IConfigurationPolicy getConfigurationPolicy();
	
	/**
	 * Sets a new policy. The configured features are recaculated
	 * @since 2.0 
	 */

	void setConfigurationPolicy(IConfigurationPolicy policy);
	
		
	/**
	 * 
	 * @param feature the Feature to install
	 * @param monitor the Progress Monitor
	 * @since 2.0 
	 */

	IFeatureReference install(IFeature feature, IProgressMonitor monitor) throws CoreException;

	/**
	 * 
	 * @param feature the Feature to remove
	 * @param monitor the Progress Monitor
	 * @since 2.0 
	 */

	void remove(IFeature feature, IProgressMonitor monitor) throws CoreException;
		
	/**
	 * Configure the DefaultFeature to be available at next startup
	 * @since 2.0 
	 */

	void configure(IFeatureReference feature) throws CoreException;
	
	/**
	 * Unconfigure the feature from the execution path
	 * @since 2.0 
	 */

	void unconfigure(IFeatureReference feature,IProblemHandler handler) throws CoreException;
	
		
	/**
	 * returns the feature used in this configurationSite
	 * This is a subset of the feature of teh site
	 * @since 2.0 
	 */

	IFeatureReference[] getConfiguredFeatures();
	
	/**
	 * returns the InstallConfiguration this Configuration Site is part of
	 * @since 2.0
	 */
	IInstallConfiguration getInstallConfiguration();
	
}

