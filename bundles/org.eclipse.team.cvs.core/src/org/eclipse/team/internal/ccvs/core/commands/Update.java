package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Util;

class Update extends Command {

	public Update(ResponseDispatcher responseDispatcher,
					RequestSender requestSender) {
		
		super(responseDispatcher,requestSender);
	}
	
	/**
	 * @see ICommand#getName()
	 */
	public String getName() {
		return RequestSender.UPDATE;
	}
	
	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.UPDATE;
	}
		
	public void sendRequestsToServer(IProgressMonitor monitor) throws CVSException {

		ICVSResource[] mWorkResources;

		// Get the folders we want to work on
		mWorkResources = getResourceArguments();
		
		// XXX other clients send this. Should we?
		// requestSender.writeLine("UseUnchanged");
		// requestSender.writeLine("Case");
		// requestSender.sendArgument("-u");	
		
		// Send all folders that are already managed to the server
		// even folders that are empty
		sendFileStructure(mWorkResources,monitor,false,true);
		sendHomeFolder();
				
	}
	
	/**
	 * On successful finish, prune empty directories if the -P or -D option was specified.
	 */
	protected void finished(boolean success) throws CVSException {
		// If we didn't succeed, don't do any post processing
		if (!success)
			return;
		// If we are pruning (-P) or getting a sticky copy using -D, then prune empty directories
		if (Util.isOption(getLocalOptions(), Client.PRUNE_OPTION) 
			|| Util.isOption(getLocalOptions(), Client.DATE_TAG_OPTION)) {
				
			// Get the folders we want to work on
			ICVSResource[] mWorkResources = getResourceArguments();
			// Delete empty directories
			ICVSResourceVisitor visitor = new PruneFolderVisitor();
			for (int i=0; i<mWorkResources.length; i++) {
				mWorkResources[i].accept(visitor);
			}
			
		}	
	}
}


