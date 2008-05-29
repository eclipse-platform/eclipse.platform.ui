/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;


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
	 * @param monitor progress monitor, or <code>null</code> progress monitor, or <code>null</code>. A cancelable progress 
	 * monitor is provided by the Job framework. It should be noted that the setCanceled(boolean) method should 
	 * never be called on the provided monitor or the monitor passed to any delegates from this method; due to a 
	 * limitation in the progress monitor framework using the setCanceled method can cause entire workspace batch 
	 * jobs to be canceled, as the canceled flag is propagated up the top-level parent monitor. 
	 * The provided monitor is not guaranteed to have been started. 
	 * @param launch the launch object to contribute processes and debug
	 *  targets to
	 * @exception CoreException if launching fails 
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException;
	
}
