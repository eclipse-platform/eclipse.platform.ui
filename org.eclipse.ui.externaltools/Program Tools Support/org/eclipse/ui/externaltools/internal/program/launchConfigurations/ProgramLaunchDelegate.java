package org.eclipse.ui.externaltools.internal.program.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsUtil;
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
				
		Process p = DebugPlugin.exec(cmdLine, workingDir);
		IProcess process = null;
		if (p != null) {
			process = DebugPlugin.newProcess(launch, p, location.toOSString());
		}
		
		if (!ExternalToolsUtil.isBackground(configuration)) {
			// wait for process to exit
			while (!process.isTerminated()) {
				try {
					if (monitor.isCanceled()) {
						process.terminate();
						break;
					}
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
			}
			
		}
		
		// refresh resources
		if (ExternalToolsUtil.getRefreshScope(configuration) != null) {
			BackgroundResourceRefresher refresher = new BackgroundResourceRefresher(configuration, process, resourceContext);
			refresher.startBackgroundRefresh();
		}		
	}
	
}
