package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;

/**
 * Reacts on the "Copy-file"-Response of the server.
 * Just copies the file as suggested by the server.<br>
 * NOTE: The handler acctually copies the file, what does not 
 * 		 seem to cause a problem, because it is only used for
 * 		 making copies for security on a merge.
 */
class CopyHandler extends ResponseHandler {

	/**
	 * @see IResponseHandler#getName()
	 */
	public String getName() {
		return "Copy-file";
	}

	/**
	 * @see IResponseHandler#handle(Connection, PrintStream, IManagedFolder)
	 */
	public void handle(
		Connection connection,
		PrintStream messageOutput,
		IManagedFolder mRoot)
		throws CVSException {
		
		String fileName;
		
		IManagedFolder mParent;
		IManagedFile mFile;
		IManagedFile mNewFile;
		
		// Read the info associated with the Updated response
		String localDirectory = connection.readLine();
		String repositoryFilename = connection.readLine();
		String newFilename = connection.readLine();

		// Get the local file		
		fileName = repositoryFilename.substring(repositoryFilename.lastIndexOf("/") + 1);
		mParent = mRoot.getFolder(localDirectory);
		mFile = mParent.getFile(fileName);

		Assert.isTrue(mParent.exists() && mParent.isCVSFolder());
		Assert.isTrue(mFile.exists() && mFile.isManaged());
		
		// Move the file to newFile (we know we do not need the
		// original any more anyway)
		mNewFile = mParent.getFile(newFilename);
		mFile.moveTo(mNewFile);
	}

}

