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
import org.eclipse.team.internal.ccvs.core.ICVSResource;

/**
 * Visitor to send the local file structure to the CVS Server.
 * 
 * Files are sent as Unchanged or Modified.
 * Folders are sent if they contain files unless sendEmptyFolders is true
 * in which case all folders are sent.
 *
 * @param sendEmptyFolders sends the folder-entrie even if there is no file 
 		  to send in it
 */

class FileStructureVisitor extends AbstractStructureVisitor {

	private final boolean sendEmptyFolders;
	private final Set sentFiles;

	/**
	 * Constructor for the visitor
	 * 
	 * @param modifiedOnly sends files that are modified only to the server
	 * @param emptyFolders sends the folder-entrie even if there is no file to send in it
	 */
	public FileStructureVisitor(Session session, boolean sendEmptyFolders, boolean sendModifiedContents, IProgressMonitor monitor) {
			
		super(session, true, sendModifiedContents, monitor);
		this.sendEmptyFolders = sendEmptyFolders;
		sentFiles = new HashSet();
	}

	/**
	 * @see ICVSResourceVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(ICVSFile mFile) throws CVSException {
		sendFile(mFile);
	}

	/**
	 * @see ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder mFolder) throws CVSException {

		if (sendEmptyFolders) {
			// If we want to send empty folder, that just send it when
			// we come to it
			sendFolder(mFolder);
		}

		boolean exists = mFolder.exists();
		boolean isCVSFolder = mFolder.isCVSFolder();
		
		// We are only interested in folders that exist or are CVS folders
		// A folder could be a non-existant CVS folder if it is a holder for outgoing file deletions
		if ( ! exists && ! isCVSFolder) return;
		
		if (exists &&  isOrphanedSubtree(mFolder)) {
			return;
		}

		// Send files, then the questionable folders, then the managed folders
		ICVSResource[] files = mFolder.members(ICVSFolder.FILE_MEMBERS);
		for (int i = 0; i < files.length; i++) {
			files[i].accept(this);
		}
		ICVSResource[] folders = mFolder.members(ICVSFolder.FOLDER_MEMBERS | ICVSFolder.UNMANAGED_MEMBERS);
		for (int i = 0; i < folders.length; i++) {
			folders[i].accept(this);
		}
		folders = mFolder.members(ICVSFolder.FOLDER_MEMBERS | ICVSFolder.MANAGED_MEMBERS);
		for (int i = 0; i < folders.length; i++) {
			folders[i].accept(this);
		}
	}
	
	protected void sendFile(ICVSFile mFile) throws CVSException {

		// Send the parent folder if it hasn't been sent already
		sendFolder(mFile.getParent());

		// Send the file
		super.sendFile(mFile);
		
		// Record all managed files we sent
		if (mFile.isManaged()) {
			sentFiles.add(mFile);
		}
	}

	/**
	 * Return all the files that have been send to the server
	 */
	public ICVSFile[] getSentFiles() {
		return (ICVSFile[]) sentFiles.toArray(new ICVSFile[sentFiles.size()]);
	}
}