/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * Handles any "Set-sticky" and "Clear-stick" responses from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Set-sticky myproject/ \n
 *   /u/cvsroot/myproject/ \n
 *   Tsometag \n
 *   [...]
 * </pre>
 * Then we set or clear the sticky tag property of the folder "myproject",
 * automatically creating it if it does not exist locally,
 * </p>
 */
class StickyHandler extends ResponseHandler {
	private final boolean setSticky;
		
	public StickyHandler(boolean setSticky) {
		this.setSticky = setSticky;
	}

	public String getResponseID() {
		if (setSticky) {
			return "Set-sticky"; //$NON-NLS-1$
		} else {
			return "Clear-sticky"; //$NON-NLS-1$
		}
	}

	public void handle(Session session, String localDir,
		IProgressMonitor monitor) throws CVSException {
		// read additional data for the response
		String repositoryDir = session.readLine();
		String tag = null;
		if (setSticky) {
			tag = session.readLine();
			if (tag.length() == 0) tag = null; // FIXME: is this correct
		}

		// create the directory then set or clear the sticky tag
		Assert.isTrue(repositoryDir.endsWith("/")); //$NON-NLS-1$
		repositoryDir = repositoryDir.substring(0, repositoryDir.length() - 1);		
		try {
            ICVSFolder folder = createFolder(session, localDir, repositoryDir);
            FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
            // Added to ignore sync info for workspace root
            if (syncInfo == null) return;
            FolderSyncInfo newInfo = new FolderSyncInfo(syncInfo.getRepository(),
            	syncInfo.getRoot(), tag != null ? new CVSEntryLineTag(tag) : null,
            	syncInfo.getIsStatic());
            // only set the sync info if it has changed
            if (!syncInfo.equals(newInfo))
            	folder.setFolderSyncInfo(newInfo);
        } catch (CVSException e) {
            if (!handleInvalidResourceName(session, session.getLocalRoot().getFolder(localDir), e)) {
                throw e;
            }
        }
	}
}

