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
 * Handles a "Copy-file" response from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Copy-file myproject/ \n
 *   /u/cvsroot/myproject/oldfile.txt \n
 *   newfile.txt
 *   [...]
 * </pre>
 * Then we copy (or optionally rename) the local file "oldfile.txt" in
 * folder "myproject" to "newfile.txt".  This response is used to create
 * a backup copy of an existing file before merging in new changes.
 * </p>
 */
class CopyHandler extends ResponseHandler {
	public String getResponseID() {
		return "Copy-file"; //$NON-NLS-1$
	}

	public void handle(Session session, String localDir,
		IProgressMonitor monitor) throws CVSException {
		// read additional data for the response
		String repositoryFile = session.readLine();
		String newFile = session.readLine();
		if (session.isNoLocalChanges() || ! session.isCreateBackups()) return;

		// Get the local file		
		String fileName = repositoryFile.substring(repositoryFile.lastIndexOf("/") + 1); //$NON-NLS-1$
		ICVSFolder mParent = session.getLocalRoot().getFolder(localDir);
		ICVSFile mFile = mParent.getFile(fileName);

		Assert.isTrue(mParent.exists());
		Assert.isTrue(mFile.exists() && mFile.isManaged());
		
		// rename the file
		mFile.copyTo(newFile);
	}
}

