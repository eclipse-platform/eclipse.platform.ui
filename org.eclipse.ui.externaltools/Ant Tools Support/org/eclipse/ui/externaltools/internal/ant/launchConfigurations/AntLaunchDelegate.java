package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.ui.console.IConsoleContentProvider;
import org
	.eclipse
	.ui
	.externaltools
	.internal
	.program
	.launchConfigurations
	.BackgroundResourceRefresher;
import org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Launch delegate for ant scripts
 */
public class AntLaunchDelegate implements ILaunchConfigurationDelegate {
	
	private static final String ANT_LOGGER_CLASS = "org.eclipse.ui.externaltools.internal.ant.logger.AntProcessBuildLogger"; //$NON-NLS-1$
	private static final String BASE_DIR_PREFIX = "-Dbasedir="; //$NON-NLS-1$
	private static final String INPUT_HANDLER_CLASS = "org.eclipse.ui.externaltools.internal.ant.inputhandler.AntInputHandler"; //$NON-NLS-1$	

	/**
	 * Constructs a new launch delegate
	 */
	public AntLaunchDelegate() {
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
		String baseDir = null;
		if (workingDirectory != null) {
			baseDir = workingDirectory.toOSString();
		}
		
		if (monitor.isCanceled()) {
			return;
		}
		
		// resolve arguments
		String[] arguments = ExternalToolsUtil.getArguments(configuration, resourceContext);
		if (arguments == null) {
			arguments = new String[0];
		}
		
		String[] runnerArgs = arguments;
		if (baseDir != null && baseDir.length() > 0) {
			// Ant requires the working directory to be specified
			// as one of the arguments, so it needs to be appended.
			runnerArgs = new String[arguments.length + 1];
			System.arraycopy(arguments, 0, runnerArgs, 0, arguments.length);
			runnerArgs[arguments.length] = BASE_DIR_PREFIX + baseDir;			
		}
				
		final AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(location.toOSString());
		// TODO: set targets
		runner.addBuildLogger(ANT_LOGGER_CLASS);
		runner.setInputHandler(INPUT_HANDLER_CLASS);
		
		if (monitor.isCanceled()) {
			return;
		}
		
		int cmdLineLength = 2;
		if (arguments != null) {
			cmdLineLength += arguments.length;
		}
		String[] cmdLine = new String[cmdLineLength];
		cmdLine[0] = location.toOSString();
		if (arguments != null) {
			System.arraycopy(arguments, 0, cmdLine, 1, arguments.length);
		}
		// link the process to its build logger via a timestamp
		long timeStamp = System.currentTimeMillis();
		String idStamp = new Long(timeStamp).toString();
		cmdLine[cmdLine.length - 1] = "-D" + AntProcess.ATTR_ANT_PROCESS_ID + "=" + idStamp;		
		runnerArgs = cmdLine;
		runner.setArguments(runnerArgs);
						
		if (monitor.isCanceled()) {
			return;
		}
		
		// link the process to its build logger via a timestamp
		Map attributes = new HashMap();
		attributes.put(IConsoleContentProvider.ATTR_CONSOLE_CONTENT_PROVIDER, AntConsoleContentProvider.ID_ANT_CONSOLE_CONTNET_PROVIDER);
		attributes.put(AntProcess.ATTR_ANT_PROCESS_ID, idStamp);
		final AntProcess process = new AntProcess(location.toOSString(), launch, attributes);
		
		if (ExternalToolsUtil.isBackground(configuration)) {
			Runnable r = new Runnable() {
				public void run() {
					try {
						runner.run();
					} catch (CoreException e) {
					}
					process.terminated();
				}
			};
			Thread background = new Thread(r);
			background.start();
			// refresh resources after process finishes
			if (ExternalToolsUtil.getRefreshScope(configuration) != null) {
				BackgroundResourceRefresher refresher = new BackgroundResourceRefresher(configuration, process, resourceContext);
				refresher.startBackgroundRefresh();
			}				
		} else {
			// run the script
			try {
				runner.run(monitor);
			} catch (CoreException e) {
				process.terminated();
				throw e;
			}
			process.terminated();
			
			// refresh resources
			ExternalToolsUtil.refreshResources(configuration, resourceContext, monitor);
		}		
	}
}
