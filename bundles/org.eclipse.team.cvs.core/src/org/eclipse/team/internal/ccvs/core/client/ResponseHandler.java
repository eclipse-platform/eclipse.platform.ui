/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;


import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Handles server responses that arise as a result of issuing a request
 * (usually a command) to a CVS server.  The processing of each such
 * response is deferred to subclasses.
 */
public abstract class ResponseHandler {
	/**
	 * Returns the text string of the server response handled by this object.
	 * @return the id
	 */
	public abstract String getResponseID();

	/**
	 * Handles a server response.
	 * <p>
	 * Suppose as a result of performing a command the CVS server responds
	 * as follows:<br>
	 * <pre>
	 *   [...]
	 *   Clear-sticky myDirectory \n
	 *   /u/cvsroot/myDirectory \n
	 *   [...]
	 * </pre>
	 * Then the <code>handle</code> method of the <code>ResponseHandler</code>
	 * for <em>Clear-sticky</em> will be invoked with <code>argument</code>
	 * set to <em>"myDirectory"</em>.  It must then read the remaining
	 * response data from the connection (<em>"/u/cvsroot/myDirectory"</em>
	 * including the newline) and take any necessary action.
	 * </p><p>
	 * Note:  The type and quantity of additional data that must be read
	 * from the connection varies on a per-response basis.
	 * </p>
	 * @param session the Session used for CVS communication
	 * @param argument the argument supplied with the response
	 * @param monitor the progress monitor for the current CVS command
	 */
	public abstract void handle(Session session, String argument,
		IProgressMonitor monitor) throws CVSException;
	
	/**
	 * Creates a new CVS folder.
	 * @param localDir the local path of the folder relative to root
	 * @param repositoryDir the remote path of the folder relative to the repository
	 * @return the new folder
	 */
	protected static ICVSFolder createFolder(
			Session session,
			String localDir, 
			String repositoryDir) throws CVSException {
		
		ICVSFolder folder = session.getLocalRoot().getFolder(localDir);
		if (!folder.exists() 
				&&  (!CVSProviderPlugin.getPlugin().getPruneEmptyDirectories() 
						|| !folder.getParent().isCVSFolder())) {
			// Only create the folder if pruning is disabled or the
			// folder's parent is not a CVS folder (which occurs on checkout).
			// When pruning is enabled, the folder will be lazily created
			// when it contains a file (see getExistingFolder)
			folder.mkdir();
		}
		if (! folder.isCVSFolder()) {
			folder.setFolderSyncInfo(new FolderSyncInfo(
				Util.getRelativePath(session.getRepositoryRoot(), repositoryDir),
				session.getCVSRepositoryLocation().getLocation(),
				null, false));
		}
		return folder;
	}

	protected ICVSFolder getExistingFolder(Session session, String localDir) throws CVSException {
			ICVSFolder mParent = session.getLocalRoot().getFolder(localDir);
			if (! mParent.exists()) {
				// First, check if the parent is a phantom
				IContainer container = (IContainer)mParent.getIResource();
				if (container != null) {
					// Create all the parents as need
					recreatePhatomFolders(mParent);
				}
			}
			return mParent;
		}

	/**
	 * Method recreatePhatomFolders.
	 * @param mParent
	 */
	private void recreatePhatomFolders(ICVSFolder folder) throws CVSException {
		ICVSFolder parent = folder.getParent();
		if (!parent.exists()) {
			recreatePhatomFolders(parent);
		}
		folder.mkdir();
	}

	/**
	 * Return as instance that can be used by an open session. Subclasses that contain
	 * session related state must override this message to return a copy of themselves.
	 */
	/* package */ ResponseHandler getInstance() {
		return this;
	}
}

