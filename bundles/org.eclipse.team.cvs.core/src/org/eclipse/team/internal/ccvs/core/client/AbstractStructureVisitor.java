package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * An ICVSResourceVisitor that is superclass to all ICVSResourceVisitor's used
 * by Command and it's subclasses.
 * Provides helper methods to send files and folders with modifications
 * to the server.
 */
abstract class AbstractStructureVisitor implements ICVSResourceVisitor {
	protected final Session session;
	
	// The last folder that has already been sent to the server during this visit
	private ICVSFolder lastFolderSend;

	// Subclasses can use to show visitor progress
	protected final IProgressMonitor monitor;

	public AbstractStructureVisitor(Session session, IProgressMonitor monitor) {
		this.session = session;
		this.monitor = monitor;
	}

	/**
	 * Send the folder relative to the root to the server. Send all 
	 * appropiate modifier like Sticki, Questionable, Static-directory.
	 * <br>
	 * If this folder was send last, it is not resend again (there is 
	 * no advantage of doing so).
	 */
	protected void sendFolder(ICVSFolder mFolder, boolean constructFolder, boolean sendQuestionable) throws CVSException {

		String local;
		String remote;
		CVSEntryLineTag tag;

		// Do not send the same folder twice
		if (mFolder.equals(lastFolderSend)) {
			return;
		}

		local = mFolder.getRelativePath(session.getLocalRoot());

		if (constructFolder && mFolder.exists()) {
			session.sendConstructedDirectory(local, local);
			lastFolderSend = mFolder;
			return;
		}

		if (sendQuestionable && !mFolder.isCVSFolder()) {
			// This implies, that the mFolder exists. If we have not send the parent-folder of this 
			// folder we have to send the parent-folder to have this questionable below this parent-folder.
			Assert.isTrue(mFolder.getParent().isCVSFolder());
			sendFolder(mFolder.getParent(), constructFolder, sendQuestionable);
			session.sendQuestionable(mFolder.getName());
			return;
		}

		remote = mFolder.getRemoteLocation(session.getLocalRoot());

		if (remote == null) {
			return;
		}

		session.sendDirectory(local, remote);

		FolderSyncInfo info = mFolder.getFolderSyncInfo();
		if (info != null) {

			if (info.getIsStatic()) {
				session.sendStaticDirectory();
			}

			tag = info.getTag();

			if (tag != null && tag.getType() != tag.HEAD) {
				session.sendSticky(tag.toEntryLineFormat(false));
			}
		}

		// Remember, that we send this folder
		lastFolderSend = mFolder;
	}

	/*
	 * Send the information about the file to the server.
	 * 
	 * If the file is modified, its contents are sent as well.
	 */
	protected void sendFile(ICVSFile mFile, boolean sendQuestionable, String mode) throws CVSException {

		// Send the file's entry line to the server
		if (mFile.isManaged()) {
			session.sendEntry(mFile.getSyncInfo().getEntryLine(false));
		} else {
			// If the file is not managed, send a questionable to the server if the file exists locally
			// A unmanaged, locally non-existant file results from the explicit use of the file name as a command argument
			if (sendQuestionable) {
				if (mFile.exists()) {
					session.sendQuestionable(mFile.getName());
				}
				return;
			}
		}
		
		// If the file exists, send the appropriate indication to the server
		if (mFile.exists()) {
			if (mFile.isModified()) {
//				if (session.isNoLocalChanges()) {
//					session.sendIsModified(mFile);
//				} else {
					boolean binary = mode != null && mode.indexOf(ResourceSyncInfo.BINARY_TAG) != -1;
					session.sendModified(mFile, monitor, binary);
//				}
			} else {
				session.sendUnchanged(mFile.getName());
			}
		}
	}
}