/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;


import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.program.launchConfigurations.BackgroundResourceRefresher;
import org.eclipse.ui.externaltools.internal.variable.ExpandVariableContext;

/**
 * Launch delegate for ant builds
 */
public class AntLaunchDelegate implements ILaunchConfigurationDelegate {
	
	private static final String ANT_LOGGER_CLASS = "org.eclipse.ui.externaltools.internal.ant.logger.AntProcessBuildLogger"; //$NON-NLS-1$
	private static final String NULL_LOGGER_CLASS = "org.eclipse.ui.externaltools.internal.ant.logger.NullBuildLogger"; //$NON-NLS-1$
	private static final String BASE_DIR_PREFIX = "-Dbasedir="; //$NON-NLS-1$
	private static final String INPUT_HANDLER_CLASS = "org.eclipse.ui.externaltools.internal.ant.inputhandler.AntInputHandler"; //$NON-NLS-1$	

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
		
		if (AntRunner.isBuildRunning()) {
			IStatus status= new Status(IStatus.ERROR, IExternalToolConstants.PLUGIN_ID, 1, MessageFormat.format(AntLaunchConfigurationMessages.getString("AntLaunchDelegate.Build_In_Progress"), new String[]{location.toOSString()}), null); //$NON-NLS-1$
			throw new CoreException(status);
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
		String idStamp = Long.toString(timeStamp);
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
		if (ExternalToolsUtil.getCaptureOutput(configuration)) {
			runner.addBuildLogger(ANT_LOGGER_CLASS);
		} else {
			runner.addBuildLogger(NULL_LOGGER_CLASS);
		}
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
		
		String antHome= AntUtil.getAntHome(configuration);
		if (antHome != null) {
			runner.setAntHome(antHome);
		}
		
		monitor.worked(1);
								
		if (monitor.isCanceled()) {
			return;
		}
		
		// link the process to its build logger via a timestamp
		Map attributes = new HashMap();
		attributes.put(IProcess.ATTR_PROCESS_TYPE, IExternalToolConstants.ID_ANT_PROCESS_TYPE);
		attributes.put(AntProcess.ATTR_ANT_PROCESS_ID, idStamp);
		final AntProcess process = new AntProcess(location.toOSString(), launch, attributes);
		
		// create "fake" command line for the process
		StringBuffer commandLine= generateCommandLine(location, arguments, userProperties, propertyFiles, targets, antHome);
		process.setAttribute(IProcess.ATTR_CMDLINE, commandLine.toString());
		
		if (ExternalToolsUtil.isBackground(configuration)) {
			Runnable r = new Runnable() {
				public void run() {
					try {
						runner.run(process);
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
			// execute the build 
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

	private StringBuffer generateCommandLine(IPath location, String[] arguments, Map userProperties, String[] propertyFiles, String[] targets, String antHome) {
		StringBuffer commandLine= new StringBuffer();
		if (antHome != null) {
			commandLine.append(antHome);
			commandLine.append(File.separator);
		}
		commandLine.append("ant"); //$NON-NLS-1$
		
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				String arg = arguments[i];
				commandLine.append(' ');
				commandLine.append(arg);
			}
		}
		if (propertyFiles != null) {
			for (int i = 0; i < propertyFiles.length; i++) {
				String path = propertyFiles[i];
				commandLine.append(" -propertyfile "); //$NON-NLS-1$
				commandLine.append(path);
			}
		}
		if (userProperties != null) {
			Iterator keys = userProperties.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String)keys.next();
				commandLine.append(" -D"); //$NON-NLS-1$
				commandLine.append(key);
				commandLine.append('='); 
				String value = (String)userProperties.get(key);
				commandLine.append(value);
			}
		}
		
		commandLine.append(" -inputhandler "); //$NON-NLS-1$
		commandLine.append(INPUT_HANDLER_CLASS);
		
		commandLine.append(" -logger "); //$NON-NLS-1$
		commandLine.append(ANT_LOGGER_CLASS);
		
		commandLine.append(" -buildfile "); //$NON-NLS-1$
		commandLine.append(location.toOSString());
		
		if (targets != null) {
			for (int i = 0; i < targets.length; i++) {
				commandLine.append(" "); //$NON-NLS-1$
				commandLine.append(targets[i]);
			}
		}
		return commandLine;
	}
}
