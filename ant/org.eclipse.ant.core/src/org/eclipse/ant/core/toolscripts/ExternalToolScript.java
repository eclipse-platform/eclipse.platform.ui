package org.eclipse.ant.core.toolscripts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.eclipse.ant.core.AntPlugin;
import org.eclipse.ant.core.IAntRunnerListener;
import org.eclipse.core.runtime.*;
/*
 * (c) Copyright 2002 IBM Corp.
 * All Rights Reserved.
 */

/**
 * A tool script that is simply an external command to run.
 */
public class ExternalToolScript extends ToolScript {
	protected static final String ARG_COMMAND_LINE = "CommandLine";
	protected String commandLine;
/**
 * Creates a new tool script that runs the given command.
 */
public ExternalToolScript(String command) {
	this.commandLine = command;
}
/**
 * Factory method for creating a tool script based on builder arguments.
 * Returns null if no corresponding tool script could be created.
 */
public static ToolScript createFromArguments(Map arguments) {
	String command = (String)arguments.get(ARG_COMMAND_LINE);
	if (command != null) {
		return new ExternalToolScript(command);
	}
	return null;
}
/**
 * @see ToolScript#execute(IProgressMonitor)
 */
protected void execute(IAntRunnerListener listener, IProgressMonitor monitor) throws CoreException {
	try {
		monitor.beginTask("Running: " + commandLine, IProgressMonitor.UNKNOWN);
		Process p = Runtime.getRuntime().exec(commandLine);
		boolean[] finished = new boolean[1];
		if (listener != null) {
			finished[0] = false;
			new Thread(getRunnable(p.getInputStream(), listener, Project.MSG_INFO, finished)).start();
			new Thread(getRunnable(p.getErrorStream(), listener, Project.MSG_ERR, finished)).start();
		}
		p.waitFor();
		finished[0] = true;
	} catch (Exception e) {
		String msg = e.getMessage();
		if (msg == null)
			msg = "Unknown problem: " + e.getClass().getName();
		throw new CoreException(new Status(IStatus.ERROR, AntPlugin.PI_ANT, 1, msg, e));
	} finally {
		monitor.done();
	}
}
protected void fillBuilderArguments(Map arguments) {
	arguments.put(ARG_COMMAND_LINE, commandLine);
}
/**
 * Returns the command that will be run.
 */
public String getCommand() {
	return commandLine;
}
public int getKind() {
	return KIND_EXTERNAL_COMMAND;
}
/**
 * Returns a runnable that is used to capture and print out a stream
 * from another process.
 */
protected Runnable getRunnable(final InputStream input, final IAntRunnerListener listener, final int severity, final boolean[] finished) {
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
					listener.messageLogged(sb.toString(), severity);
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
/**
 * Returns an NL-enabled string describing this command.
 */
public String toString() {
	return "External command (" + commandLine + ")";
}
}
