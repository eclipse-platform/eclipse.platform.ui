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
package org.eclipse.team.tests.ccvs.ui;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Test;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.tests.harness.EclipseTestHarnessApplication;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.Workbench;

/**
 * A test harness with UI and logging support.
 * <pre>
 * Supported arguments:
 *   -test <suite>   : id of suite to run (must be plugged into extension point)
 *   -log <file>     : specify a file for logging
 *   -nolog          : do not write a log file
 *   -repeat <n>     : number of iterations to run
 *   -ignorefirst    : ignore (do not record) results from first iteration
 *   -purge          : purge all projects from the workspace before each iteration
 *   <anything else> : passed verbatim to the org.eclipse.ui.workbench application
 * </pre>
 */
public class EclipseUITestHarnessApplication extends EclipseTestHarnessApplication {
	protected boolean purgeWorkspace;
	protected boolean ignoreFirst;
	protected int repeatCount;
	protected LoggingTestResult logResult;
	
	/**
	 * Application entry point.
	 */
	public Object run(Object userArgs) throws Exception {
		PrintStream logStream = System.err;
		String logFilename = null;
		purgeWorkspace = false;
		ignoreFirst = false;
		repeatCount = 1;
		if (userArgs instanceof String[]) {
			// parse args, no error handling
			String[] args = (String[]) userArgs;
			List argsList = new ArrayList(args.length);
			for (int i = 0; i < args.length; ++i) {
				if ("-repeat".equals(args[i])) {
					repeatCount = Integer.parseInt(args[++i]);
				} else if ("-ignorefirst".equals(args[i])) {
					ignoreFirst = true;
				} else if ("-nolog".equals(args[i])) {
					logStream = null;
				} else if ("-log".equals(args[i])) {
					logFilename = args[++i];
				} else if ("-purge".equals(args[i])) {
					purgeWorkspace = true;
				} else {
					argsList.add(args[i]);
				}
			}
			userArgs = argsList.toArray(new String[argsList.size()]);
		}
		// setup logging
		if (logFilename != null) {
			File file = new File(logFilename);
			logStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFilename)));
		}
		logResult = new LoggingTestResult(logStream);
		try {
			logResult.startLog(System.currentTimeMillis(), getSDKBuildId());
			return launchWorkbench(userArgs);
		} finally {
			logResult.endLog();
			if (logFilename != null) logStream.close();
		}
	}
	
	/**
	 * Launches the Workbench UI.
	 */
	protected Object launchWorkbench(final Object userArgs) throws Exception {
		final Exception[] exception = new Exception[1];
		Workbench workbench = new Workbench() {
			/*** this code should be kept in sync with Workbench.runEventLoop() ***/
			protected void runEventLoop(Window.IExceptionHandler handler) {
				// Dispatch all events.
				Display display = Display.getCurrent();
				while (true) {
					try {
						if (!display.readAndDispatch())
							break;
					} catch (Throwable e) {
						break;
					}
				}
		
				// Run our hook.
				try {
					workbenchHook(this);
				} catch (Exception e) {
					exception[0] = e;
				}
				
				// Close the workbench.
				close();		
			}
		};
		Object result = workbench.run(userArgs);
		if (exception[0] != null) throw exception[0];
		return result;
	}

	/**
	 * Callback from Workbench if it launched successfully.
	 */
	protected Object workbenchHook(Workbench workbench) throws Exception {
		// run the underlying non-ui test launcher to locate and start the test cases
		return super.run(workbench.getCommandLineArgs());
	}
	
	/**
	 * Runs the specified test.  Called from the non-ui test launcher.
	 */
	protected void run(Test test) {
		for (int i = 0; i < repeatCount; ++i) {
			if (purgeWorkspace) purgeWorkspaceProjects();
			LoggingTestRunner runner = new LoggingTestRunner();
			runner.doRun(test, (i == 0 && ignoreFirst) ? null : logResult, false);
		}
	}
	
	/**
	 * Purges the projects in the workspace.
	 */
	public static void purgeWorkspaceProjects() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		// purge all known projects from the workspace
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0; i < projects.length; ++i) {
			IProject project = projects[i];
			try {
				project.delete(true, true, null);
			} catch (CoreException e) {
				System.err.println("Could not purge project: " + project.getName());
			}
		}
	}
	
	/**
	 * Gets the SDK build id.
	 */
	public static String getSDKBuildId() {	
		try {
			URL url = Platform.getPluginRegistry().getPluginDescriptor("org.eclipse.sdk").getInstallURL();
			url = new URL(url, "platform.ini");
			InputStream is = url.openStream();
			try {
				Properties sdkProperties = new Properties();
				sdkProperties.load(is);
				String buildId = sdkProperties.getProperty("buildID");
				if (buildId != null) return buildId;
			} finally {
				is.close();
			}
		} catch (Exception e) {
		}
		return "unknown";
	}
}
