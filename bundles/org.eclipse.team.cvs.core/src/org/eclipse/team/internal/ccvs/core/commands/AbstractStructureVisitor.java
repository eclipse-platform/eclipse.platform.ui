package org.eclipse.team.internal.ccvs.core.commands;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.FileDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;

/**
 * An IManagedVisitor that is superclass to all IManagedVisitor's used
 * by Command and it's subclasses.
 * Provides helper methods to send files and folders with modifications
 * to the server.
 */
abstract class AbstractStructureVisitor implements IManagedVisitor {

	private final RequestSender requestSender;
	private final IManagedFolder mRoot;
	private final IProgressMonitor monitor;
	//The last folder that has already been sent to the server during this visit
	private IManagedFolder lastFolderSend;
	
	public AbstractStructureVisitor(RequestSender requestSender,
									IManagedFolder mRoot,
									IProgressMonitor monitor) {
										
		this.requestSender = requestSender;
		this.mRoot = mRoot;
		this.monitor = monitor;
	}
	
	/**
	 * Send the folder relative to the root to the server
	 */
	void sendFolder(IManagedFolder mFolder, 
					boolean contructFolder) 
					throws CVSException{

		String local;
		String remote;
		
		// Do not send the same folder twice
		if (mFolder.equals(lastFolderSend)) {
			return;
		}

		local = mFolder.getRelativePath(mRoot);
		
		if (contructFolder  && mFolder.exists()) {
			requestSender.sendConstructedDirectory(local,local);
			lastFolderSend = mFolder;
			return;
		}
		
		remote = mFolder.getRemoteLocation(mRoot);
		
		if (remote != null) {
			requestSender.sendDirectory(local, remote);
		} 
		
		// Remember, that we send this folder
		lastFolderSend = mFolder;
	}

	/**
	 * Send a file up to the server.
	 * If it is modified send the content as well.
	 */
	void sendFile(IManagedFile mFile, 
					boolean sendQuestionable,
					String mode) throws CVSException {
		
		boolean binary = mode!=null && 
						mode.indexOf(FileProperties.BINARY_TAG)!=-1;
		
		if (mFile.isManaged()) {
			requestSender.sendEntry(mFile.getFileInfo().getEntryLineForServer());
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
		
		if (mFile.isDirty()) {
			requestSender.sendModified(mFile,monitor,binary);
		} else {
			requestSender.sendUnchanged(mFile.getName());
		}		
	}
}

