package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;

/**
 * The UnsupportedHandler throws an error whenever
 * it is called.
 * It is made for a response, that we have to answer on
 * but it has to be registerd at a later time.
 */
class UnsupportedHandler extends ResponseHandler {
	
	String name;
	
	public UnsupportedHandler(String name) {
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
		PrintStream monitor,
		IManagedFolder mRoot)
		throws CVSException {
		throw new CVSException(Policy.bind("UnsupportedHandler.message"));
	}


}
