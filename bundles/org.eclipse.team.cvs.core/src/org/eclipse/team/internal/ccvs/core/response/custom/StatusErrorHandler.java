package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.response.*;

public class StatusErrorHandler extends ResponseHandler {

	public static final String NAME = "E";

	private static boolean isFolder = false;
	private IStatusListener statusListener;
	private List errors;

	public StatusErrorHandler(IStatusListener statusListener) {
		this.statusListener = statusListener;
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
		if (line.startsWith("cvs server: Examining")) {
			isFolder = true;
			return;
		}
		if (isFolder && line.startsWith("cvs [server aborted]: could not chdir to")) {
			String folderPath = line.substring(41, line.indexOf(':', 42));
			// Pass null to listener indication that resource exists but does not a revision number
			if (statusListener != null)
				statusListener.fileStatus(
					new Path(folderPath).removeFirstSegments(1),
					IStatusListener.FOLDER_RIVISION);
			isFolder = false;
			return;
		}
		errors.add(line);
		return;
	}
}