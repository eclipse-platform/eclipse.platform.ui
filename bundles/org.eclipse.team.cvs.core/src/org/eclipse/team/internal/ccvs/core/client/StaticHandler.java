package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * Handles any "Set-static-directory" and "Clear-static-directory" responses
 * from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Set-static-directory myproject/ \n
 *   /u/cvsroot/myproject/ \n
 *   [...]
 * </pre>
 * Then we set or clear the static flag of the folder "myproject",
 * automatically creating it if it does not exist locally,
 * </p>
 */
class StaticHandler extends ResponseHandler {
	private final boolean setStaticDirectory;

	public StaticHandler(boolean setStaticDirectory) {
		this.setStaticDirectory = setStaticDirectory;
	}

	public String getResponseID() {
		if (setStaticDirectory) {
			return "Set-static-directory"; //$NON-NLS-1$
		} else {
			return "Clear-static-directory"; //$NON-NLS-1$
		}
	}

	public void handle(Session session, String localDir,
		IProgressMonitor monitor) throws CVSException {
		// read additional data for the response
		String repositoryDir = session.readLine();

		// create the directory then set or clear the static flag
		Assert.isTrue(repositoryDir.endsWith("/")); //$NON-NLS-1$
		repositoryDir = repositoryDir.substring(0, repositoryDir.length() - 1);
		ICVSFolder folder = createFolder(session, localDir, repositoryDir);
		FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
		folder.setFolderSyncInfo(new FolderSyncInfo(syncInfo.getRepository(),
			syncInfo.getRoot(), syncInfo.getTag(), setStaticDirectory));
	}
}

