package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Handles a "Checked-in" response from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Checked-in ??? \n
 *   [...]
 * </pre>
 * Then 
 * </p>
 */
class CheckedInHandler extends ResponseHandler {
	public String getResponseID() {
		return "Checked-in"; //$NON-NLS-1$
	}

	public void handle(Session session, String localDir,
		IProgressMonitor monitor) throws CVSException {
		// read additional data for the response
		String repositoryFile = session.readLine();
		String entryLine = session.readLine();
		
		// clear file update modifiers
		session.setModTime(null);
		
		// Get the local file		
		String fileName = repositoryFile.substring(repositoryFile.lastIndexOf("/") + 1); //$NON-NLS-1$
		ICVSFolder mParent = session.getLocalRoot().getFolder(localDir);
		ICVSFile mFile = mParent.getFile(fileName);
		
		ResourceSyncInfo newInfo = mFile.getSyncInfo();
		
		if (newInfo==null) {
			// cvs add of a file
			newInfo = new ResourceSyncInfo(entryLine, null, null);
		} else {
			// commit of a changed file
			ResourceSyncInfo fileInfo = mFile.getSyncInfo();
			newInfo = new ResourceSyncInfo(entryLine, fileInfo.getPermissions(), mFile.getTimeStamp());
		}

		mFile.setSyncInfo(newInfo);
	}
}

