package org.eclipse.update.configuration;

import org.eclipse.update.configuration.*;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface ILocalSiteChangedListener {
	/**
	 * @since 2.0 
	 */
	void currentInstallConfigurationChanged(IInstallConfiguration configuration);
	/**
	 * @since 2.0 
	 */
	void installConfigurationRemoved(IInstallConfiguration configuration);
}