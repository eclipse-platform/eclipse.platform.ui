package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.Util;

class Checkout extends Command {
	
	/**
	 * Pipe everything to the superclass
	 */
	public Checkout(ResponseDispatcher responseDispathcer,
					RequestSender requestSender) {
		super(responseDispathcer,requestSender);
	}
	
	/**
	 * @see Request#getName()
	 */
	public String getName() {
		return RequestSender.CHECKOUT;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.CHECKOUT;
	}
	
	/**
	 * Start the Checkout command:
	 *    Send the module that is going to be checked-out to the server 
	 *    by reading the name of the resource given
	 *    (This has to change to we give it the name of the modul and the
	 *    Checkout creates everything for us)
	 * 
	 * 
	 * @see Request#setUp(IRequestContext)
	 */
	protected void sendRequestsToServer(IProgressMonitor monitor) throws CVSException {
		
		// We need a folder to put the project(s) we checkout into
		Assert.isTrue(getRoot().isFolder());

		// Just send the homefolder, and do not look into
		// the CVS-Folder to send it to the server
		// (this could be changed to make it compatible)
		sendHomeFolder(false);
		
	}
	
	/**
	 * On sucessful finish, prune empty directories if 
	 * the -P option was specified (or is implied by -D or -r)
	 */
	protected void finished(boolean success) throws CVSException {
		// If we didn't succeed, don't do any post processing
		if (!success)
			return;
		// If we are retrieving the modules file, ignore other options
		if (Util.isOption(getLocalOptions(), "-c"))
			return;
		// If we are pruning (-P) or getting a sticky copy (-D or -r), then prune empty directories
		if (Util.isOption(getLocalOptions(), Client.PRUNE_OPTION)
				|| Util.isOption(getLocalOptions(), Client.DATE_TAG_OPTION)
				|| Util.isOption(getLocalOptions(), Client.TAG_OPTION)) {
			// Get the name of the resulting directory
			String dir = Util.getOption(getLocalOptions(), "-d", false);
			ICVSResource[] resources;
			if (dir == null)
				// Get the folders we want to work on from the arguments
				resources = getResourceArguments();
			else
				// Create the folder we want to work on from the -d option
				resources = new ICVSResource[] {getRoot().getFolder(dir)};
			// Delete empty directories
			ICVSResourceVisitor visitor = new PruneFolderVisitor();
			for (int i=0; i<resources.length; i++) {
				resources[i].accept(visitor);
			}
		}	
	}

}