package org.eclipse.toolscript.core.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.IOException;
import java.io.InputStream;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.toolscript.ui.internal.ToolScriptMessages;

/**
 * Execute scripts that represent programs in the file
 * system.
 */
public class ProgramScriptRunner extends ToolScriptRunner {

	/**
	 * Creates an empty program script runner
	 */
	public ProgramScriptRunner() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared in ToolScriptRunner.
	 */
	public void execute(IProgressMonitor monitor, IToolScriptContext scriptContext) throws CoreException {
		String commandLine = scriptContext.getExpandedLocation() + " " + scriptContext.getExpandedArguments(); //$NON-NLS-1$;
		try {
			startMonitor(monitor, scriptContext);
			Process p = Runtime.getRuntime().exec(commandLine);
			boolean[] finished = new boolean[1];
			
			//
			// DO TO: This needs to be updated to use log document support
			//
//			finished[0] = false;
//			new Thread(getRunnable(p.getInputStream(), null, Project.MSG_INFO, finished)).start();
//			new Thread(getRunnable(p.getErrorStream(), null, Project.MSG_ERR, finished)).start();

			p.waitFor();
			finished[0] = true;
		} catch (Exception e) {
			handleException(e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns a runnable that is used to capture and print out a stream
	 * from another process.
	 */
	private Runnable getRunnable(final InputStream input, final BuildListener listener, final int severity, final boolean[] finished) {
		return new Runnable() {
			public void run() {
				try {
					StringBuffer sb;
					BuildEvent event = new BuildEvent((Task)null);
					while (!finished[0]) {
						sb = new StringBuffer();
						int c = input.read();
						while (c != -1) {
							sb.append((char)c);
							c = input.read();
						}
						event.setMessage(sb.toString(), severity);
						listener.messageLogged(event);
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
