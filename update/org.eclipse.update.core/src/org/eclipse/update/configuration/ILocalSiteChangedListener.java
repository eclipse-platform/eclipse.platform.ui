package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

/**
 * Local site change listener.
 * 
 * @since 2.0
 */
public interface ILocalSiteChangedListener {
	
	/**
	 * Indicates the current configuration has changed.
	 * 
	 * @param configuration the current cunfiguration
	 * @since 2.0 
	 */
	public void currentInstallConfigurationChanged(IInstallConfiguration configuration);
	
	/**
	 * Indicates the specified configuration was removed.
	 * 
	 * @param configuration the configuration
	 * @since 2.0 
	 */
	public void installConfigurationRemoved(IInstallConfiguration configuration);
}