package org.eclipse.update.core;

import java.util.Date;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IConfigurationPolicy {
	int getPolicy();
	IFeatureReference[] getFilteredFeatures(IFeatureReference[] features);
}

