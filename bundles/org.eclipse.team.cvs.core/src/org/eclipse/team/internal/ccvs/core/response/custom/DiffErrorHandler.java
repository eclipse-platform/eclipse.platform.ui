package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.response.ResponseHandler;

public class DiffErrorHandler extends ResponseHandler {

	public static final String NAME = "E";
	protected OutputStream out;
	private List errors;

	public DiffErrorHandler(List errors) throws CVSException {
		this.errors = errors;
	}
	public String getName() {
		return NAME;
	}
	public void handle(
		Connection context,
		PrintStream messageOutput,
		IManagedFolder mRoot,
		IProgressMonitor monitor)
			throws CVSException {

		String line = context.readLine();
		// ignore these errors for now - this is used only with the diff
		// request and the errors can be safely ignored.
		if(!line.startsWith("cvs server:")) {
			errors.add(line);
		}
	}
}
