package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;

class Commit extends Command {

	public Commit(ResponseDispatcher responseDispathcer,
					RequestSender requestSender) {
		
		super(responseDispathcer,requestSender);
	}

	/**
	 * @see ICommand#getName()
	 */	
	public String getName() {
		return RequestSender.CI;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.CI;
	}

	/**
	 * Send all files under the workingFolder as changed files to 
	 * the server.
	 * 
	 * @see Request#sendRequestsToServer(IProgressMonitor)
	 */		
	public void sendRequestsToServer(IProgressMonitor monitor) throws CVSException {
		
		IManagedResource[] mWorkResources;
		
		Assert.isTrue(allResourcesManaged());

		// Get the folders we want to work on
		mWorkResources = getWorkResources();
		
		// Send all changed files to the server	
		sendFileStructure(mWorkResources,monitor,true,false,false);		
		sendHomeFolder();

	}

}

	