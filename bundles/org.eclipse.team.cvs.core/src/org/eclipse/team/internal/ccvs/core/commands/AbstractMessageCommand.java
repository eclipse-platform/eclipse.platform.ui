package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;

/**
 * Superclass for commands that do not change the structure on 
 * the local working copy (it can change the content of the files).<br>
 * Most of the subclasses are asking the server for response in 
 * message format (log, status)
 */
abstract class AbstractMessageCommand extends Command {

	/**
	 * Constructor for AbstractMessageCommand.
	 * @param responseDispatcher
	 * @param requestSender
	 */
	public AbstractMessageCommand(
		ResponseDispatcher responseDispatcher,
		RequestSender requestSender) {
		super(responseDispatcher, requestSender);
	}

	/**
	 * @see Command#sendRequestsToServer(IProgressMonitor)
	 */
	protected void sendRequestsToServer(IProgressMonitor monitor)
		throws CVSException {
			
		ICVSResource[] mWorkResources;

		// Get the folders we want to work on
		mWorkResources = getResourceArguments();
		
		// Send all folders that are already managed to the server
		sendFileStructure(mWorkResources,monitor,false,false);
		sendHomeFolder();			
	}

}

