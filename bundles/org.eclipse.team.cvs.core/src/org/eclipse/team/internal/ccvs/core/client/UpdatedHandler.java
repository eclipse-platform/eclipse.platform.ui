package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.EntryFileDateFormat;

/**
 * Handles any "Updated" and "Merged" responses
 * from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Updated ???\n
 *   [...]
 * </pre>
 * Then 
 * </p>
 */

/**
 * Does get information about the file that is updated
 * and the file-content itself and puts it on the fileSystem.
 * 
 * The difference beetween the "Updated" and the "Merged" is, that
 * an "Merged" file is not going to be up-to-date after the operation.
 * 
 * Requiers a exisiting parent-folder.
 */
class UpdatedHandler extends ResponseHandler {
	private static final EntryFileDateFormat dateFormatter = new EntryFileDateFormat();
	private static final String READ_ONLY_FLAG = "u=rw"; //$NON-NLS-1$
	private final boolean updateResponse;
	
	public UpdatedHandler(boolean updateResponse) {
		this.updateResponse = updateResponse;
	}
	
	public String getResponseID() {
		if (updateResponse) {
			return "Updated"; //$NON-NLS-1$
		} else {
			return "Merged"; //$NON-NLS-1$
		}
	}

	public void handle(Session session, String localDir,
		IProgressMonitor monitor) throws CVSException {
		// read additional data for the response
		String repositoryFile = session.readLine();
		String entryLine = session.readLine();
		String permissionsLine = session.readLine();

		// clear file update modifiers
		Date modTime = session.getModTime();
		session.setModTime(null);
		
		// Get the local file
		String fileName =
			repositoryFile.substring(repositoryFile.lastIndexOf("/") + 1); //$NON-NLS-1$
		ICVSFolder mParent = session.getLocalRoot().getFolder(localDir);
		Assert.isTrue(mParent.exists());
		ICVSFile mFile = mParent.getFile(fileName);
		
		boolean binary = entryLine.indexOf("/" + ResourceSyncInfo.BINARY_TAG) != -1; //$NON-NLS-1$
		boolean readOnly = permissionsLine.indexOf(READ_ONLY_FLAG) == -1;
		
		session.receiveFile(mFile, binary, monitor);
		if (readOnly) mFile.setReadOnly();
		
		// Set the timestamp in the file, set the result in the fileInfo
		String timestamp = null;
		if (modTime != null) timestamp = dateFormatter.formatDate(modTime);
		mFile.setTimeStamp(timestamp);
		if (updateResponse) {
			timestamp = mFile.getTimeStamp();
		} else {
			timestamp = ResourceSyncInfo.RESULT_OF_MERGE + mFile.getTimeStamp();
		}			
		mFile.setSyncInfo(new ResourceSyncInfo(entryLine, permissionsLine, timestamp));

		Assert.isTrue(mFile.isModified() != updateResponse);
	}
}