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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ResourceSelectionManager;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
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
		
}
