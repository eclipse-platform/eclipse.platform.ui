package org.eclipse.update.configuration;

import org.eclipse.update.core.IFeature;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
public interface IConfiguredSiteChangedListener {
	/**
	 * @since 2.0 
	 */
	void featureInstalled(IFeature feature);
	/**
	 * @since 2.0 
	 */
	void featureUninstalled(IFeature feature);
}

