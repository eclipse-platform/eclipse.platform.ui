package org.eclipse.ui.externaltools.internal.program.model;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.IExternalToolRunner;
import org.eclipse.ui.externaltools.model.IRunnerContext;
import org.eclipse.ui.externaltools.model.IRunnerLog;

/**
 * Runs all external tools of type program.
 */
public final class ProgramRunner implements IExternalToolRunner {

	/**
	 * Creates a program runner
	 */
	public ProgramRunner() {
		super();
	}

	/**
	 * Handles exceptions that may occur while running.
	 */
	private void handleException(Exception e, MultiStatus status) {
		String msg = e.getMessage();
		if (msg == null)
			msg = ToolMessages.getString("ProgramRunner.internalErrorMessage"); //$NON-NLS-1$;
		status.merge(ExternalToolsPlugin.newErrorStatus(msg, e));
	}

	/* (non-Javadoc)
	 * Method declared in IExternalToolsRunner.
	 */
	public void run(IProgressMonitor monitor, IRunnerContext runnerContext, MultiStatus status) {
		// Runtime exec requires an array where the first element is
		// the file to run, and the remaining elements are the
		// arguments to past along.
		String[] args = runnerContext.getExpandedArguments();
		String[] commands = new String[args.length + 1];

		commands[0] = runnerContext.getExpandedLocation();
		System.arraycopy(args, 0, commands, 1, args.length);

		try {
			// Determine the working directory to use, if any
			File workingDirectory = null;
			if (runnerContext.getExpandedWorkingDirectory().length() > 0)
				workingDirectory = new File(runnerContext.getExpandedWorkingDirectory());

			startMonitor(monitor, runnerContext, IProgressMonitor.UNKNOWN);
			if (monitor.isCanceled())
				return;

			// Print out the command used to run the program.
			if (IRunnerLog.LEVEL_VERBOSE <= runnerContext.getLog().getFilterLevel()) {
				runnerContext.getLog().append(
					ToolMessages.getString("ProgramRunner.callingRuntimeExec"), //$NON-NLS-1$;
					IRunnerLog.LEVEL_VERBOSE);
				runnerContext.getLog().append(
					ToolMessages.format("ProgramRunner.program", new Object[] {commands[0]}), //$NON-NLS-1$;
					IRunnerLog.LEVEL_VERBOSE);
				for (int i = 1; i < commands.length; i++) {
					runnerContext.getLog().append(
						ToolMessages.format("ProgramRunner.argument", new Object[] {commands[i]}), //$NON-NLS-1$;
						IRunnerLog.LEVEL_VERBOSE);
				}
				if (workingDirectory != null) {
					runnerContext.getLog().append(
						ToolMessages.format("ProgramRunner.workDir", new Object[] {workingDirectory.toString()}), //$NON-NLS-1$;
						IRunnerLog.LEVEL_VERBOSE);
				}
			}
			
			// Run the program
			boolean[] finished = new boolean[] {false};
			Process p;
			if (workingDirectory != null)
				p = Runtime.getRuntime().exec(commands, null, workingDirectory);
			else
				p = Runtime.getRuntime().exec(commands);
				
			// Collect the program's output in the background
			if (runnerContext.getCaptureOutput()) {
				startThread(
					p.getInputStream(),
					runnerContext.getLog(),
					IRunnerLog.LEVEL_INFO,
					finished);
				startThread(
					p.getErrorStream(), 
					runnerContext.getLog(), 
					IRunnerLog.LEVEL_ERROR, 
					finished);
			}

			if (monitor.isCanceled()) {
				p.destroy();
			} else {
				p.waitFor();
					
				// Sleep to allow the two new threads to begin reading
				// the program-running process's input and error streams
				// before finished[0] is set to true. This is especially
				// necessary with short programs that execute quickly. If
				// finished[0] is set to true before the threads run,
				// nothing will be read from the input and error streams.
				Thread.sleep(300);
			}
				
			finished[0] = true;
		} catch (IOException e) {
			handleException(e, status);
		} catch (InterruptedException e) {
			handleException(e, status);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Starts a thread to capture the specified stream contents
	 * and log it.
	 */
	private void startThread(final InputStream input, final IRunnerLog log, final int level, final boolean[] finished) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					StringBuffer sb;
					while (!finished[0]) {
						sb = new StringBuffer();
						int c = input.read();
						while (c != -1) {
							sb.append((char)c);
							c = input.read();
						}
						log.append(sb.toString(), level);
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
					input.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				}
			}
		});
		
		t.start();
	}
	
	/**
	 * Starts the monitor to show progress while running.
	 */
	private void startMonitor(IProgressMonitor monitor, IRunnerContext runnerContext, int workAmount) {
		String label = ToolMessages.format("ProgramRunner.runningToolLabel", new Object[] {runnerContext.getName()}); //$NON-NLS-1$;
		monitor.beginTask(label, workAmount);
	}
}
