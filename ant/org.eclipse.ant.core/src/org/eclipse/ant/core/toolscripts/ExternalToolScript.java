package org.eclipse.ant.core.toolscripts;

import java.io.IOException;
import java.util.Map;

import org.eclipse.ant.core.AntPlugin;
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
protected void execute(IProgressMonitor monitor) throws CoreException {
	try {
		monitor.beginTask("Running: " + commandLine, IProgressMonitor.UNKNOWN);
		Process p = Runtime.getRuntime().exec(commandLine);
		p.waitFor();
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
 * Returns an NL-enabled string describing this command.
 */
public String toString() {
	return "External command (" + commandLine + ")";
}
}
