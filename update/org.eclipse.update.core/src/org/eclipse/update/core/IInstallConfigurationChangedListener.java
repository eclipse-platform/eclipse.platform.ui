package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IInstallConfigurationChangedListener {
	void installSiteAdded(ISite site);
	void installSiteRemoved(ISite site);
	void linkedSiteAdded(ISite site);
	void linkedSiteRemoved(ISite site);
}	

