package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
public interface CopyOfISiteChangedListener {
	void featureUpdated(IFeature feature);
	void featureInstalled(IFeature feature);
	void featureUninstalled(IFeature feature);
}

