package org.eclipse.update.core;



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
	IConfigurationPolicy getPolicy();
	
	/**
	 * Sets a new policy. The configured features are recaculated
	 */
	void setPolicy(IConfigurationPolicy policy);
	
	/**
	 * Returns teh configured features for this Site based on the current policy
	 */
	IFeatureReference[] getConfiguredFeatures();
	
	/**
	 * returns <code>true</code> if the feature is configured for this Site
	 */
	boolean isConfigured(IFeatureReference feature);
	
	/**
	 * Returns true if features can be installed in this Site
	 */
	boolean isInstallSite();
	
	/**
	 * Configure the Feature to be available at next startup
	 */
	void configure(IFeatureReference feature);
	
	/**
	 * Unconfigure the feature from the execution path
	 */
	void unconfigure(IFeatureReference feature);
	
}

