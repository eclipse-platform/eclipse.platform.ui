package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;

/**
 * Handles a "Removed" response from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Removed ??? \n
 *   [...]
 * </pre>
 * Then 
 * </p>
 */

/**
 * It removes the file from both the entries of the parent-folder
 * and from the local filesystem.
 */
class RemovedHandler extends ResponseHandler {
	public String getResponseID() {
		return "Removed"; //$NON-NLS-1$
	}

	public void handle(Session session, String localDir, IProgressMonitor monitor) throws CVSException {
		
		// read additional data for the response
		String repositoryFile = session.readLine();

		// Get the local file		
		String fileName = repositoryFile.substring(repositoryFile.lastIndexOf("/") + 1); //$NON-NLS-1$
		ICVSFolder mParent = session.getLocalRoot().getFolder(localDir);
		ICVSFile mFile = mParent.getFile(fileName);
		
		if ( ! mFile.isManaged()) {
			throw new CVSException(Policy.bind("RemovedHandler.invalid", new Path(localDir).append(fileName).toString())); //$NON-NLS-1$
		}
		
		// delete then unmanage the file
		if (mFile.isReadOnly()) mFile.setReadOnly(false);
		mFile.delete();
		mFile.unmanage(null);
	}
}

