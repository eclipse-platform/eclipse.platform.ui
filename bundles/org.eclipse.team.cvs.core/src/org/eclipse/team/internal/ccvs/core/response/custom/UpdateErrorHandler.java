package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.response.ResponseHandler;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.resources.*;

/**
 * This handler is used by the RemoteResource hierarchy to retrieve E messages
 * from the CVS server in order to determine the folders contained in a parent folder.
 */
public class UpdateErrorHandler extends ResponseHandler {

	public static final String NAME = "E";

	IUpdateMessageListener updateMessageListener;
	List errors;

	public UpdateErrorHandler(IUpdateMessageListener updateMessageListener, List errors) {
		this.updateMessageListener = updateMessageListener;
		this.errors = errors;
	}
	public String getName() {
		return NAME;
	}
	public void handle(Connection context, 
						PrintStream messageOutput,
						IManagedFolder mRoot,
						IProgressMonitor monitor) throws CVSException {
		String line = context.readLine();
		if (line.startsWith("cvs server: Updating")) {
			if (updateMessageListener != null) {
				IPath path = new Path(line.substring(21));
				updateMessageListener.directoryInformation(path, false);
			}
		} else if (line.startsWith("cvs server: skipping directory")) {
			if (updateMessageListener != null) {
				IPath path = new Path(line.substring(31));
				updateMessageListener.directoryDoesNotExist(path);
			}
		} else if (line.startsWith("cvs server: New directory")) {
			if (updateMessageListener != null) {
				IPath path = new Path(line.substring(27, line.indexOf('\'', 27)));
				updateMessageListener.directoryInformation(path, true);
			}
		} else if (!line.startsWith("cvs server: cannot open directory")
				&& !line.startsWith("cvs server: nothing known about")) {
			errors.add(new Status(IStatus.ERROR, CVSProviderPlugin.ID, CVSException.IO_FAILED, line, null));
		}
	}
}