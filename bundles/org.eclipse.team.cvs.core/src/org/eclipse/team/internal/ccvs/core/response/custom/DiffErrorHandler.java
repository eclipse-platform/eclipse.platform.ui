package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.response.ResponseHandler;

public class DiffErrorHandler extends ResponseHandler {

	public static final String NAME = "E";
	protected OutputStream out;

	public DiffErrorHandler() throws CVSException {
	}
	public String getName() {
		return NAME;
	}
	public void handle(
		Connection connection,
		PrintStream messageOutput,
		ICVSFolder mRoot,
		IProgressMonitor monitor)
			throws CVSException {

		String line = connection.readLine();
		// ignore these errors for now - this is used only with the diff
		// request and the errors can be safely ignored.
		if(!line.startsWith("cvs server:")) {
			connection.addError(new Status(IStatus.ERROR, CVSProviderPlugin.ID, CVSException.IO_FAILED, line, null));
		}
	}
}
