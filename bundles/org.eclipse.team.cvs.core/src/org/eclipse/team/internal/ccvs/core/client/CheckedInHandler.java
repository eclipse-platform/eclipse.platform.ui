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
		
		// Set the entry and do not change the permissions
		// CheckIn can be an response on adding a new file,
		// so we can not rely on having a fileInfo ...
		
		// In this case we do not save permissions, but as we
		// haven't got anything from the server we do not need
		// to. Saveing permissions is only cashing information
		// from the server.
		boolean changeFile = mFile.getSyncInfo() == null;
		
		// If the file is not on disk then we have got an removed
		// file and therefore a file that is dirty after the check-in
		// as well
		changeFile = changeFile || !mFile.exists();
		ResourceSyncInfo newInfo;
		
		if (changeFile) {
			newInfo = new ResourceSyncInfo(entryLine, null, ResourceSyncInfo.DUMMY_TIMESTAMP);
		} else {
			ResourceSyncInfo fileInfo = mFile.getSyncInfo();
			newInfo = new ResourceSyncInfo(entryLine, fileInfo.getPermissions(), mFile.getTimeStamp());
		}

		mFile.setSyncInfo(newInfo);
		
		// This doesn't work with remote files.
		//Assert.isTrue(changeFile == mFile.isModified());
	}
}

