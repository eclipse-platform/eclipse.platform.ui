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
public class ConfigurationSiteAdapter
	extends UIModelObject
	implements IConfiguredSiteAdapter {
	private IInstallConfiguration config;
	private IConfiguredSite csite;
		
	public ConfigurationSiteAdapter(IInstallConfiguration config, IConfiguredSite csite) {
		this.csite = csite;
		this.config = config;
	}
	
	public IConfiguredSite getConfigurationSite() {
		return csite;
	}

	public IInstallConfiguration getInstallConfiguration() {
		return config;
	}
}
