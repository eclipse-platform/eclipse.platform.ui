package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;

/**
 * ResponseHandler is an abstract class implementing the IResponseHandler.
 * 
 * At the moment it does just provide some additional helper-classes.
 */
public abstract class ResponseHandler implements IResponseHandler {
	
	public static final String SERVER_DELIM = "/";
	public static final String DUMMY_TIMESTAMP = "dummy timestamp";
	public static final String RESULT_OF_MERGE = "Result of merge+";

	/**
	 * Call the old method without a monitor. Either this method or
	 * the called method have to be overloaded, otherwise an 
	 * UnsupportedOperationException is thrown.<br>
	 * This is done for convinience to be able to keep the old methods
	 * that do not use a progress-monitor.
	 * 
	 * Handle the given response from the server.
	 */
	public void handle(Connection connection, 
							PrintStream messageOutput,
							IManagedFolder mRoot,
							IProgressMonitor monitor) 
							throws CVSException {
		
		handle(connection,messageOutput,mRoot);
	}
	
	/**
	 * This method throws an UnsupportedOperationException.
	 * To be overloaded
	 */
	public void handle(Connection connection, 
							PrintStream messageOutput,
							IManagedFolder mRoot) 
							throws CVSException {
		throw new UnsupportedOperationException();
	}
}

