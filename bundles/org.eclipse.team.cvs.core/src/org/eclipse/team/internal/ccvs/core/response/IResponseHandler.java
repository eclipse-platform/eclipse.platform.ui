package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;

/**
 * Represents an handler for a specific response of the 
 * server. 
 * e.g. an handler could get information out of the pipe 
 * and write it down to disk.
 */
public interface IResponseHandler {

	static final char BLANK_DELIMITER = ' ';

	/**
	 * Returns the responses type. This is the name of
	 * the CVS response in <code>String</code> form.
	 */
	public String getName();
	
	/**
	 * Handle the given response from the server.
	 */
	public void handle(Connection connection, 
							PrintStream messageOutput,
							ICVSFolder mRoot,
							IProgressMonitor monitor) 
							throws CVSException;
}


