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
	protected void sendFile(ICVSFile mFile) throws CVSException {
		KSubstOption ksubst;
		ResourceSyncInfo info = mFile.getSyncInfo();
		boolean addedFile = (info==null);

		// Send the parent folder if it hasn't been sent already
		sendFolder(mFile.getParent());

		Policy.checkCanceled(monitor);

		if (addedFile) {
			ksubst = KSubstOption.fromPattern(mFile.getName());
			MutableResourceSyncInfo newInfo = new MutableResourceSyncInfo(mFile.getName(), ResourceSyncInfo.ADDED_REVISION);	
			newInfo.setKeywordMode(ksubst.toMode());
			info = newInfo;
		} else {
			// existing file
			ksubst = KSubstOption.fromMode(info.getKeywordMode());
		}
		session.sendEntry(info.getServerEntryLine(null));
		
		if (!mFile.exists()) {
			return;
		}

		if (mFile.isModified() || addedFile) {
			session.sendModified(mFile, ksubst.isBinary(), monitor);
		} else {
			session.sendUnchanged(mFile);
		}
	}			
}
