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
	/**
	 * @deprecated should use getConfigurationSite().getSite()
	 */
	void featureAdded(IConfigurationSite site,IFeature feature);
	/**
	 * @deprecated should use getConfigurationSite().getSite()
	 */
	void featureRemoved(IConfigurationSite site, IFeature feature);
}	

