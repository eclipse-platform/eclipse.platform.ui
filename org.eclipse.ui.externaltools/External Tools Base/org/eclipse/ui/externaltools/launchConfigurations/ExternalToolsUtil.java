package org.eclipse.ui.externaltools.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ResourceSelectionManager;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.registry.RefreshScopeVariable;
import org
	.eclipse
	.ui
	.externaltools
	.internal
	.registry
	.RefreshScopeVariableRegistry;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Utilities for external tool launch configurations.
 * <p>
 * This class it not intended to be instantiated.
 * </p>
 */
public class ExternalToolsUtil {
	
	/**
	 * Not to be instantiated.	 */
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
	 * Saves any dirty editors, as specified by the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @exception CoreException if unable to retrieve the associated launch
	 * configuration attribute
	 */
	public static void saveDirtyEditors(ILaunchConfiguration configuration) throws CoreException {
		boolean save = configuration.getAttribute(IExternalToolConstants.ATTR_SAVE_DIRTY_EDITORS, false);
		if (save) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				IWorkbenchPage[] pages = windows[i].getPages();
				for (int j = 0; j < pages.length; j++) {
					pages[j].saveAllEditors(false);
				}
			}
		}
	}		
	
	/**
	 * Returns the resource associated with the selection or active editor in
	 * the active workbench window, or <code>null</code> if none.
	 * 
	 * @return returns the resource associated with the selection or active editor in
	 * the active workbench window, or <code>null</code> if none	 */
	public static IResource getActiveResource() {
		return ResourceSelectionManager.getDefault().getActiveResource();
	}	

	/**
	 * Expands and returns the location attribute of the given launch
	 * configuration, based on the given variable context. The location is
	 * verified to point to an existing file, in the local file system.
	 * 
	 * @param configuration launch configuration	 * @param context context used to expand variables	 * @return an absolute path to a file in the local file system  	 * @throws CoreException if unable to retrieve the associated launch
	 * configuration attribute, if unable to resolve any variables, or if the
	 * resolved location does not point to an existing file in the local file
	 * system
	 */
	public static IPath getLocation(ILaunchConfiguration configuration, ExpandVariableContext context) throws CoreException {
		String location = configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, (String)null);
		if (location == null) {
			abort(MessageFormat.format("Location not specified by {0}", new String[]{configuration.getName()}), null, 0);
		} else {	
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, ToolMessages.getString("RunExternalToolAction.runProblem"), null); //$NON-NLS-1$;
			String expandedLocation = ToolUtil.expandFileLocation(location, context, status);
			if (status.isOK()) {
				if (expandedLocation == null || expandedLocation.length() == 0) {
					String msg = ToolMessages.format("DefaultRunnerContext.invalidLocation", new Object[] {configuration.getName()}); //$NON-NLS-1$
					abort(msg, null, 0);
				} else {
					File file = new File(expandedLocation);
					if (file.isFile()) {
						return new Path(expandedLocation);
					} else {
						String msg = ToolMessages.format("DefaultRunnerContext.invalidLocation", new Object[] {configuration.getName()}); //$NON-NLS-1$
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
	 * Expands and returns the working directory attribute of the given launch
	 * configuration, based on the given variable context. Returns
	 * <code>null</code> if a working directory is not specified. If specified,
	 * the working is verified to point to an existing directory in the local
	 * file system.
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand variables
	 * @return an absolute path to a direcoty in the local file system, or
	 * <code>null</code> if unspecified
	 * @throws CoreException if unable to retrieve the associated launch
	 * configuration attribute, if unable to resolve any variables, or if the
	 * resolved location does not point to an existing directory in the local
	 * file system
	 */
	public static IPath getWorkingDirectory(ILaunchConfiguration configuration, ExpandVariableContext context) throws CoreException {
		String location = configuration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, (String)null);
		if (location != null) {
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, ToolMessages.getString("RunExternalToolAction.runProblem"), null); //$NON-NLS-1$;
			String expandedLocation = ToolUtil.expandDirectoryLocation(location, context, status);
			if (status.isOK()) {
				if (expandedLocation != null && expandedLocation.length() > 0) {
					File path = new File(expandedLocation);
					if (path.isDirectory()) {
						return new Path(expandedLocation); 
					} else {
						String msg = ToolMessages.format("DefaultRunnerContext.invalidDirectory", new Object[] {configuration.getName()}); //$NON-NLS-1$
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
	public static String[] getArguments(ILaunchConfiguration configuration, ExpandVariableContext context) throws CoreException {
		String args = configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, (String)null);
		if (args != null) {
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, ToolMessages.getString("RunExternalToolAction.runProblem"), null); //$NON-NLS-1$;
			String[] expandedArgs = ToolUtil.expandArguments(args, context, status);
			if (status.isOK()) {
				return expandedArgs;
			} else {
				throw new CoreException(status);
			} 
		}
		return null;
	}		
	
	/**
	 * Returns the refresh scope specified by the given launch configuration or
	 * <code>null</code> if none.
	 * 
	 * @param configuration	 * @return refresh scope	 * @throws CoreException if unable to access the associated attribute	 */	
	public static String getRefreshScope(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, (String)null);
	}
	
	/**
	 * Returns whether the refresh scope specified by the given launch
	 * configuration is recursive.
	 * 
	 * @param configuration
	 * @return whether the refresh scope is recursive
	 * @throws CoreException if unable to access the associated attribute
	 */	
	public static boolean isRefreshRecursive(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IExternalToolConstants.ATTR_REFRESH_RECURSIVE, false);
	}
	
	/**
	 * Refreshes the resources as specified by the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand variables
	 * @param monitor progress monitor	 * @throws CoreException if an exception occurrs while refreshing resources	 */
	public static void refreshResources(ILaunchConfiguration configuration, ExpandVariableContext context, IProgressMonitor monitor) throws CoreException {
		String scope = getRefreshScope(configuration);
		if (scope == null)
			return;
		
		ToolUtil.VariableDefinition varDef = ToolUtil.extractVariableTag(scope, 0);
		if (varDef.start == -1 || varDef.end == -1 || varDef.name == null) {
			String msg = ToolMessages.format("DefaultRunnerContext.invalidRefreshVarFormat", new Object[] {configuration.getName()}); //$NON-NLS-1$
			abort(msg, null, 0);
		}
		
		RefreshScopeVariableRegistry registry = ExternalToolsPlugin.getDefault().getRefreshVariableRegistry();
		RefreshScopeVariable variable = registry.getRefreshVariable(varDef.name);
		if (variable == null) {
			String msg = ToolMessages.format("DefaultRunnerContext.noRefreshVarNamed", new Object[] {configuration.getName(), varDef.name}); //$NON-NLS-1$
			abort(msg, null, 0);
		}

		int depth = IResource.DEPTH_ZERO;
		if (isRefreshRecursive(configuration))
			depth = IResource.DEPTH_INFINITE;

		if (monitor.isCanceled())
			return;
					
		IResource[] resources = variable.getExpander().getResources(varDef.name, varDef.argument, context);
		if (resources == null || resources.length == 0)
			return;
			
		monitor.beginTask(
			ToolMessages.getString("DefaultRunnerContext.refreshResources"), //$NON-NLS-1$
			resources.length);
			
		MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "Exception(s) occurred during refresh.", null);
		for (int i = 0; i < resources.length; i++) {
			if (monitor.isCanceled())
				break;
			if (resources[i] != null && resources[i].isAccessible()) {
				try {
					resources[i].refreshLocal(depth, null);
				} catch (CoreException e) {
					status.merge(e.getStatus());
				}
			}
			monitor.worked(1);
		}
		
		monitor.done();
		if (!status.isOK()) {
			throw new CoreException(status);
		}		
	}	
	
	/**
	 * Returns whether this tool is to be run in the background..
	 * 
	 * @param configuration
	 * @return whether this tool is to be run in the background
	 * @throws CoreException if unable to access the associated attribute
	 */	
	public static boolean isBackground(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IExternalToolConstants.ATTR_RUN_IN_BACKGROUND, false);
	}	
}
