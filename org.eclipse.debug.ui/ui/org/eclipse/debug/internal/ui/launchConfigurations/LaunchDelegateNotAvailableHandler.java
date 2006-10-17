/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;

/**
 * This class provides a mechanism to prompt users in the UI thread from debug.core in the case where
 * a launch delegate has gone missing and a new choice needs to be made in the launch dialog.
 * 
 * @since 3.3
 * 
 * EXPERIMENTAL
 */
public class LaunchDelegateNotAvailableHandler implements IStatusHandler {

	/**
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		if(source instanceof Object[]) {
			Object[] infos = (Object[]) source;
			if(infos.length == 2) {
				ILaunchConfiguration config = (ILaunchConfiguration) infos[0];
				String mode = (String) infos[1];
				DebugUITools.openLaunchConfigurationEditDialog(DebugUIPlugin.getShell(), 
						config, 
						DebugUITools.getLaunchGroup(config, mode).getIdentifier(),
						null);
			}
		}
		return Status.OK_STATUS;
	}
}
