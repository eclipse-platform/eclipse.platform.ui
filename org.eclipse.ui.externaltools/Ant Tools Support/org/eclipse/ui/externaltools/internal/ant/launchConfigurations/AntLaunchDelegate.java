package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.program.launchConfigurations.BackgroundResourceRefresher;
import org.eclipse.ui.externaltools.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

/**
 * Launch delegate for ant builds
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
		
		if (ExternalToolsUtil.isBackground(configuration)) {
			monitor.beginTask(MessageFormat.format(AntLaunchConfigurationMessages.getString("AntLaunchDelegate.Launching_{0}_1"), new String[] {configuration.getName()}), 10); //$NON-NLS-1$
		} else {
			monitor.beginTask(MessageFormat.format(AntLaunchConfigurationMessages.getString("AntLaunchDelegate.Running_{0}_2"), new String[] {configuration.getName()}), 100); //$NON-NLS-1$
		}
		
		// get variable context
		ExpandVariableContext resourceContext = ExternalToolsUtil.getVariableContext();
		monitor.worked(1);

		if (monitor.isCanceled()) {
			return;
		}
		
		// resolve location
		IPath location = ExternalToolsUtil.getLocation(configuration, resourceContext);
		monitor.worked(1);
		
		if (monitor.isCanceled()) {
			return;
		}		
		
		// resolve working directory
		IPath workingDirectory = ExternalToolsUtil.getWorkingDirectory(configuration, resourceContext);
		String baseDir = null;
		if (workingDirectory != null) {
			baseDir = workingDirectory.toOSString();
		}
		monitor.worked(1);
		
		if (monitor.isCanceled()) {
			return;
		}

		// link the process to its build logger via a timestamp
		long timeStamp = System.currentTimeMillis();
		String idStamp = new Long(timeStamp).toString();
		String idProperty = "-D" + AntProcess.ATTR_ANT_PROCESS_ID + "=" + idStamp; //$NON-NLS-1$ //$NON-NLS-2$
		
		// resolve arguments
		String[] arguments = ExternalToolsUtil.getArguments(configuration, resourceContext);
		int argLength = 1; // at least one user property - timestamp
		if (arguments != null) {
			argLength += arguments.length;
		}		
		if (baseDir != null && baseDir.length() > 0) {
			argLength++;
		}
		String[] runnerArgs = new String[argLength];
		if (arguments != null) {
			System.arraycopy(arguments, 0, runnerArgs, 0, arguments.length);
		}
		if (baseDir != null && baseDir.length() > 0) {
			runnerArgs[runnerArgs.length - 2] = BASE_DIR_PREFIX + baseDir;
		}
		runnerArgs[runnerArgs.length -1] = idProperty;
		
		Map userProperties= AntUtil.getProperties(configuration);
		String[] propertyFiles= AntUtil.getPropertyFiles(configuration);
		
		final AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(location.toOSString());
		runner.addBuildLogger(ANT_LOGGER_CLASS);
		runner.setInputHandler(INPUT_HANDLER_CLASS);
		runner.setArguments(runnerArgs);
		if (userProperties != null) {
			runner.addUserProperties(userProperties);
		}
		
		if (propertyFiles != null) {
			runner.setPropertyFiles(propertyFiles);
		}
		
		String[] targets = AntUtil.getTargetsFromConfig(configuration);
		if (targets != null) {
			runner.setExecutionTargets(targets);
		}
		
		URL[] customClasspath= AntUtil.getCustomClasspath(configuration);
		if (customClasspath != null) {
			runner.setCustomClasspath(customClasspath);
		}
		
		boolean reuseClassLoader= AntUtil.shouldReuseClassLoader(configuration);
		runner.setReuseClassLoader(reuseClassLoader);
		
		monitor.worked(1);
								
		if (monitor.isCanceled()) {
			return;
		}
		
		// link the process to its build logger via a timestamp
		Map attributes = new HashMap();
		attributes.put(IProcess.ATTR_PROCESS_TYPE, IExternalToolConstants.ID_ANT_PROCESS_TYPE);
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
			monitor.worked(1);
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
		
		monitor.done();	
	}
}
