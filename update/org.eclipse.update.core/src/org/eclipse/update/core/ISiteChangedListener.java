package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
public interface ISiteChangedListener {
	/**
	 * @since 2.0 
	 */
	void featureUpdated(IFeature feature);
	/**
	 * @since 2.0 
	 */
	void featureInstalled(IFeature feature);
	/**
	 * @since 2.0 
	 */
	void featureUninstalled(IFeature feature);
}

