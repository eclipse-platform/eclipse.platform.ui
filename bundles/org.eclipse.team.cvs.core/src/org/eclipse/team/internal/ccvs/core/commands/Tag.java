package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Assert;

public class Tag extends Command {

	/**
	 * Constructor for Tag.
	 * @param responseDispatcher
	 * @param requestSender
	 */
	public Tag(ResponseDispatcher responseDispatcher, RequestSender requestSender) {
		super(responseDispatcher, requestSender);
	}

	/**
	 * @see ICommand#getName()
	 */
	public String getName() {
		return RequestSender.TAG;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.TAG;
	}

	/**
	 * @see Command#sendRequestsToServer(IProgressMonitor)
	 */
	protected void sendRequestsToServer(IProgressMonitor monitor)
		throws CVSException {

		// Either we got parameters or the folder we are in is an cvsFolder
		Assert.isTrue(getArguments().length > 1  ||
					  getRoot().isCVSFolder()); 
		
		// Get the folders we want to work on, ignoring the first argument
		IManagedResource[] mWorkResources = getWorkResources(1);
		
		// Send all folders that are already managed to the server
		sendFileStructure(mWorkResources,monitor,false,false,false);
		sendHomeFolder();
	}
}

