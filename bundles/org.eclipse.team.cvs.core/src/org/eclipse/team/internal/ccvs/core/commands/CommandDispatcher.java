package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/** 
 * Class that acctually runs the commands that come form the 
 * "command-line". It searches the command and runs it with the
 * rest of the parameters.
 * 
 * @see CommandExecuter#execute(String command, IConnection, String[], String[], ICvsResource, OutputStream)
 */
public class CommandDispatcher {
	
	private Map commandPool;

	private ResponseDispatcher responseDispatcher;
	private RequestSender requestSender;
	
	/**
	 * Puts all the Commands in a container in order to have access
	 * to them when they are going to be executed
	 * 
	 * Generic approche to "plug in" new commands just by adding them
	 * to this constructor
	 */
	public CommandDispatcher(ResponseDispatcher responseDispatcher,
							RequestSender requestSender) {

		commandPool = new HashMap();
		
		registerCommand(new Update(responseDispatcher,requestSender));
		registerCommand(new Checkout(responseDispatcher,requestSender));
		registerCommand(new Commit(responseDispatcher,requestSender));
		registerCommand(new Import(responseDispatcher,requestSender));
		registerCommand(new Add(responseDispatcher,requestSender));
		registerCommand(new Remove(responseDispatcher,requestSender));
		registerCommand(new Status(responseDispatcher,requestSender));
		registerCommand(new Log(responseDispatcher,requestSender));
		registerCommand(new Tag(responseDispatcher,requestSender));
		registerCommand(new Admin(responseDispatcher,requestSender));
		registerCommand(new Diff(responseDispatcher,requestSender));

	}
	
	/**
	 * Internal helper-method to put the commands into
	 * the hashtabe
	 */
	private void registerCommand(ICommand command) {
		
		// Do not register commands twice
		Assert.isTrue(commandPool.get(command.getName()) == null);

		commandPool.put(command.getName(),command);
	}

	/**
	 * Runs the given command on the cvs server.
	 * 
	 * The only public method of the commands-package.
	 * 
	 * Preconditiones:
	 *   - all arguments non-null
	 *   - globalOptions, localOptions arguments can be empty Arrays
	 *   - the connection has to be set up
	 *   - for most commands:
	 *       root.isCVSFolder() = true ||
	 *       root.getChild(arguments[0]) = true
	 * 
	 * This method is not thread safe. In other words, this method is not to be 
	 * invoked concurrently with the same connection or command name.
	 */
	public void execute(String commandName,
					String[] globalOptions, 
					String[] localOptions, 
					String[] arguments, 
					ICVSFolder mRoot,
					IProgressMonitor monitor, 
					PrintStream messageOut) throws CVSException {
		
		ICommand command;

		Assert.isNotNull(commandPool.get(commandName));
		
		command = (ICommand) commandPool.get(commandName);
		command.execute(globalOptions,
					localOptions,
					arguments,
					mRoot,
					monitor, 
					messageOut);		
		
	}

}


