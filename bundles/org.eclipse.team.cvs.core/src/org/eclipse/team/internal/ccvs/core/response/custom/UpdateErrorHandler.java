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
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
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
	
	public static final String SERVER_PREFIX = "cvs server: ";
	public static final String SERVER_ABORTED_PREFIX = "cvs [server aborted]: ";

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
						ICVSFolder mRoot,
						IProgressMonitor monitor) throws CVSException {
		String line = context.readLine();
		if (line.startsWith(SERVER_PREFIX)) {
			// Strip the prefix from the line
			String message = line.substring(SERVER_PREFIX.length());
			if (message.startsWith("Updating")) {
				if (updateMessageListener != null) {
					IPath path = new Path(message.substring(8));
					updateMessageListener.directoryInformation(path, false);
				}
			} else if (message.startsWith("skipping directory")) {
				if (updateMessageListener != null) {
					IPath path = new Path(message.substring(18));
					updateMessageListener.directoryDoesNotExist(path);
				}
			} else if (message.startsWith("New directory")) {
				if (updateMessageListener != null) {
					IPath path = new Path(message.substring(15, message.indexOf('\'', 15)));
					updateMessageListener.directoryInformation(path, true);
				}
			} else if (message.endsWith("is no longer in the repository")) {
				if (updateMessageListener != null) {
					String filename = message.substring(0, message.indexOf(' '));
					updateMessageListener.fileDoesNotExist(filename);
				}
			} else if (!message.startsWith("cannot open directory")
					&& !message.startsWith("nothing known about")) {
				errors.add(new Status(IStatus.ERROR, CVSProviderPlugin.ID, CVSException.IO_FAILED, line, null));
			}
		} else if (line.startsWith(SERVER_ABORTED_PREFIX)) {
			// Strip the prefix from the line
			String message = line.substring(SERVER_ABORTED_PREFIX.length());
			if (message.startsWith("no such tag")) {
				// This is reported from CVS when a tag is used on the update there are no files in the directory
				// To get the folders, the update request should be re-issued for HEAD
				// XXX should we add special handling or just let the caller hande the error
			} 
			errors.add(new Status(IStatus.ERROR, CVSProviderPlugin.ID, CVSException.IO_FAILED, line, null));
		}
	}
}