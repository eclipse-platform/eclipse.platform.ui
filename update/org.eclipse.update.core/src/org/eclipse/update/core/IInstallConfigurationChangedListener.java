package org.eclipse.update.core;

import org.eclipse.update.configuration.*;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IInstallConfigurationChangedListener {
	/**
	 * @since 2.0 
	 */
	void installSiteAdded(IConfiguredSite site);
	/**
	 * @since 2.0 
	 */
	void installSiteRemoved(IConfiguredSite site);
	/**
	 * @since 2.0 
	 */
	void linkedSiteAdded(IConfiguredSite site);
	/**
	 * @since 2.0 
	 */
	void linkedSiteRemoved(IConfiguredSite site);
}	

