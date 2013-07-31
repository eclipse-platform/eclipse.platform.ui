/*******************************************************************************
 *  Copyright (c) 2010, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.ui.midi.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;

/**
 * Example status handler used to open the launch dialog on a launch failure. This handler
 * handles the '303' status code from 'org.eclipse.debug.examples.core' plug-in.
 */
public class ExampleLaunchStatusHandler implements IStatusHandler {

	@Override
	public Object handleStatus(IStatus status, Object source) throws CoreException {
		if (source instanceof ILaunchConfigurationDialog) {
			return null;
		}
		throw new CoreException(new Status(IStatus.ERROR, "org.eclipse.debug.examples.ui", "'source' should be an instanceof ILaunchConfigrationDialog")); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
