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


import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * This visitor is used by the Add command to ensure that the parent
 * folder is sent along with the added resource.
 */
class AddStructureVisitor extends AbstractStructureVisitor {
	
	public AddStructureVisitor(Session session, LocalOption[] localOptions) {
		super(session, localOptions, false, true);
	}

	/**
	 * @see ICVSResourceVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(ICVSFile mFile) throws CVSException {
		
		// Send the parent folder
		sendFolder(mFile.getParent());
		
		// Sends the Is-modified request if it is supported, otherwise
		// the file contents are sent as binary.  The server does not
		// need the contents at this stage so this should not be a problem.
		session.sendIsModified(mFile, true, monitor);
		
	}

	/**
	 * @see ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder mFolder) throws CVSException {
		
		Assert.isNotNull(mFolder);
		
		// Send the parent folder
		sendFolder(mFolder.getParent());
		
		// Send the directory
		String localPath = mFolder.getRelativePath(session.getLocalRoot());
		String remotePath = mFolder.getRemoteLocation(session.getLocalRoot());
		session.sendDirectory(localPath, remotePath);
		
		// Record that we sent this folder
		recordLastSent(mFolder);
	}

}

