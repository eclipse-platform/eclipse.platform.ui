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
	 */
	ISite getSite();
	
	/**
	 * returns the policy for this configuration Site
	 */
	IConfigurationPolicy getConfigurationPolicy();
	
	/**
	 * Sets a new policy. The configured features are recaculated
	 */
	void setConfigurationPolicy(IConfigurationPolicy policy);
	
	/**
	 * Returns true if features can be installed in this Site
	 */
	boolean isInstallSite();
	
	/**
	 * 
	 * @param feature the Feature to install
	 * @param installConfiguration the configuration to modify
	 * @param monitor the Progress Monitor
	 */
	void install(IFeature feature, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Configure the Feature to be available at next startup
	 */
	void configure(IFeatureReference feature) throws CoreException;
	
	/**
	 * Unconfigure the feature from the execution path
	 */
	void unconfigure(IFeatureReference feature) throws CoreException;
	
	/**
	 * sets if the site is an installable site
	 */
	void setInstallSite(boolean installable);
	
	/**
	 * returns the feature used in this configurationSite
	 * This is a subset of the feature of teh site
	 */
	IFeatureReference[] getConfiguredFeatures();
	
}

