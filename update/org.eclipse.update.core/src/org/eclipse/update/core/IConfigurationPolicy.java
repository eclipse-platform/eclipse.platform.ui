package org.eclipse.update.core;

import java.util.Date;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IConfigurationPolicy {
	/**
	 * return the policy used
	 * @see org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy
	 */
	int getPolicy();
	
	/**
	 * return the list of filtered features based on the policy (include/exclude list)
	 * @see org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy#getList()
	 */
	IFeatureReference[] getFilteredFeatures(IFeatureReference[] featuresToFilter);
	
		/**
	 * Returns teh configured features for this Site based on the current policy
	 */
	IFeatureReference[] getConfiguredFeatures();
	
	/**
	 * returns <code>true</code> if the feature is configured for this Site
	 */
	boolean isConfigured(IFeatureReference feature);
	
	/**
	 * Configure the Feature to be available at next startup
	 */
	void configure(IFeatureReference feature);
	
	/**
	 * Unconfigure the feature from the execution path
	 */
	void unconfigure(IFeatureReference feature);

}

