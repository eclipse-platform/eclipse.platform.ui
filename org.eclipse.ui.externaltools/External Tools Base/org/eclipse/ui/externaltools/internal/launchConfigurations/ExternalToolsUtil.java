/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Keith Seitz (keiths@redhat.com) - environment variables contribution (Bug 27243(
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.launchConfigurations;


import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.variables.LaunchVariableUtil;
import org.eclipse.debug.ui.launchVariables.RefreshTab;
import org.eclipse.debug.ui.launchVariables.VariableContextManager;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolMigration;

/**
 * Utilities for external tool launch configurations.
 * <p>
 * This class it not intended to be instantiated.
 * </p>
 */
public class ExternalToolsUtil {

	private static final String LAUNCH_CONFIG_HANDLE = "LaunchConfigHandle"; //$NON-NLS-1$

	/**
	 * Not to be instantiated.
	 */
	private ExternalToolsUtil() {
	};

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
	 * configuration, based on the given variable context. The location is
	 * verified to point to an existing file, in the local file system.
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand variables
	 * @return an absolute path to a file in the local file system  
	 * @throws CoreException if unable to retrieve the associated launch
	 * configuration attribute, if unable to resolve any variables, or if the
	 * resolved location does not point to an existing file in the local file
	 * system
	 */
	public static IPath getLocation(ILaunchConfiguration configuration) throws CoreException {
		String location = configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, (String) null);
		if (location == null) {
			abort(MessageFormat.format(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsUtil.Location_not_specified_by_{0}_1"), new String[] { configuration.getName()}), null, 0); //$NON-NLS-1$
		} else {
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsUtil.Could_not_resolve_location._3"), null); //$NON-NLS-1$
			String expandedLocation = LaunchVariableUtil.expandVariables(location, status, VariableContextManager.getDefault().getVariableContext());
			if (status.isOK()) {
				if (expandedLocation == null || expandedLocation.length() == 0) {
					String msg = MessageFormat.format(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsUtil.invalidLocation_{0}"), new Object[] { configuration.getName()}); //$NON-NLS-1$
					abort(msg, null, 0);
				} else {
					File file = new File(expandedLocation);
					if (file.isFile()) {
						return new Path(expandedLocation);
					} else {
						String msg = MessageFormat.format(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsUtil.invalidLocation_{0}"), new Object[] { configuration.getName()}); //$NON-NLS-1$
						abort(msg, null, 0);
					}
				}
			} else {
				throw new CoreException(status);
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
		return configuration.getAttribute(IExternalToolConstants.ATTR_CAPTURE_OUTPUT, true);
	}

	/**
	 * Expands and returns the working directory attribute of the given launch
	 * configuration, based on the given variable context. Returns
	 * <code>null</code> if a working directory is not specified. If specified,
	 * the working is verified to point to an existing directory in the local
	 * file system.
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand variables
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
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsUtil.Could_not_resolve_working_directory._4"), null); //$NON-NLS-1$
			String expandedLocation = LaunchVariableUtil.expandVariables(location, status, VariableContextManager.getDefault().getVariableContext());
			if (status.isOK()) {
				if (expandedLocation != null && expandedLocation.length() > 0) {
					File path = new File(expandedLocation);
					if (path.isDirectory()) {
						return new Path(expandedLocation);
					} else {
						String msg = MessageFormat.format(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsUtil.invalidDirectory_{0}"), new Object[] { expandedLocation, configuration.getName()}); //$NON-NLS-1$
						abort(msg, null, 0);
					}
				}
			} else {
				throw new CoreException(status);
			}
		}
		return null;
	}

	/**
	 * Expands and returns the arguments attribute of the given launch
	 * configuration, based on the given variable context. Returns
	 * <code>null</code> if arguments are not specified.
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand variables
	 * @return an array of resolved arguments, or <code>null</code> if
	 * unspecified
	 * @throws CoreException if unable to retrieve the associated launch
	 * configuration attribute, or if unable to resolve any variables
	 */
	public static String[] getArguments(ILaunchConfiguration configuration) throws CoreException {
		String args = configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, (String) null);
		if (args != null) {
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsUtil.Could_not_resolve_an_argument._1"), null); //$NON-NLS-1$
			String[] expandedArgs = LaunchVariableUtil.expandStrings(args, status, VariableContextManager.getDefault().getVariableContext());
			if (status.isOK()) {
				return expandedArgs;
			} else {
				throw new CoreException(status);
			}
		}
		return null;
	}

	/**
	 * Returns whether the given configuration is to be run in the background.
	 * 
	 * @param configuration the configuration for which the background state should
	 * 		be determined.
	 * @return whether the given configuration is to be run in the background
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static boolean isBackground(ILaunchConfiguration configuration) throws CoreException {
		boolean defaultValue= true;
		if (configuration.getCategory().equals(IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_CATEGORY)) {
			// Project builders are not run in the background by default.
			defaultValue= false;
		}
		return configuration.getAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, defaultValue);
	}
	
	/**
	 * Returns the collection of resources for the build scope as specified by the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand variables
	 * @param monitor progress monitor
	 * @throws CoreException if an exception occurs while refreshing resources
	 */
	public static IResource[] getResourcesForBuildScope(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		String scope = configuration.getAttribute(IExternalToolConstants.ATTR_BUILD_SCOPE, (String) null);
		if (scope == null) {
			return null;
		}
	
		return RefreshTab.expandResources(scope, monitor);
	
	}

	/**
	 * Returns a launch configuration from the given ICommand arguments. If the
	 * given arguments are from an old-style external tool, an unsaved working
	 * copy will be created from the arguments and returned.
	 * 
	 * @param commandArgs the builder ICommand arguments
	 * @param newName a new name for the config if the one in the command is
	 * invalid
	 * @return a launch configuration, a launch configuration working copy, or
	 * <code>null</code> if not possible.
	 */
	public static ILaunchConfiguration configFromBuildCommandArgs(Map commandArgs) {
		String configHandle = (String) commandArgs.get(LAUNCH_CONFIG_HANDLE);
		if (configHandle == null) {
			// Probably an old-style external tool. Try to migrate.
			return ExternalToolMigration.configFromArgumentMap(commandArgs);
		}
		try {
			return DebugPlugin.getDefault().getLaunchManager().getLaunchConfiguration(configHandle);
		} catch (CoreException e) {
			return null;
		}
	}
}
