package org.eclipse.team.internal.ccvs.core.client;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * The diff command needs to send a file structure to the server that differs somewhat from the canonical
 * format sent by other commands. Instead of sending new files as questionables this class sends
 * new files as modified and fakes them being added. The contents are sent to the server and are 
 * included in the returned diff report.
 */
class DiffStructureVisitor extends FileStructureVisitor {
	
	public DiffStructureVisitor(Session session, IProgressMonitor monitor) {
		super(session, false, true, monitor);
	}
	
	/**
	 * Send unmanaged files as modified with a default entry line.
	 */
	protected void sendFile(ICVSFile mFile, String mode) throws CVSException {
		boolean binary = mode != null && KSubstOption.fromMode(mode).isBinary();
		boolean newFile = false;

		Policy.checkCanceled(monitor);

		if (mFile.isManaged()) {
			session.sendEntry(mFile.getSyncInfo().getServerEntryLine(null /*don't send merged timestamps*/));
		} else {
			MutableResourceSyncInfo info = new MutableResourceSyncInfo(mFile.getName(), ResourceSyncInfo.ADDED_REVISION);
			session.sendEntry(info.getServerEntryLine(null));
			newFile = true;
		}

		if (!mFile.exists()) {
			return;
		}

		if (mFile.isModified() || newFile) {
			session.sendModified(mFile, binary, monitor);
		} else {
			session.sendUnchanged(mFile);
		}
	}			
}
