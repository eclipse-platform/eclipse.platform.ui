package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;

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

}