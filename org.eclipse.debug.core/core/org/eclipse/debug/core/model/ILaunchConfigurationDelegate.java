package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * A launch configuration delegate performs launching for a
 * specific type of launch configuration. A launch configuration
 * delegate is defined by the <code>delegate</code> attribute
 * of a <code>launchConfigurationType</code> extension.
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * @see org.eclipse.debug.core.ILaunchConfigurationType
 * @see ILaunchConfiguration
 * @since 2.0
 */
public interface ILaunchConfigurationDelegate {
	
	/**
	 * Launches the given configuration in the specified mode, contributing
	 * debug targets and/or processes to the given launch object. The
	 * launch object has already been registered with the launch manager.
	 * 
	 * @param configuration the configuration to launch
	 * @param mode the mode in which to launch, one of the mode constants
	 *  defined by <code>ILaunchManager</code> -
	 *  <code>RUN_MODE</code> or <code>DEBUG_MODE</code>.
	 * @param monitor progress monitor, or <code>null</code>
	 * @param launch the launch object to contribute processes and debug
	 *  targets to
	 * @exception CoreException if launching fails 
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException;
	
}
