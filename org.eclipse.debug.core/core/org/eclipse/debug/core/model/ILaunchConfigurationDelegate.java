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
 * Note: This interface is yet experimental.
 * <p>
 * A launch configuration delegate performs launching for a
 * specific type of launch configuration. A launch configuration
 * delegate is defined by the <code>delegate</code> attribute
 * of a <code>launchConfigurationType</code> extension.
 * </p>
 * <p>
 * This interface is intended to be implemented by clients.
 * </p>
 * <p>
 * <b>NOTE:</b> This class/interface is part of an interim API that is still under development and expected to 
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback 
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see org.eclipse.debug.core.ILaunchConfigurationType
 * @see ILaunchConfiguration
 */
public interface ILaunchConfigurationDelegate {
	
	/**
	 * Launches the given configuration in the specified mode, and
	 * returns the resulting launch object that describes the launched
	 * configuration. The resulting launch object is registered with the
	 * launch manager. Returns <code>null</code> if the launch is not
	 * completed.
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
