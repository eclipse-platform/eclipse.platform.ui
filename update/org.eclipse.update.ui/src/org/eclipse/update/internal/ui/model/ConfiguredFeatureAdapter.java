/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.model;

import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.core.*;

/**
 * @version 	1.0
 * @author
 */
public class ConfiguredFeatureAdapter extends SimpleFeatureAdapter implements IConfiguredFeatureAdapter {
	private IConfigurationSiteAdapter adapter;
	private boolean configured;
	
	public ConfiguredFeatureAdapter(IConfigurationSiteAdapter adapter, IFeature feature, boolean configured) {
		super(feature);
		this.adapter = adapter;
		this.configured = configured;
	}
	
	public IConfigurationSite getConfigurationSite() {
		return adapter.getConfigurationSite();
	}
	public IInstallConfiguration getInstallConfiguration() {
		return adapter.getInstallConfiguration();
	}
	public boolean isConfigured() {
		return configured;
	}
}
