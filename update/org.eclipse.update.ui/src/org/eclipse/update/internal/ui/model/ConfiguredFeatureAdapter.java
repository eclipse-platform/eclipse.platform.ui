/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.model;

import org.eclipse.update.core.*;
import org.eclipse.update.configuration.*;

/**
 * @version 	1.0
 * @author
 */
public class ConfiguredFeatureAdapter extends SimpleFeatureAdapter implements IConfiguredFeatureAdapter {
	private IConfiguredSiteAdapter adapter;
	private boolean configured;
	
	public ConfiguredFeatureAdapter(IConfiguredSiteAdapter adapter, IFeature feature, boolean configured) {
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
}