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

public class NewEntryHandler extends ResponseHandler {

	/*
	 * @see ResponseHandler#getResponseID()
	 */
	public String getResponseID() {
		return "New-entry"; //$NON-NLS-1$
	}

	/*
	 * @see ResponseHandler#handle(Session, String, IProgressMonitor)
	 */
	public void handle(Session session, String localDir, IProgressMonitor monitor)
		throws CVSException {
			
		// read additional data for the response
		String repositoryFile = session.readLine();
		String entryLine = session.readLine();
		
		// Clear the recorded mod-time
		session.setModTime(null);
		
		// Get the local file		
		String fileName = repositoryFile.substring(repositoryFile.lastIndexOf("/") + 1); //$NON-NLS-1$
		ICVSFolder mParent = session.getLocalRoot().getFolder(localDir);
		ICVSFile mFile = mParent.getFile(fileName);

		ResourceSyncInfo fileInfo = mFile.getSyncInfo();
		ResourceSyncInfo newInfo = new ResourceSyncInfo(entryLine, fileInfo.getPermissions(), null);
		mFile.setSyncInfo(newInfo);
	}
}
