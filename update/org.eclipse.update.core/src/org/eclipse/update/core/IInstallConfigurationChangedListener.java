package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IInstallConfigurationChangedListener {
	void installSiteAdded(IConfigurationSite site);
	void installSiteRemoved(IConfigurationSite site);
	void linkedSiteAdded(IConfigurationSite site);
	void linkedSiteRemoved(IConfigurationSite site);
	void featureAdded(IConfigurationSite site,IFeature feature);
	void featureRemoved(IConfigurationSite site, IFeature feature);
}	

