package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * Handles a "Remove-entry" response from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Remove-entry ??? \n
 *   [...]
 * </pre>
 * Then 
 * </p>
 */

/**
 * It removes the file from both the entries of the parent-folder. 
 * This happen, when the folder has allready been removed locally
 * what happens on a checkin that includes a removed file.
 */
class RemoveEntryHandler extends ResponseHandler {
	public String getResponseID() {
		return "Remove-entry"; //$NON-NLS-1$
	}

	public void handle(Session session, String localDir,
		IProgressMonitor monitor) throws CVSException {
		// read additional data for the response
		String repositoryFile = session.readLine();

		// Get the local file		
		String fileName = repositoryFile.substring(repositoryFile.lastIndexOf("/") + 1); //$NON-NLS-1$
		ICVSFolder mParent = session.getLocalRoot().getFolder(localDir);
		ICVSFile mFile = mParent.getFile(fileName);
		mFile.unmanage(null);
	}
}

