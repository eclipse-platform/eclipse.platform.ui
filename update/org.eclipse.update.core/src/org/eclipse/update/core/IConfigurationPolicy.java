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

}

