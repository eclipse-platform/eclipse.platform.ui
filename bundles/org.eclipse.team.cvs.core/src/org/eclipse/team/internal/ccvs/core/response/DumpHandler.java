package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;

/**
 * The Dump-Handler reads the rest of the 
 * line out of the connection and ignores it
 */
class DumpHandler extends ResponseHandler {

	String name;
	
	public DumpHandler(String name) {
		this.name = name;
	}
	
	/**
	 * @see IResponseHandler#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see IResponseHandler#handle(Connection, OutputStream, ICVSFolder)
	 */
	public void handle(
		Connection connection,
		PrintStream messageOutput,
		IManagedFolder mRoot)
		throws CVSException {

		connection.readLine();

	}


}

