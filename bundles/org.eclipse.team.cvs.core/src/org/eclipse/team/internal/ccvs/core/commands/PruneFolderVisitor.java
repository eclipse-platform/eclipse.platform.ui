package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;

/**
 * Goes recursivly through the folders checks if they are empyty
 * and deletes them. Of course it is starting at the leaves of the
 * recusion (the folders that do not have subfolders).
 */
public class PruneFolderVisitor implements IManagedVisitor {

	/**
	 * @see IManagedVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(IManagedFile file) throws CVSException {
	}

	/**
	 * @see IManagedVisitor#visitFolder(IManagedFolder)
	 */
	public void visitFolder(IManagedFolder folder) throws CVSException {
		folder.acceptChildren(this);
		if (folder.getFiles().length == 0 && 
			folder.getFolders().length == 0) {
			folder.setFolderInfo(null);
			folder.delete();
		}
	}

}

