package org.eclipse.team.internal.ccvs.core.requests;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.response.ResponseHandler;


/**
 * Takes the valid-requsts out of the stream and puts
 * it into the RequestSender.
 * Checking whether requests are acctually valid is to
 * be done in the future.
 */
class ValidRequestHandler extends ResponseHandler {
	
	RequestSender requestSender;
	
	public ValidRequestHandler(RequestSender requestSender) {
		this.requestSender = requestSender;
	}
	
	/**
	 * @see IResponseHandler#getName()
	 */
	public String getName() {
		return "Valid-requests";
	}

	/**
	 * @see IResponseHandler#handle(Connection, OutputStream, ICVSFolder)
	 */
	public void handle(
		Connection connection,
		PrintStream monitor,
		IManagedFolder mRoot)
		throws CVSException {
			
		// Set the ValidRequests of the requestSender
		requestSender.setValidRequest(connection.readLine());

	}

}
