package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
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

	private final RequestSender requestSender;
	private final ICVSFolder mRoot;
	private final IProgressMonitor monitor;
	//The last folder that has already been sent to the server during this visit
	private ICVSFolder lastFolderSend;
	
	public AbstractStructureVisitor(RequestSender requestSender,
									ICVSFolder mRoot,
									IProgressMonitor monitor) {
										
		this.requestSender = requestSender;
		this.mRoot = mRoot;
		this.monitor = monitor;
	}
	
	/**
	 * Send the folder relative to the root to the server. Send all 
	 * appropiate modifier like Sticki, Questionable, Static-directory.
	 * <br>
	 * If this folder was send last, it is not resend again (there is 
	 * no advantage of doing so).
	 */
	void sendFolder(ICVSFolder mFolder, 
					boolean constructFolder,
					boolean sendQuestionable)
					throws CVSException{

		String local;
		String remote;
		CVSEntryLineTag tag;
		
		// Do not send the same folder twice
		if (mFolder.equals(lastFolderSend)) {
			return;
		}

		local = mFolder.getRelativePath(mRoot);
		
		if (constructFolder  && mFolder.exists()) {
			requestSender.sendConstructedDirectory(local,local);
			lastFolderSend = mFolder;
			return;
		}
		
		if (sendQuestionable && !mFolder.isCVSFolder()) {
			// This implies, that the mFolder exists
			
			// If we have not send the parent-folder of this 
			// folder we have to send the parent-folder to have
			// this questianable below this parent-folder
			Assert.isTrue(mFolder.getParent().isCVSFolder());
			sendFolder(mFolder.getParent(),constructFolder,sendQuestionable);
			
			requestSender.sendQuestionable(mFolder.getName());
			return;
		}
		
		remote = mFolder.getRemoteLocation(mRoot);
		
		if (remote == null) {
			return;
		}
		
		requestSender.sendDirectory(local, remote);
		
		FolderSyncInfo info = mFolder.getFolderSyncInfo();
		if (info != null) { 
			
			if (info.getIsStatic()) {
				requestSender.sendStaticDirectory();
			}
			
			tag = info.getTag();
			
			if (tag != null && tag.getType() != tag.HEAD) {
				requestSender.sendSticky(tag.toEntryLineFormat(false));
			} 
		}
	
		// Remember, that we send this folder
		lastFolderSend = mFolder;
	}

	/**
	 * Send a file up to the server.
	 * If it is modified send the content as well.
	 */
	void sendFile(ICVSFile mFile, 
					boolean sendQuestionable,
					String mode) throws CVSException {
		
		boolean binary = mode!=null && 
						mode.indexOf(ResourceSyncInfo.BINARY_TAG)!=-1;
		
		if (mFile.isManaged()) {
			requestSender.sendEntry(mFile.getSyncInfo().getEntryLine(false));
		} else if (sendQuestionable) {
			requestSender.sendQuestionable(mFile.getName());
			return;
			// The client does not do it and we do not know whether to do it
			// } else if (mode != null && !"".equals(mode)) {
			// requestSender.sendKopt(mode);
		}
		
		if (!mFile.exists()) {
			return;
		}
		
		if (mFile.isModified()) {
			requestSender.sendModified(mFile,monitor,binary);
		} else {
			requestSender.sendUnchanged(mFile.getName());
		}		
	}
}

