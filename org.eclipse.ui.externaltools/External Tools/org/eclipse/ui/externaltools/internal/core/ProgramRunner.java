package org.eclipse.ui.externaltools.internal.core;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.externaltools.internal.ui.LogConsoleDocument;

/**
 * Execute external tools that represent programs in the file
 * system.
 */
public class ProgramRunner extends ExternalToolsRunner {

	/**
	 * Creates an empty program runner
	 */
	public ProgramRunner() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared in ExternalToolsRunner.
	 */
	public void execute(IProgressMonitor monitor, IRunnerContext runnerContext) throws CoreException, InterruptedException {
		String commandLine = runnerContext.getExpandedLocation() + " " + runnerContext.getExpandedArguments(); //$NON-NLS-1$;
		try {
			File workingDir = null;
			if (runnerContext.getExpandedWorkingDirectory().length() > 0)
				workingDir = new File(runnerContext.getExpandedWorkingDirectory());
			startMonitor(monitor, runnerContext, monitor.UNKNOWN);
			boolean[] finished = new boolean[1];
			
			finished[0] = false;
			Process p;
			if (workingDir != null)
				p = Runtime.getRuntime().exec(commandLine, null, workingDir);
			else
				p = Runtime.getRuntime().exec(commandLine);		
			new Thread(getRunnable(p.getInputStream(), LogConsoleDocument.getInstance(), LogConsoleDocument.MSG_INFO, finished, runnerContext.getShowLog())).start();
			new Thread(getRunnable(p.getErrorStream(), LogConsoleDocument.getInstance(), LogConsoleDocument.MSG_ERR, finished, runnerContext.getShowLog())).start();
	
			p.waitFor();
				
			// Sleep to allow the two new threads to begin reading
			// the program-running process's input and error streams
			// before finished[0] is set to true. This is especially
			// necessary with short programs that execute quickly. If
			// finished[0] is set to true before the threads run,
			// nothing will be read from the input and error streams.
			Thread.currentThread().sleep(200);
				
			finished[0] = true;
		} catch (IOException e) {
			handleException(e);
		} catch (InterruptedException e) {
			handleException(e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns a runnable that is used to capture and print out a stream
	 * from another process.
	 */
	private Runnable getRunnable(final InputStream input, final LogConsoleDocument document, final int severity, final boolean[] finished, final boolean showLog) {
		return new Runnable() {
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
						if (showLog)
							document.append(sb.toString(), severity);
						try {
							Thread.currentThread().sleep(100);
						} catch (InterruptedException e) {
						}
					}
					input.close();
				} catch (IOException e) {
					e.printStackTrace(System.out);
				}
			}
		};
	}

}
