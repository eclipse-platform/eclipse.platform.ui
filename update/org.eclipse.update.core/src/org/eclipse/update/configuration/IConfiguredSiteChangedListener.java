package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.update.core.IFeature;

/**
 * Configuration change listener.
 * 
 * @since 2.0
 */
public interface IConfiguredSiteChangedListener {
	
	/**
	 * Indicates the specified feature was installed.
	 * 
	 * @param feature the feature
	 * @since 2.0 
	 */
	public void featureInstalled(IFeature feature);
	
	/**
	 * Indicates the specified feature was removed (uninstalled)
	 * 
	 * @param feature the feature
	 * @since 2.0 
	 */
	public void featureRemoved(IFeature feature);
	
	/**
	 * Indicates the specified feature was configured.
	 * 
	 * @param feature the feature
	 * @since 2.0 
	 */
	public void featureConfigured(IFeature feature);
	
	/**
	 * Indicates the specified feature was unconfigured.
	 * 
	 * @param feature the feature
	 * @since 2.0 
	 */
	public void featureUnconfigured(IFeature feature);
}

