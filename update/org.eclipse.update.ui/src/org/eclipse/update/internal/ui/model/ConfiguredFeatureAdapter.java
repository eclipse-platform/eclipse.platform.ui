/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;

/**
 * @version 	1.0
 * @author
 */
public class ConfiguredFeatureAdapter
	extends SimpleFeatureAdapter
	implements IConfiguredFeatureAdapter {
	private IConfiguredSiteAdapter adapter;
	private boolean configured;

	public ConfiguredFeatureAdapter(
		IConfiguredSiteAdapter adapter,
		IFeature feature,
		boolean configured) {
		super(feature);
		this.adapter = adapter;
		this.configured = configured;
	}

	public IConfiguredSite getConfigurationSite() {
		return adapter.getConfigurationSite();
	}
	public IInstallConfiguration getInstallConfiguration() {
		return adapter.getInstallConfiguration();
	}
	public boolean isConfigured() {
		return configured;
	}
	public IFeatureAdapter[] getIncludedFeatures() {
		try {
			IFeatureReference[] included = getFeature().getIncludedFeatureReferences();
			ConfiguredFeatureAdapter[] result =
				new ConfiguredFeatureAdapter[included.length];
			for (int i = 0; i < included.length; i++) {
				result[i] =
					new ConfiguredFeatureAdapter(adapter, included[i].getFeature(), configured);
				result[i].setIncluded(true);
			}
			return result;
		} catch (CoreException e) {
			return new IFeatureAdapter[0];
		}
	}
}