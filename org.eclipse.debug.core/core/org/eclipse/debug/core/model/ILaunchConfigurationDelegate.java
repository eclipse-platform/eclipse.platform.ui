package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * Note: This interface is yet experimental.
 * <p>
 * A launch configuration delegate performs lanuching for a
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
 * @see ILaunchConfigurationType
 * @see ILaunchConfiguration
 */
public interface ILaunchConfigurationDelegate {
	
	/**
	 * Launches the given configuration in the specified mode, and
	 * returns the resulting launch object that describes the launched
	 * configuration. The resulting launch object is registered with the
	 * launch manager. Returns <code>null</code> if the launch is not
	 * completed or the launch does not succeed.
	 * 
	 * @param configuration the configuration to launch
	 * @param mode the mode in which to launch, one of the mode constants
	 *  defined by this <code>ILaunchConfiguration</code> -
	 *  <code>RUN</code> or <code>DEBUG</code>.
	 * @return the resuling launch object, or <code>null</code> if the
	 *  launch is not completed, or does not succeed.
	 */
	public ILaunch launch(ILaunchConfiguration configuration, String mode);
	
	/**
	 * Returns whether the given configuration can be launched in the
	 * specified mode with its current attribute settings.
	 * 
	 * @param mode a mode in which a configuration can be launched, one of
	 *  the mode constants defined by this <code>ILaunchConfiguration</code>
	 *  - <code>RUN</code> or <code>DEBUG</code>.
	 * @return whether the given configuration can be launched in the specified
	 *  mode with its current attribute settings
	 */
	public boolean canLaunch(ILaunchConfiguration configuration, String mode);
	
	/**
	 * Initializes the given configuration's attributes to default settings
	 * based on the the specified object.
	 * 
	 * @param configuration a working copy configuration in which to
	 *  set default attributes for launching
	 * @param object a context from which to initialize settings
	 */
	public void initializeDefaults(ILaunchConfigurationWorkingCopy configuration, Object object);	

}
