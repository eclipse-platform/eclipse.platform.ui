package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Assert;

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
		FileStructureVisitor visitor;
		IManagedFile[] changedFiles;
		
		Assert.isTrue(allResourcesManaged());

		visitor = new FileStructureVisitor(requestSender,getRoot(),monitor,true,false);

		// Get the folders we want to work on
		mWorkResources = getWorkResources();
		
		// Send all changed files to the server	
		for (int i = 0; i < mWorkResources.length; i++) {
			mWorkResources[i].accept(visitor);
		}
		
		sendHomeFolder();
			
		// Send the changed files as arguments
		changedFiles = visitor.getSentFiles();
		for (int i = 0; i < changedFiles.length; i++) {
			requestSender.sendArgument(changedFiles[i].getRelativePath(getRoot()));
		}
 
	}
	
	/**
	 * We do not want to send the arguments here, because we send
	 * them in sendRequestsToServer (special handling).
	 */
	protected void sendArguments() throws CVSException {
		return;
	}

}

	