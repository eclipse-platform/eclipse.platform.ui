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
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;
import org.eclipse.team.internal.ccvs.core.response.ResponseDispatcher;
import org.eclipse.team.internal.ccvs.core.util.Util;

class Import extends Command {

	/**
	 * Constructor for Import.
	 * @param responseDispatcher
	 * @param requestSender
	 */
	public Import(ResponseDispatcher responseDispatcher,
				   RequestSender requestSender) {
		super(responseDispatcher, requestSender);
	}

	/**
	 * @see Command#sendRequestsToServer(IProgressMonitor)
	 */
	protected void sendRequestsToServer(IProgressMonitor monitor)
		throws CVSException {
	 	
	 	String mode = null;
	 	String[] wrappers;
	 	String[] ignores;
	 	IManagedVisitor visitor;
	 	
		// If the arguments are not three, the server is going to
		// reject the request
		// NOTE: Yes, but at least the user would get better feedback!
		// We should be throwing a CVSException!
		Assert.isTrue(getArguments().length == 3);

		// At this point we need to know wether we need to send	the file
		// as a binary. The server will set the mode properly based on the wrapper option.
		if (Util.isOption(getLocalOptions(),FileProperties.BINARY_TAG)) {
			mode = FileProperties.BINARY_TAG;
		}

		ignores = Util.getOptions(getLocalOptions(),Client.IGNORE_OPTION,false);
		wrappers = Util.getOptions(getLocalOptions(),Client.WRAPPER_OPTION,false);
		
		visitor = new ImportStructureVisitor(requestSender,
										getRoot(),
										monitor,
										mode,
										ignores,
										wrappers);
		
		getRoot().accept(visitor);
		
		sendHomeFolder(false);
	}

	/**
	 * @see ICommand#getName()
	 */
	public String getName() {
		return RequestSender.IMPORT;
	}

	/**
	 * @see ICommand#getRequestName()
	 */
	public String getRequestName() {
		return RequestSender.IMPORT;
	}

}

