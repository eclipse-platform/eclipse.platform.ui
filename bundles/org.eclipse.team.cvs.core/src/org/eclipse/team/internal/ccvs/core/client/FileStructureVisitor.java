package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * This visitor send the fileStructure to the requestSender.
 * 
 * If accepted by an ICVSResource:<br>
 * Send all Directory under mResource as arguments to the server<br>
 * If accepted by a file:<br>
 * Send the file to the server<br>
 * <br>
 * Files that are changed are send with the content.
 * 
 * @param modifiedOnly sends files that are modified only to the server
 * @param emptyFolders sends the folder-entrie even if there is no file 
 		  to send in it
 */

class FileStructureVisitor extends AbstractStructureVisitor {

	private final boolean modifiedOnly;
	private final boolean emptyFolders;
	private final Set sentFiles;

	/**
	 * Constructor for the visitor
	 * 
	 * @param modifiedOnly sends files that are modified only to the server
	 * @param emptyFolders sends the folder-entrie even if there is no file to send in it
	 */
	public FileStructureVisitor(Session session,
		boolean modifiedOnly, boolean emptyFolders, IProgressMonitor monitor) {
		super(session, monitor);
		this.modifiedOnly = modifiedOnly;
		this.emptyFolders = emptyFolders;
		sentFiles = new HashSet();
	}

	/**
	 * @see ICVSResourceVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(ICVSFile mFile) throws CVSException {

		// We assume, that acceptChildren() does call all the files
		// and then the folder or first all the folders and then the
		// files and does not mix. This is specified as well.

		if (!modifiedOnly || mFile.isModified()) {
			// sendFile sends the folder if it is nessary
			sendFile(mFile);
		}
	}

	/**
	 * @see ICVSResourceVisitor#visitFolder(ICVSFolder)
	 */
	public void visitFolder(ICVSFolder mFolder) throws CVSException {

		ICVSFile[] files;
		ICVSFolder[] folders;

		if (emptyFolders) {
			// If we want to send empty folder, that just send it when
			// we come to it
			sendFolder(mFolder);
		}

		if ( ! mFolder.isCVSFolder() || ! mFolder.exists() || isOrphanedSubtree(mFolder)) {
			return;
		}

		// We have to do a manual visit to ensure that the questionable
		// folders are send before the normal

		files = mFolder.getFiles();

		for (int i = 0; i < files.length; i++) {
			files[i].accept(this);
		}

		folders = mFolder.getFolders();

		for (int i = 0; i < folders.length; i++) {
			if (!folders[i].isCVSFolder()) {
				folders[i].accept(this);
				folders[i] = null;
			}
		}

		for (int i = 0; i < folders.length; i++) {
			if (folders[i] != null) {
				folders[i].accept(this);
			}
		}
	}

	private void sendFile(ICVSFile mFile) throws CVSException {

		// Only if we know about the file, it is added to the
		// list of sended files, Questionables do not go into
		// the list
		if (mFile.isManaged()) {
			sentFiles.add(mFile);
		}

		// Send the folder if it hasn't been send so far
		sendFolder(mFile.getParent());

		if (mFile.getSyncInfo() == null) {
			sendFile(mFile, true, null);
		} else {
			sendFile(mFile, true, mFile.getSyncInfo().getKeywordMode());
		}
	}

	private void sendFolder(ICVSFolder mFolder) throws CVSException {
		sendFolder(mFolder, false, true);
	}

	/**
	 * Return all the files that have been send to the server
	 */
	public ICVSFile[] getSentFiles() {
		return (ICVSFile[]) sentFiles.toArray(new ICVSFile[sentFiles.size()]);
	}
}