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

package org.eclipse.debug.internal.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Resolves the value of environment variables. 
 */
public class EnvironmentVariableResolver implements IDynamicVariableResolver {

	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable, java.lang.String)
	 */
	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument == null) {
			throw new CoreException(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, DebugCoreMessages.getString("EnvironmentVariableResolver.0"), null)); //$NON-NLS-1$
		}
		Map map= DebugPlugin.getDefault().getLaunchManager().getNativeEnvironment();
		if (Platform.getOS().equals(Constants.OS_WIN32)) {
			// On Win32, env variables are case insensitive, so we uppercase everything
			// for map matches
			argument= argument.toUpperCase();
		}
		return (String) map.get(argument);
	}

}
