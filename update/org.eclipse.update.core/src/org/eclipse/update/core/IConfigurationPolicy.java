package org.eclipse.update.core;

import java.util.Date;
import org.eclipse.core.runtime.CoreException;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IConfigurationPolicy {
	/**
	 * return the policy used
	 * @see org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy
	 * @since 2.0 
	 */

	int getPolicy();
	
	/**
	 * return the list of filtered features based on the policy (include/exclude list)
	 * @see org.eclipse.core.boot.IPlatformConfiguration.ISitePolicy#getList()
	 * @since 2.0 
	 */

	IFeatureReference[] getFilteredFeatures(IFeatureReference[] featuresToFilter);
	
	/**
	 * Returns teh configured features for this Site based on the current policy
	 * @since 2.0 
	 */

	IFeatureReference[] getConfiguredFeatures();

	/**
	 * Returns teh features that should not be configured for this Site based on the current policy
	 * @since 2.0 
	 */

	IFeatureReference[] getUnconfiguredFeatures();
	
	/**
	 * returns <code>true</code> if the feature is configured for this Site
	 * @since 2.0 
	 */

	boolean isConfigured(IFeatureReference feature);

}

