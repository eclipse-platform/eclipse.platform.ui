/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Optional enhancements to the launch configuration delegate interface
 * that launch delegates may optionally implement. Allows launch delegates
 * to create the launch object to be used in a launch.
 * <p>
 * Clients implementing <code>ILaunchConfigurationDelegate</code> may also
 * implement this interface.
 * </p>
 * @since 3.0
 */
public interface ILaunchConfigurationDelegate2 extends ILaunchConfigurationDelegate {
	
	/**
	 * Returns a launch object to use when launching the given launch
	 * configuration in the given mode, or <code>null</code> if a new default
	 * launch object should be created by the debug platform. If a launch object
	 * is returned, its launch mode must match that of the mode specified in
	 * this method call.
	 *  
	 * @param configuration the configuration being launched
	 * @param mode the mode the configuration is being launched in
	 * @return a launch object or <code>null</code>
	 * @throws CoreException if unable to launch
	 */
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException;
}
