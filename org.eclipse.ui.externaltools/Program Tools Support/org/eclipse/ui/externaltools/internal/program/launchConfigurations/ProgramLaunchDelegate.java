package org.eclipse.ui.externaltools.internal.program.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Launch delegate for a program.
 */
public class ProgramLaunchDelegate implements ILaunchConfigurationDelegate {

	/**
	 * Constructor for ProgramLaunchDelegate.
	 */
	public ProgramLaunchDelegate() {
		super();
	}

	/**
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		if (monitor.isCanceled()) {
			return;
		}
		
		// save dirty editors
		ExternalToolsUtil.saveDirtyEditors(configuration);
		
		if (monitor.isCanceled()) {
			return;
		}
		
		// get resource context
		IResource resource = ExternalToolsUtil.getActiveResource();
		ExpandVariableContext resourceContext = new ExpandVariableContext(resource);

		if (monitor.isCanceled()) {
			return;
		}
		
		// resolve location
		IPath location = ExternalToolsUtil.getLocation(configuration, resourceContext);
		
		if (monitor.isCanceled()) {
			return;
		}		
		
		// resolve working directory
		IPath workingDirectory = ExternalToolsUtil.getWorkingDirectory(configuration, resourceContext);
		
		if (monitor.isCanceled()) {
			return;
		}
		
		// resolve arguments
		String[] arguments = ExternalToolsUtil.getArguments(configuration, resourceContext);
		
		if (monitor.isCanceled()) {
			return;
		}
		
		int cmdLineLength = 1;
		if (arguments != null) {
			cmdLineLength += arguments.length;
		}
		String[] cmdLine = new String[cmdLineLength];
		cmdLine[0] = location.toOSString();
		if (arguments != null) {
			System.arraycopy(arguments, 0, cmdLine, 1, arguments.length);
		}
		
		File workingDir = null;
		if (workingDirectory != null) {
			workingDir = workingDirectory.toFile();
		}
		
		if (monitor.isCanceled()) {
			return;
		}
				
		Process p = exec(cmdLine, workingDir);
		DebugPlugin.newProcess(launch, p, location.toOSString());
		
	}
	
	/**
	 * Performs a runtime exec on the given command line in the context
	 * of the specified working directory, and returns
	 * the resulting process. If the current runtime does not support the
	 * specification of a working directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if the
	 * exec should be re-executed without specifying a working directory.
	 * 
	 * @param cmdLine the command line
	 * @param workingDirectory the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *  cancelled
	 * @see Runtime
	 * 
	 * TODO: this should be pushed down to debug core
	 */
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		Process p= null;
		try {

			if (workingDirectory == null) {
				p= Runtime.getRuntime().exec(cmdLine, null);
			} else {
				p= Runtime.getRuntime().exec(cmdLine, null, workingDirectory);
			}
		} catch (IOException e) {
				if (p != null) {
					p.destroy();
				}
				Status status = new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 0, "Exception occurred launching process.", e);
				throw new CoreException(status);
		} catch (NoSuchMethodError e) {
			//attempting launches on 1.2.* - no ability to set working directory
			exec(cmdLine, null);
			
//			IStatus status = new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), IJavaLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED, LaunchingMessages.getString("AbstractVMRunner.Eclipse_runtime_does_not_support_working_directory_2"), e); //$NON-NLS-1$
//			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
//			
//			if (handler != null) {
//				Object result = handler.handleStatus(status, this);
//				if (result instanceof Boolean && ((Boolean)result).booleanValue()) {
//					p= exec(cmdLine, null);
//				}
//			}
		}
		return p;
	}	

}
