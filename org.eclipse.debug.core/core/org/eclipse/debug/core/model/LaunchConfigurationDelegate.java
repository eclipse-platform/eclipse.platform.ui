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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;

/**
 * Default implementation of a launch configuration delegate.
 * <p>
 * Clients implementing launch configration delegates should subclass
 * this class.
 * </p>
 * @since 3.0
 */
public abstract class LaunchConfigurationDelegate implements ILaunchConfigurationDelegate2 {
	
	/**
	 * Status code for which a UI prompter is registered.
	 */
	protected static final IStatus promptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200, "", null);  //$NON-NLS-1$//$NON-NLS-2$
	
	/**
	 * Status code for which a prompter is registered to ask the user if they
	 * want to launch in debug mode when breakpoints are present.
	 */
	protected static final IStatus switchToDebugPromptStatus = new Status(IStatus.INFO, "org.eclipse.debug.core", 201, "", null);  //$NON-NLS-1$//$NON-NLS-2$
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#getLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#buildForLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#finalLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return true;
	}
	/* (non-Javadoc)
	 * 
	 * If launching in run mode, and the configuration supports debug mode, check
	 * if there are any breakpoints in the workspace, and ask the user if they'd
	 * rather launch in debug mode.
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate2#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		if (mode.equals(ILaunchManager.RUN_MODE)  && configuration.supportsMode(ILaunchManager.DEBUG_MODE)) {
			IBreakpointManager breakpointManager = DebugPlugin.getDefault().getBreakpointManager();
			if (!breakpointManager.isEnabled()) {
				// no need to check breakpoints individually.
				return true; 
			}
			IBreakpoint[] breakpoints = breakpointManager.getBreakpoints();
			for (int i = 0; i < breakpoints.length; i++) {
				if (breakpoints[i].isEnabled()) {
					IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);
					if (prompter != null) {
						boolean lauchInDebugModeInstead = ((Boolean)prompter.handleStatus(switchToDebugPromptStatus, configuration)).booleanValue();
						if (lauchInDebugModeInstead) { 
							return false; //kill this launch
						} 
					}
					// if no user prompt, or user says to continue (no need to check other breakpoints)
					return true;
				}
			}
		}	
		// no enabled breakpoints... continue launch
		return true;
	}

}
