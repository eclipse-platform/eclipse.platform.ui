package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

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
	 * Launches the given configuration in the specified mode, and
	 * returns the resulting launch object that describes the launched
	 * configuration. The resulting launch object is registered with the
	 * launch manager. Returns <code>null</code> if the launch is not
	 * completed.
	 * <p>
	 * [Issue: this API is being changed. A launch object will be created
	 *  and registered before this method is called, and will be passed
	 *  as a parameter to this method. This delegate will add targets and
	 *  processes to the launch as required. This method will not return
	 *  a value (i.e. will be 'void').]
	 * </p>
	 * 
	 * @param configuration the configuration to launch
	 * @param mode the mode in which to launch, one of the mode constants
	 *  defined by this <code>ILaunchConfiguration</code> -
	 *  <code>RUN</code> or <code>DEBUG</code>.
	 * @param monitor progress monitor, or <code>null</code>
	 * @return the resulting launch object, or <code>null</code> if the
	 *  launch is not completed.
	 * @exception CoreException if launching fails 
	 */
	public ILaunch launch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException;
	
}
