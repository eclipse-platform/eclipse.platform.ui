/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Pawel Piech (Wind River) - ported PDA Virtual Machine to Java (Bug 261400)
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.launcher;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.debug.examples.core.pda.DebugCorePlugin;
import org.eclipse.debug.examples.core.pda.model.PDADebugTarget;


/**
 * Launches PDA program on a PDA interpreter written in Perl
 */
public class PDALaunchDelegate extends LaunchConfigurationDelegate {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		//#ifdef ex1
//#		// TODO: Exercise 1 - Launch a command shell as a system process to echo "foo"
		//#elseif ex1_answer
//#		Process process = DebugPlugin.exec(new String[]{"cmd", "/C",  "\"echo foo\""}, null);
//#		new RuntimeProcess(launch, process, "Hello", null);
		//#else		
		
		List commandList = new ArrayList();
		
        // Get Java VM path
        String javaVMHome = System.getProperty("java.home");
        String javaVMExec = javaVMHome + File.separatorChar + "bin" + File.separatorChar + "java";
        if (File.separatorChar == '\\') {
            javaVMExec += ".exe";
        }   
        File exe = new File(javaVMExec);
        if (!exe.exists()) {
            abort(MessageFormat.format("Specified java VM executable {0} does not exist.", new Object[]{javaVMExec}), null);
        }
        commandList.add(javaVMExec);

        commandList.add("-cp");
        commandList.add(File.pathSeparator + DebugCorePlugin.getFileInPlugin(new Path("bin")));
        
        commandList.add("org.eclipse.debug.examples.pdavm.PDAVirtualMachine");

		// program name
		String program = configuration.getAttribute(DebugCorePlugin.ATTR_PDA_PROGRAM, (String)null);
		if (program == null) {
			abort("Perl program unspecified.", null);
		}
		
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(program));
		if (!file.exists()) {
			abort(MessageFormat.format("Perl program {0} does not exist.", new String[] {file.getFullPath().toString()}), null);
		}
		
		commandList.add(file.getLocation().toOSString());
		
		// if in debug mode, add debug arguments - i.e. '-debug requestPort eventPort'
		int requestPort = -1;
		int eventPort = -1;
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			requestPort = findFreePort();
			eventPort = findFreePort();
			if (requestPort == -1 || eventPort == -1) {
				abort("Unable to find free port", null);
			}
			commandList.add("-debug");
			commandList.add("" + requestPort);
			commandList.add("" + eventPort);
		}
		
		String[] commandLine = (String[]) commandList.toArray(new String[commandList.size()]);
		Process process = DebugPlugin.exec(commandLine, null);
		IProcess p = DebugPlugin.newProcess(launch, process, javaVMExec);
		// if in debug mode, create a debug target 
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			IDebugTarget target = new PDADebugTarget(launch, p, requestPort, eventPort);
			launch.addDebugTarget(target);
		}
		//#endif
	}
	
	/**
	 * Throws an exception with a new status containing the given
	 * message and optional exception.
	 * 
	 * @param message error message
	 * @param e underlying exception
	 * @throws CoreException
	 */
	private void abort(String message, Throwable e) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, DebugCorePlugin.PLUGIN_ID, 0, message, e));
	}
	
	/**
	 * Returns a free port number on localhost, or -1 if unable to find a free port.
	 * 
	 * @return a free port number on localhost, or -1 if unable to find a free port
	 */
	public static int findFreePort() {
		ServerSocket socket= null;
		try {
			socket= new ServerSocket(0);
			return socket.getLocalPort();
		} catch (IOException e) { 
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		return -1;		
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#buildForLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		return false;
	}	
}
