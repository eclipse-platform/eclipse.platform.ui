package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.response.ResponseHandler;

public class StatusErrorHandler extends ResponseHandler {

	public static final String NAME = "E";

	private static boolean isFolder = false;
	private IStatusListener statusListener;

	public StatusErrorHandler(IStatusListener statusListener) {
		this.statusListener = statusListener;
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
		if (line.startsWith("cvs server: conflict:")) {
			// We get this because we made up an entry line to send to the server
			// Just ignore it
			// XXX We should make this a warning!!!
			return;
		}
		if (line.startsWith("cvs server: Examining")) {
			isFolder = true;
			return;
		}
		if (isFolder && line.startsWith("cvs [server aborted]: could not chdir to")) {
			String folderPath = line.substring(41, line.indexOf(':', 42));
			// Pass null to listener indicating that the resource exists but does not have a revision number
			// (i.e. the resource is a folder)
			if (statusListener != null)
				// XXX We should be using that path relative to the root of the command (mRoot)!!!
				statusListener.fileStatus(new Path(folderPath).removeFirstSegments(1), IStatusListener.FOLDER_RIVISION);
			isFolder = false;
			return;
		}
		connection.addError(new Status(IStatus.ERROR, CVSProviderPlugin.ID, CVSException.IO_FAILED, line, null));
		return;
	}
}