package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;

/**
 * Goes recursivly through the folders checks if they are empyty
 * and deletes them. Of course it is starting at the leaves of the
 * recusion (the folders that do not have subfolders).
 */
public class PruneFolderVisitor implements ICVSResourceVisitor {

	/**
	 * @see ICVSResourceVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(ICVSFile file) throws CVSException {
	}

	/**
	 * @see ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder folder) throws CVSException {
		folder.acceptChildren(this);
		if (folder.getFiles().length == 0 && 
			folder.getFolders().length == 0) {
			folder.delete();
			folder.unmanage();
		}
	}
}