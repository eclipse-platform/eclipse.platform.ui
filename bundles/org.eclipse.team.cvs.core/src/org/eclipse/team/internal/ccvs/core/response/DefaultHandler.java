package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;

/**
 * Response to the response that is not handled otherwise
 * 
 * Reads the rest of the line, checks for error and dumps
 * everything that it has read.
 */
class DefaultHandler extends ResponseHandler {

	/**
	 * @see IResponseHandler#getName()
	 */
	public String getName() {
		return "dump";
	}

	/**
	 * @see IResponseHandler#handle(Connection, OutputStream, ICVSFolder)
	 */
	public void handle(
		Connection connection,
		PrintStream messageOutput,
		IManagedFolder mRoot)
		throws CVSException {
		
		// FIXME look wether we need this or if the connection has
		//       appropiate handling
		
		// Check if the command is truncated because of
		// an closed connection
		if (connection.isClosed()) {
			throw new CVSException(Policy.bind("DefaultHandler.connectionClosed"));
		}
	
		if (connection.getLastUsedDelimiterToken() == BLANK_DELIMITER) {
			connection.readLine();
		}
	}


}

