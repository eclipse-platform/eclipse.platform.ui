package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;

/**
 * Goes recursivly through the folders checks if they are empyty
 * and deletes them. Of course it is starting at the leaves of the
 * recusion (the folders that do not have subfolders).
 */
class PruneFolderVisitor implements ICVSResourceVisitor {
	
	private Session session;
	
	public PruneFolderVisitor() {
	}
	
	/**
	 * This method is used to visit a set of ICVSResources.
	 */
	public void visit(Session s, ICVSResource[] resources) throws CVSException {
		session = s;
		
		// Visit the resources
		for (int i = 0; i < resources.length; i++) {
			resources[i].accept(this);
		}
	}
	
	/**
	 * @see ICVSResourceVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(ICVSFile file) throws CVSException {
	}

	/**
	 * @see ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder folder) throws CVSException {
		// First prune any empty children
		folder.acceptChildren(this);
		// Then prune the folder if it is not the command root.
		// XXX Seems a bit inefficient to fetch the files and folders separately
		if ( ! folder.equals(session.getLocalRoot()) &&
			folder.getFiles().length == 0 && 
			folder.getFolders().length == 0) {
			folder.delete();
			folder.unmanage(null);
		}
	}
}