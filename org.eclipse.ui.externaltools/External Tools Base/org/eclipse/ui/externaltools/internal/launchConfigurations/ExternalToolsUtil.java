/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Keith Seitz (keiths@redhat.com) - Bug 27243 (environment variables contribution)
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.launchConfigurations;


import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * Utilities for external tool launch configurations.
 * <p>
 * This class it not intended to be instantiated.
 * </p>
 */
public class ExternalToolsUtil {

	/**
	 * Throws a core exception with an error status object built from
	 * the given message, lower level exception, and error code.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 */
	protected static void abort(String message, Throwable exception, int code) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, code, message, exception));
	}
	
	/**
	 * Expands and returns the location attribute of the given launch
	 * configuration. The location is
	 * verified to point to an existing file, in the local file system.
	 * 
	 * @param configuration launch configuration
	 * @return an absolute path to a file in the local file system  
	 * @throws CoreException if unable to retrieve the associated launch
	 * configuration attribute, if unable to resolve any variables, or if the
	 * resolved location does not point to an existing file in the local file
	 * system
	 */
	public static IPath getLocation(ILaunchConfiguration configuration) throws CoreException {
		String location = configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, (String) null);
		if (location == null) {
			abort(MessageFormat.format(ExternalToolsLaunchConfigurationMessages.ExternalToolsUtil_Location_not_specified_by__0__1, new String[] { configuration.getName()}), null, 0);
		} else {
			String expandedLocation = getStringVariableManager().performStringSubstitution(location);
			if (expandedLocation == null || expandedLocation.length() == 0) {
				String msg = MessageFormat.format(ExternalToolsLaunchConfigurationMessages.ExternalToolsUtil_invalidLocation__0_, new Object[] { configuration.getName()});
				abort(msg, null, 0);
			} else {
				File file = new File(expandedLocation);
				if (file.isFile()) {
					return new Path(expandedLocation);
				} 
				
				String msg = MessageFormat.format(ExternalToolsLaunchConfigurationMessages.ExternalToolsUtil_invalidLocation__0_, new Object[] { configuration.getName()});
				abort(msg, null, 0);
			}
		}
		// execution will not reach here
		return null;
	}
	
	/**
	 * Returns a boolean specifying whether or not output should be captured for
	 * the given configuration
	 * 
	 * @param configuration the configuration from which the value will be
	 * extracted
	 * @return boolean specifying whether or not output should be captured
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static boolean getCaptureOutput(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
	}

	/**
	 * Expands and returns the working directory attribute of the given launch
	 * configuration. Returns <code>null</code> if a working directory is not
	 * specified. If specified, the working is verified to point to an existing
	 * directory in the local file system.
	 * 
	 * @param configuration launch configuration
	 * @return an absolute path to a directory in the local file system, or
	 * <code>null</code> if unspecified
	 * @throws CoreException if unable to retrieve the associated launch
	 * configuration attribute, if unable to resolve any variables, or if the
	 * resolved location does not point to an existing directory in the local
	 * file system
	 */
	public static IPath getWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
		String location = configuration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, (String) null);
		if (location != null) {
			String expandedLocation = getStringVariableManager().performStringSubstitution(location);
			if (expandedLocation.length() > 0) {
				File path = new File(expandedLocation);
				if (path.isDirectory()) {
					return new Path(expandedLocation);
				} 
				String msg = MessageFormat.format(ExternalToolsLaunchConfigurationMessages.ExternalToolsUtil_invalidDirectory__0_, new Object[] { expandedLocation, configuration.getName()});
				abort(msg, null, 0);
			}
		}
		return null;
	}

	/**
	 * Expands and returns the arguments attribute of the given launch
	 * configuration. Returns <code>null</code> if arguments are not specified.
	 * 
	 * @param configuration launch configuration
	 * @return an array of resolved arguments, or <code>null</code> if
	 * unspecified
	 * @throws CoreException if unable to retrieve the associated launch
	 * configuration attribute, or if unable to resolve any variables
	 */
	public static String[] getArguments(ILaunchConfiguration configuration) throws CoreException {
		String args = configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, (String) null);
		if (args != null) {
			String expanded = getStringVariableManager().performStringSubstitution(args);
			return parseStringIntoList(expanded);
		}
		return null;
	}

	private static IStringVariableManager getStringVariableManager() {
		return VariablesPlugin.getDefault().getStringVariableManager();
	}
	
	/**
	 * Returns whether the given launch configuration is enabled. This property
	 * is intended only to apply to external tool builder configurations and
	 * determines whether the project builder will launch the configuration
	 * when it builds.
	 *  
	 * @param configuration the configuration for which the enabled state should
	 * 		be determined.
	 * @return whether the given configuration is enabled to be run when a build occurs.
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static boolean isBuilderEnabled(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IExternalToolConstants.ATTR_BUILDER_ENABLED, true);
	}
	
	/**
	 * Returns the collection of resources for the build scope as specified by the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @throws CoreException if an exception occurs while retrieving the resources
	 */
	public static IResource[] getResourcesForBuildScope(ILaunchConfiguration configuration) throws CoreException {
		String scope = configuration.getAttribute(IExternalToolConstants.ATTR_BUILD_SCOPE, (String) null);
		if (scope == null) {
			return null;
		}
	
		return RefreshTab.getRefreshResources(scope);
	}
	
	/**
	 * Parses the argument text into an array of individual
	 * strings using the space character as the delimiter.
	 * An individual argument containing spaces must have a
	 * double quote (") at the start and end. Two double 
	 * quotes together is taken to mean an embedded double
	 * quote in the argument text.
	 * 
	 * @param arguments the arguments as one string
	 * @return the array of arguments
	 */
	public static String[] parseStringIntoList(String arguments) {
		if (arguments == null || arguments.length() == 0) {
			return new String[0];
		}
		String[] res= DebugPlugin.parseArguments(arguments);
		return res;		
	}	
	
}
