package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Special visitor which handles added and removed files in a special way.
 * Added resources are skipped. Deleted resources are sent as if they were not deleted.
 */
class TagFileSender extends FileStructureVisitor {

	public TagFileSender(Session session, IProgressMonitor monitor) {
		super(session, false, false, monitor);
	}
	
	/** 
	 * Override sendFile to provide custom handling of added and deleted resources.
	 * Added resources are skipped. Deleted resources are sent as if they were not deleted.
	 */
	protected void sendFile(ICVSFile mFile) throws CVSException {
		if (mFile.isManaged()) {
			// Send the parent folder if it hasn't been sent already
			sendFolder(mFile.getParent());
			// Send the file if appropriate
			ResourceSyncInfo info = mFile.getSyncInfo();
			if (info.isDeleted()) {
				info = new ResourceSyncInfo(info.getName(), info.getRevision(), info.getTimeStamp(), info.getKeywordMode(), info.getTag(), info.getPermissions());
			}
			if (! info.isAdded()) {
				session.sendEntry(info.getEntryLine(false, mFile.getTimeStamp()));
				boolean binary = info != null && KSubstOption.fromMode(info.getKeywordMode()).isBinary();
				session.sendIsModified(mFile, binary, monitor);
			}
		}
	}
}
