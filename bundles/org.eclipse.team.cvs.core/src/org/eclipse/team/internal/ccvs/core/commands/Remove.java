package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;

class Remove extends Command {

	/**
	 * Constructor for Remove.
	 * @param responseContainer
	 * @param requestSender
	 */
	public Remove(
		ResponseDispatcher responseContainer,
		RequestSender requestSender) {
		super(responseContainer, requestSender);
	}

	/**
	 * @see ICommand#getName()
	 */
	public String getName() {
		return RequestSender.REMOVE;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.REMOVE;
	}

	/**
	 * @see Command#sendRequestsToServer(IProgressMonitor)
	 */
	protected void sendRequestsToServer(IProgressMonitor monitor) 
													throws CVSException {
		
		ICVSResource[] mWorkResources;
		
		// Get the folders we want to work on
		mWorkResources = getResourceArguments();
		
		// Send all changed files to the server	
		sendFileStructure(mWorkResources,monitor,true,false);		
		sendHomeFolder();
	}
}

