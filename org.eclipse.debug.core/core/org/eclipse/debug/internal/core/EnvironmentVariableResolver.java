
package org.eclipse.debug.internal.core;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.debug.core.DebugPlugin;

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
		HashMap map= LaunchManager.getNativeEnvironment();
		return (String) map.get(argument);
	}

}
