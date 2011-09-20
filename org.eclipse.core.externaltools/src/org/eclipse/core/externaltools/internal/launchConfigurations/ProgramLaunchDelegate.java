/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Keith Seitz (keiths@redhat.com) - environment variables contribution (Bug 27243)
 *     dakshinamurthy.karra@gmail.com - bug 165371
 *******************************************************************************/
package org.eclipse.core.externaltools.internal.launchConfigurations;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.RefreshUtil;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.osgi.util.NLS;

/**
 * Launch delegate for a program.
 */
public class ProgramLaunchDelegate extends LaunchConfigurationDelegate {
	
	/**
	 * Launch configuration attribute - a boolean value indicating whether a
	 * configuration should be launched in the background. Default value is <code>true</code>.
	 * <p>
	 * This constant is defined in org.eclipse.debug.ui, but has to be copied here to support
	 * headless launching.
	 * </p>
	 */
	private static final String ATTR_LAUNCH_IN_BACKGROUND = "org.eclipse.debug.ui.ATTR_LAUNCH_IN_BACKGROUND"; //$NON-NLS-1$

	/**
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.debug.core.ILaunch,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor.isCanceled()) {
			return;
		}

		// resolve location
		IPath location = ExternalToolsCoreUtil.getLocation(configuration);

		if (monitor.isCanceled()) {
			return;
		}

		// resolve working directory
		IPath workingDirectory = ExternalToolsCoreUtil
				.getWorkingDirectory(configuration);

		if (monitor.isCanceled()) {
			return;
		}

		// resolve arguments
		String[] arguments = ExternalToolsCoreUtil.getArguments(configuration);

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

		String[] envp = DebugPlugin.getDefault().getLaunchManager()
				.getEnvironment(configuration);

		if (monitor.isCanceled()) {
			return;
		}

		Process p = DebugPlugin.exec(cmdLine, workingDir, envp);
		IProcess process = null;

		// add process type to process attributes
		Map processAttributes = new HashMap();
		String programName = location.lastSegment();
		String extension = location.getFileExtension();
		if (extension != null) {
			programName = programName.substring(0, programName.length()
					- (extension.length() + 1));
		}
		programName = programName.toLowerCase();
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);

		if (p != null) {
			monitor.beginTask(NLS.bind(
					ExternalToolsProgramMessages.ProgramLaunchDelegate_3,
					new String[] { configuration.getName() }),
					IProgressMonitor.UNKNOWN);
			process = DebugPlugin.newProcess(launch, p, location.toOSString(),
					processAttributes);
		}
		if (p == null || process == null) {
			if (p != null)
				p.destroy();
			throw new CoreException(new Status(IStatus.ERROR,
					IExternalToolConstants.PLUGIN_ID,
					IExternalToolConstants.ERR_INTERNAL_ERROR,
					ExternalToolsProgramMessages.ProgramLaunchDelegate_4, null));
		}
		process.setAttribute(IProcess.ATTR_CMDLINE,
				generateCommandLine(cmdLine));

		if (configuration.getAttribute(ATTR_LAUNCH_IN_BACKGROUND, true)) {
			// refresh resources after process finishes
			String scope = configuration.getAttribute(RefreshUtil.ATTR_REFRESH_SCOPE, (String)null);
			if (scope != null) {
				BackgroundResourceRefresher refresher = new BackgroundResourceRefresher(configuration, process);
				refresher.startBackgroundRefresh();
			}
		} else {
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

			// refresh resources
			RefreshUtil.refreshResources(configuration, monitor);
		}
	}

	private String generateCommandLine(String[] commandLine) {
		if (commandLine.length < 1)
			return IExternalToolConstants.EMPTY_STRING;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < commandLine.length; i++) {
			buf.append(' ');
			char[] characters = commandLine[i].toCharArray();
			StringBuffer command = new StringBuffer();
			boolean containsSpace = false;
			for (int j = 0; j < characters.length; j++) {
				char character = characters[j];
				if (character == '\"') {
					command.append('\\');
				} else if (character == ' ') {
					containsSpace = true;
				}
				command.append(character);
			}
			if (containsSpace) {
				buf.append('\"');
				buf.append(command);
				buf.append('\"');
			} else {
				buf.append(command);
			}
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.LaunchConfigurationDelegate#getBuildOrder
	 * (org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration,
			String mode) throws CoreException {
		IProject[] projects = ExternalToolsCoreUtil.getBuildProjects(
				configuration, null);
		if (projects == null) {
			return null;
		}
		boolean isRef = ExternalToolsCoreUtil.isIncludeReferencedProjects(
				configuration, null);
		if (isRef) {
			return computeReferencedBuildOrder(projects);
		}
		return computeBuildOrder(projects);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#saveBeforeLaunch
	 * (org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, 
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected boolean saveBeforeLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		if (IExternalToolConstants.ID_EXTERNAL_TOOLS_BUILDER_LAUNCH_CATEGORY
				.equals(configuration.getType().getCategory())) {
			// don't prompt for builders
			return true;
		}
		return super.saveBeforeLaunch(configuration, mode, monitor);
	}

}
