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

/**
 * Visit the CVS file structure, only sending files if they are modified.
 */
class ModifiedFileSender extends FileStructureVisitor {

	private final Set modifiedFiles;
	
	public ModifiedFileSender(Session session, IProgressMonitor monitor) {
		super(session, false, true, monitor);
		modifiedFiles = new HashSet();
	}
	
	/**
	 * Override sendFile to only send modified files
	 */
	protected void sendFile(ICVSFile mFile) throws CVSException {
		// Only send the file if its modified
		if (mFile.isManaged() && mFile.isModified(null)) {
			super.sendFile(mFile);
			modifiedFiles.add(mFile);
		}
	}
	
	protected String getSendFileTitleKey() {
		return null;
	}
	
	/**
	 * Return all the files that have been send to the server
	 */
	public ICVSFile[] getModifiedFiles() {
		return (ICVSFile[]) modifiedFiles.toArray(new ICVSFile[modifiedFiles.size()]);
	}
}
