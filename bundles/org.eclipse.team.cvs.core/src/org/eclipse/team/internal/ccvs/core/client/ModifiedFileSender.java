package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * Visit the CVS file structure, only sending files if they are modified.
 */
class ModifiedFileSender extends FileStructureVisitor {

	public ModifiedFileSender(Session session, IProgressMonitor monitor) {
		super(session, false, true, monitor);
	}
	
	/**
	 * Override sendFile to only send modified files
	 */
	protected void sendFile(ICVSFile mFile) throws CVSException {

		// Only send the file if its modified
		if (mFile.isModified()) {
			super.sendFile(mFile);
		}
	}
	
	protected String getSendFileTitleKey() {
		return null;
	}
}
