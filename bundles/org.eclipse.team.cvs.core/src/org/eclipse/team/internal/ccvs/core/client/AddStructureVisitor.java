package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * This visitor is used by the Add command to ensure that the parent
 * folder is sent along with the added resource.
 */
class AddStructureVisitor extends AbstractStructureVisitor {
	private boolean forceSend = false;
	private Set visitedFolders = new HashSet();
	private ICVSFolder lastVisitedFolder;
	
	public AddStructureVisitor(Session session, IProgressMonitor monitor) {
		super(session, false, true, monitor);
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

