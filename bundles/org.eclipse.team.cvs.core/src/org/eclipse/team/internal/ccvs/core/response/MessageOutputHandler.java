package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;

/**
 * The MessageOutputHandler sends the whoole line (incl. read token)
 * to the messageOutput (up the userof the client)
 */
class MessageOutputHandler extends ResponseHandler {

	String name;
	
	public MessageOutputHandler(String name) {
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
		ICVSFolder mRoot) 
		throws CVSException {
		
		messageOutput.println(connection.readLine());
			
	}
}

