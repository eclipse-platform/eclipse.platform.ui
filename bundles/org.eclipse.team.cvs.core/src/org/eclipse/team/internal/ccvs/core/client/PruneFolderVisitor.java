package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
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
	
	private ICVSResource currentVisitRoot;
	
	public PruneFolderVisitor() {
	}
	
	/**
	 * This method is used to visit a set of ICVSResources.
	 */
	public void visit(Session s, ICVSResource[] resources) throws CVSException {
		session = s;
		
		// Visit the resources
		for (int i = 0; i < resources.length; i++) {
			currentVisitRoot = resources[i];
			resources[i].accept(this);
		}
	}
	
	/**
	 * @see ICVSResourceVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(ICVSFile file) throws CVSException {
		pruneParentIfAppropriate(file);
	}

	/**
	 * @see ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder folder) throws CVSException {
		// First prune any empty children
		folder.acceptChildren(this);
		pruneFolderIfAppropriate(folder);
	}
	
	private void pruneFolderIfAppropriate(ICVSFolder folder) throws CVSException {
		if (folder.isManaged() &&
		 		! folder.equals(session.getLocalRoot()) &&
				folder.members(ICVSFolder.ALL_MEMBERS).length == 0) {
			folder.delete();
			folder.unmanage(null);
			pruneParentIfAppropriate(folder);
		}
	}
	
	private void pruneParentIfAppropriate(ICVSResource resource) throws CVSException {
		// If we are visiting the current visit root, prune the parent if appropriate
		if (CVSProviderPlugin.getPlugin().getPruneEmptyDirectories() && resource.equals(currentVisitRoot)) {
			currentVisitRoot = resource.getParent();
			pruneFolderIfAppropriate((ICVSFolder)currentVisitRoot);
		}
	}
}