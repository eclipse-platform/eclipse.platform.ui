/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.model;

import org.eclipse.update.core.*;

/**
 * @version 	1.0
 * @author
 */
public class ConfigurationSiteAdapter
	extends ModelObject
	implements IConfigurationSiteAdapter {
	private IInstallConfiguration config;
	private IConfigurationSite csite;
		
	public ConfigurationSiteAdapter(IInstallConfiguration config, IConfigurationSite csite) {
		this.csite = csite;
		this.config = config;
	}
	
	public IConfigurationSite getConfigurationSite() {
		return csite;
	}

	public IInstallConfiguration getInstallConfiguration() {
		return config;
	}
}
