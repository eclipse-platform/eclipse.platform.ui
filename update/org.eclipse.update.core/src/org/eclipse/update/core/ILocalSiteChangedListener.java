package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ILocalSiteChangedListener {
	void currentInstallConfigurationChanged(IInstallConfiguration configuration);
	void installConfigurationRemoved(IInstallConfiguration configuration);
}