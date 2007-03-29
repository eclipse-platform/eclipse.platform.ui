/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
import org.eclipse.swt.widgets.Shell;

/**
 * This class provides a mechanism to prompt users in the UI thread from debug.core in the case where
 * a launch delegate has gone missing and a new choice needs to be made in the launch dialog.
 * 
 * @since 3.3
 */
public class LaunchDelegateNotAvailableHandler implements IStatusHandler {

	/**
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		if(source instanceof Object[]) {
			Object[] infos = (Object[]) source;
			if(infos.length == 2) {
				final ILaunchConfiguration config = (ILaunchConfiguration) infos[0];
				final String mode = (String) infos[1];
				final Shell shell = DebugUIPlugin.getShell();
				Runnable runnable = new Runnable() {
					public void run() {
						DebugUITools.openLaunchConfigurationDialog(shell, config, DebugUITools.getLaunchGroup(config, mode).getIdentifier(), null);
					}
				};
				DebugUIPlugin.getStandardDisplay().asyncExec(runnable);
			}
		}
		return Status.OK_STATUS;
	}
}
