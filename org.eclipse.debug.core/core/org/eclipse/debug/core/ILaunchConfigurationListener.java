package org.eclipse.debug.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Notified when a lanuch configuration is created,
 * deleted, or changed.
 */
public interface ILaunchConfigurationListener {
	
	/**
	 * The given launch configuration has been created.
	 * 
	 * @param configuration the newly created launch configuration
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration);
	
	/**
	 * The given launch configuration has changed in some way.
	 * The configuration may be a working copy.
	 * 
	 * @param configuration the launch configuration that has
	 *  changed
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration);
	
	/**
	 * The given launch configuration has been deleted.
	 * 
	 * @param configuration the deleted launch configuration
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration);	
}

