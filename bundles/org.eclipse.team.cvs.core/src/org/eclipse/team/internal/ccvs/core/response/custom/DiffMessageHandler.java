package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.response.ResponseHandler;

public class DiffMessageHandler extends ResponseHandler {

	/** The response key */
	public static final String NAME = "M";

	/**
	 * Handle the response. This response sends the message to the
	 * progress monitor using <code>IProgressMonitor.subTask(Strin)
	 * </code>.
	 */
	public DiffMessageHandler() throws CVSException {
	}
	/**
	 * @see ResponseHandler#getName()
	 */
	public String getName() {
		return NAME;
	}
	/**
	 * Handle the response.
	 * 
	 * Write lines that are not prefixed by "cvs server:" to the stream
	 */
	public void handle(
		Connection context,
		PrintStream messageOutput,
		IManagedFolder mRoot,
		IProgressMonitor monitor)
			throws CVSException {
				
		String line = context.readLine();
		if (line != null && !line.startsWith("cvs server:")) {
			messageOutput.println(line);
		}
	}
}