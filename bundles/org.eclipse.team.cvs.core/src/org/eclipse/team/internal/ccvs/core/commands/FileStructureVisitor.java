package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;

/**
 * This visitor send the fileStructure to the requestSender.
 * 
 * If accepted by an IManagedResource:<br>
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

	/**
	 * Constructor for the visitor
	 * 
	 * @param modifiedOnly sends files that are modified only to the server
	 * @param emptyFolders sends the folder-entrie even if there is no file 
 	 	to send in it
	 */	
	public FileStructureVisitor(RequestSender requestSender,
									IManagedFolder mRoot,
									IProgressMonitor monitor,
									boolean modifiedOnly,
									boolean emptyFolders) {
										
		super(requestSender, mRoot, monitor);
		this.modifiedOnly = modifiedOnly;
		this.emptyFolders = emptyFolders;

	}
	
	/**
	 * @see IManagedVisitor#visitFile(IManagedFile)
	 */
	public void visitFile(IManagedFile mFile) throws CVSException {
		
		// We assume, that acceptChildren() does call all the files
		// and then the folder or first all the folders and then the
		// files and does not mix. This is specified as well.
		
		if (!modifiedOnly || mFile.isDirty()) {
			// sendFile sends the folder if it is nessary
			sendFile(mFile);
		}
	}

	/**
	 * @see IManagedVisitor#visitFolder(IManagedFolder)
	 */
	public void visitFolder(IManagedFolder mFolder) throws CVSException {
		
		if (emptyFolders) {
			// If we want to send empty folder, that just send it when
			// we come to it
			sendFolder(mFolder);
		}
		
		if (mFolder.exists()) {
			mFolder.acceptChildren(this);
		}
		
	}
	
	private void sendFile(IManagedFile mFile) throws CVSException {
		
		// Send the folder if it hasn't been send so far
		sendFolder(mFile.getParent());
		
		if (mFile.getFileInfo() == null) {
			sendFile(mFile,true,null);
		} else {
			sendFile(mFile,true,mFile.getFileInfo().getKeywordMode());
		}
	}
	
	private void sendFolder(IManagedFolder mFolder) throws CVSException{
		sendFolder(mFolder,false);
	}
}

