package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;

/**
 * Response to a "Checked-in" form the server.
 * Does save the EntryLine that comes with the
 * response.
 */
class CheckedIn extends ResponseHandler {

	/**
	 * @see IResponseHandler#getName()
	 */
	public String getName() {
		return "Checked-in";
	}

	/**
	 * @see IResponseHandler#handle(Connection, OutputStream, ICVSFolder)
	 */
	public void handle(
		Connection connection,
		PrintStream messageOutput,
		IManagedFolder mRoot)
		throws CVSException {

		String entryLine;
		String localDirectory;
		String repositoryFilename;
		String fileName;
		boolean changeFile;
		
		IManagedFile mFile;
		IManagedFolder mParent;
		FileProperties fileInfo;
		
		// Read the info associated with the Updated response
		localDirectory = connection.readLine();
		repositoryFilename = connection.readLine();
		entryLine = connection.readLine();
		
		// Get the local file		
		fileName = repositoryFilename.substring(repositoryFilename.lastIndexOf("/") + 1);
		mParent = mRoot.getFolder(localDirectory);
		mFile = mParent.getFile(fileName);
		
		// Set the entry and do not change the permissions
		// CheckIn can be an response on adding a new file,
		// so we can not rely on having a fileInfo ...
		
		// In this case we do not save permissions, but as we
		// haven't got anything from the server we do not need
		// to. Saveing permissions is only cashing information
		// from the server.
		changeFile = mFile.getFileInfo() == null;
		
		// If the file is not on disk then we have got an removed
		// file and therefore a file that is dirty after the check-in
		// as well
		changeFile = changeFile || !mFile.exists();
		
		if (changeFile) {
			fileInfo = new FileProperties();
		} else {
			fileInfo = mFile.getFileInfo();
		}

		fileInfo.setEntryLine(entryLine);
		
		if (changeFile) {
			fileInfo.setTimeStamp(DUMMY_TIMESTAMP);
		} else {
			fileInfo.setTimeStamp(mFile.getTimeStamp());
		}			

		mFile.setFileInfo(fileInfo);
		
		Assert.isTrue(changeFile == mFile.isDirty());

	}

}

