/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Keith Seitz (keiths@redhat.com) - environment variables contribution (Bug 27243)
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.program.launchConfigurations;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.debug.ui.variables.ExpandVariableContext;

/**
 * Launch delegate for a program.
 */
public class ProgramLaunchDelegate implements ILaunchConfigurationDelegate {

	private static IWindowListener windowListener;

	/**
	 * A window listener that warns the user about any running programs when
	 * the workbench closes. Programs are killed when the VM exits.
	 */
	private class ProgramLaunchWindowListener implements IWindowListener {
		public void windowActivated(IWorkbenchWindow window) {
		}
		public void windowDeactivated(IWorkbenchWindow window) {
		}
		public void windowClosed(IWorkbenchWindow window) {
			IWorkbenchWindow windows[]= PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 1) {
				// There are more windows still open.
				return;
			}
			ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType programType= manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
			if (programType == null) {
				return;
			}
			ILaunch launches[]= manager.getLaunches();
			ILaunchConfigurationType configType;
			ILaunchConfiguration config;
			for (int i = 0; i < launches.length; i++) {
				try {
					config= launches[i].getLaunchConfiguration();
					if (config == null) {
						continue;
					}
					configType= config.getType();
				} catch (CoreException e) {
					continue;
				}
				if (configType.equals(programType)) {
					if (!launches[i].isTerminated()) {
						MessageDialog.openWarning(window.getShell(), ExternalToolsProgramMessages.getString("ProgramLaunchDelegate.Workbench_Closing_1"), ExternalToolsProgramMessages.getString("ProgramLaunchDelegate.The_workbench_is_exiting")); //$NON-NLS-1$ //$NON-NLS-2$
						break;
					}
				}
			}
		}
		public void windowOpened(IWorkbenchWindow window) {
		}
	}

	/**
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		if (monitor.isCanceled()) {
			return;
		}
		
		// get variable context
		ExpandVariableContext resourceContext = ExternalToolsUtil.getVariableContext();

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
		
		String[] envp = ExternalToolsUtil.getEnvironment(configuration, resourceContext);
		
		if (monitor.isCanceled()) {
			return;
		}
		
		if (windowListener == null) {
			windowListener= new ProgramLaunchWindowListener();
			PlatformUI.getWorkbench().addWindowListener(windowListener);
		}
		Process p = DebugPlugin.exec(cmdLine, workingDir, envp);
		IProcess process = null;
		
		// add process type to process attributes
		Map processAttributes = new HashMap();
		String programName = location.lastSegment();
		String extension = location.getFileExtension();
		if (extension != null) {
			programName = programName.substring(0, programName.length() - (extension.length() + 1));
		}
		programName = programName.toLowerCase();
		processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);
		
		if (p != null) {
			process = DebugPlugin.newProcess(launch, p, location.toOSString(), processAttributes);
		}
		process.setAttribute(IProcess.ATTR_CMDLINE, generateCommandLine(cmdLine));
		
		if (ExternalToolsUtil.isBackground(configuration)) {
			// refresh resources after process finishes
			if (ExternalToolsUtil.getRefreshScope(configuration) != null) {
				BackgroundResourceRefresher refresher = new BackgroundResourceRefresher(configuration, process, resourceContext);
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
			ExternalToolsUtil.refreshResources(configuration, resourceContext, monitor);
		}
	}
	
	private String generateCommandLine(String[] commandLine) {
		if (commandLine.length < 1) {
			return ""; //$NON-NLS-1$
		}
		StringBuffer buf= new StringBuffer(commandLine[0]);
		for (int i= 1; i < commandLine.length; i++) {
			buf.append(' ');
			buf.append(commandLine[i]);
		}	
		return buf.toString();
	}	
	
}
