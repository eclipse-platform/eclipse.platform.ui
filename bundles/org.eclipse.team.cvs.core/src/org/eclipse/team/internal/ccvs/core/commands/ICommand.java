package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;

/**
 * Represents a command of the cvs-client.
 * 
 * It gets the information provided on the command-line
 * and has to communicate to the server.
 * 
 * Normaly the response of the server is handeld by GeneralResponseHandler.
 * 
 * If custom-response-handling is needed the class should register a custom 
 * handler at the commandExecuter.
 */

interface ICommand {

	/**
	 * Runs the command.
	 * 
	 * @param global Options is allowed to have null-elements for convinience (all the others are not)
	 * @see CommandExecuter#execute(String command, IConnection, String[], String[], ICvsResource, OutputStream)
	 */
	void execute(String[] globalOptions, 
					String[] localOptions, 
					String[] arguments, 
					IManagedFolder mRoot,
					IProgressMonitor monitor, 
					PrintStream messageOut)
					throws CVSException;

	/**
	 * Returns the responses type. This is the name of
	 * the CVS command in String-Form (lowcase, like the
	 * command in the cvs-client)
	 */
	public String getName();

	/**
	 * Returns the name of the request that is send in order to
	 * start this command.
	 * This can be different from the name.
	 * e.g. the cvs-command "commit" sends "ci" to the server.
	 */
	public String getRequestName();	
}