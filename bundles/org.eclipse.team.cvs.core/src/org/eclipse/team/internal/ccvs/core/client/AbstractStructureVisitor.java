/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.NotifyInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * An ICVSResourceVisitor that is superclass to all ICVSResourceVisitor's used
 * by Command and it's subclasses.
 * Provides helper methods to send files and folders with modifications
 * to the server.
 * 
 * This class does not perform a beginTask of done on the provided monitor.
 * It is used only to signal worl and subTask.
 */
abstract class AbstractStructureVisitor implements ICVSResourceVisitor {
	
	protected Session session;
	private ICVSFolder lastFolderSent;
	protected IProgressMonitor monitor;
	protected boolean sendQuestionable;
	protected boolean sendModifiedContents;
	private boolean sendBinary;

	public AbstractStructureVisitor(Session session, boolean sendQuestionable, boolean sendModifiedContents) {
		this(session, sendQuestionable, sendModifiedContents, true);
	}

	public AbstractStructureVisitor(Session session, boolean sendQuestionable, boolean sendModifiedContents, boolean sendBinary) {
		this.session = session;
		this.sendQuestionable = sendQuestionable;
		this.sendModifiedContents = sendModifiedContents;
		this.sendBinary = sendBinary;
	}
		
	/** 
	 * Helper method to indicate if a directory has already been sent to the server
	 */
	protected boolean isLastSent(ICVSFolder folder) {
		return folder.equals(lastFolderSent);
	}
	
	/** 
	 * Helper method to record if a directory has already been sent to the server
	 */
	protected void recordLastSent(ICVSFolder folder) {
		lastFolderSent = folder;
	}
	
	/** 
	 * Helper which indicates if a folder is an orphaned subtree. 
	 * That is, a directory which contains a CVS subdirectory but is
	 * not managed by its parent. The root directory of the session
	 * is not considered orphaned even if it is not managed by its
	 * parent.
	 */
	protected boolean isOrphanedSubtree(ICVSFolder mFolder) throws CVSException {
		return mFolder.isCVSFolder() && ! mFolder.isManaged() && ! mFolder.equals(session.getLocalRoot()) && mFolder.getParent().isCVSFolder();
	}
	
	/**
	 * Send the folder relative to the root to the server. Send all 
	 * appropiate modifier like Sticky, Questionable, Static-directory.
	 * <br>
	 * Folders will only be sent once.
	 */
	protected void sendFolder(ICVSFolder mFolder) throws CVSException {

		Policy.checkCanceled(monitor);
		
		boolean exists = mFolder.exists();
		FolderSyncInfo info = mFolder.getFolderSyncInfo();
		boolean isCVSFolder = info != null;
		
		// We are only interested in folders that exist or are CVS folders
		// A folder could be a non-existant CVS folder if it is a holder for outgoing file deletions
		if ( ! exists && ! isCVSFolder) return;
		
		// Do not send the same folder twice
		if (isLastSent(mFolder)) return;
		
		// Do not send virtual directories
        if (isCVSFolder && info.isVirtualDirectory()) {
			return;
		}

		String localPath = mFolder.getRelativePath(session.getLocalRoot());
		
		monitor.subTask(Policy.bind("AbstractStructureVisitor.sendingFolder", Util.toTruncatedPath(mFolder, session.getLocalRoot(), 3))); //$NON-NLS-1$
		
		// Deal with questionable directories
		boolean isQuestionable = exists && (! isCVSFolder || isOrphanedSubtree(mFolder));
		if (isQuestionable) {
			if (sendQuestionable) {
				// We need to make sure the parent folder was sent 
				sendFolder(mFolder.getParent());
				session.sendQuestionable(mFolder);
			}
			return;
		}

		// Send the directory to the server
		String remotePath = mFolder.getRemoteLocation(session.getLocalRoot());
		if (remotePath == null) {
			throw new CVSException(Policy.bind("AbstractStructureVisitor.noRemote")); //$NON-NLS-1$
		}
		session.sendDirectory(localPath, remotePath);

		// Send any directory properties to the server
		if (info != null) {

			if (info.getIsStatic()) {
				session.sendStaticDirectory();
			}

			CVSEntryLineTag tag = info.getTag();

			if (tag != null && tag.getType() != CVSTag.HEAD) {
				session.sendSticky(tag.toEntryLineFormat(false));
			}
		}

		// Record that we sent this folder
		recordLastSent(mFolder);
		
		monitor.worked(1);
	}

	/**
	 * Send the information about the file to the server.
	 * 
	 * If the file is modified, its contents are sent as well.
	 */
	protected void sendFile(ICVSFile mFile) throws CVSException {

		Policy.checkCanceled(monitor);

		// Send the parent folder if it hasn't been sent already
		sendFolder(mFile.getParent());
		
		// Send the file's entry line to the server
		byte[] syncBytes = mFile.getSyncBytes();
		boolean isManaged = syncBytes != null;
		
		if (isManaged) {
		    sendPendingNotification(mFile);
		} else {
			// If the file is not managed, send a questionable to the server if the file exists locally
			// A unmanaged, locally non-existant file results from the explicit use of the file name as a command argument
			if (sendQuestionable) {
				if (mFile.exists()) {
					session.sendQuestionable(mFile);
				}
				return;
			}
			// else we are probably doing an import so send the file contents below
		}
		
		// Determine if we need to send the contents.
		boolean sendContents = mFile.exists() && mFile.isModified(monitor);
		if (ResourceSyncInfo.isDeletion(syncBytes)) {
		    sendEntryLineToServer(mFile, syncBytes);
		} else if (sendContents) {
		    // Perform the send of modified contents in a sheduling rule to ensure that
		    // the contents are not modified while we are sending them
		    IResource resource = mFile.getIResource();
            try {
                if (resource != null)
                    Platform.getJobManager().beginRule(resource, monitor);
		        
				sendEntryLineToServer(mFile, syncBytes);
				if (mFile.exists() && mFile.isModified(null)) {
					boolean binary = ResourceSyncInfo.isBinary(syncBytes);
					if (sendModifiedContents) {
						session.sendModified(mFile, binary, sendBinary, monitor);
					} else {
						session.sendIsModified(mFile, binary, monitor);
					}
				} else {
					session.sendUnchanged(mFile);
				}
		    } finally {
		        if (resource != null)
		            Platform.getJobManager().endRule(resource);
		    }
		} else {
		    sendEntryLineToServer(mFile, syncBytes);
			session.sendUnchanged(mFile);
		}
		
		monitor.worked(1);
	}

    private void sendEntryLineToServer(ICVSFile mFile, byte[] syncBytes) throws CVSException {
        if (syncBytes != null) {
            String syncBytesToServer = ResourceSyncInfo.getTimestampToServer(syncBytes, mFile.getTimeStamp());
            session.sendEntry(syncBytes, syncBytesToServer);
        }
    }

    protected void sendPendingNotification(ICVSFile mFile) throws CVSException {
		NotifyInfo notify = mFile.getPendingNotification();
		if (notify != null) {
			sendFolder(mFile.getParent());
			session.sendNotify(mFile.getParent(), notify);
		}
	}
	
	/**
	 * This method is used to visit a set of ICVSResources. Using it ensures
	 * that a common parent between the set of resources is only sent once
	 */
	public void visit(Session session, ICVSResource[] resources, IProgressMonitor monitor) throws CVSException {
		
		// Sort the resources to avoid sending the same directory multiple times
		List resourceList = new ArrayList(resources.length);
		resourceList.addAll(Arrays.asList(resources));
		final ICVSFolder localRoot = session.getLocalRoot();
		Collections.sort(resourceList, new Comparator() {
			public int compare(Object object1, Object object2) {
				ICVSResource resource1 = (ICVSResource)object1;
				ICVSResource resource2 = (ICVSResource)object2;
				try {
					String path1 = resource1.getParent().getRelativePath(localRoot);
					String path2 = resource2.getParent().getRelativePath(localRoot);
					int pathCompare = path1.compareTo(path2);
					if (pathCompare == 0) {
						if (resource1.isFolder() == resource2.isFolder()) {
							return resource1.getName().compareTo(resource2.getName());
						} else if (resource1.isFolder()) {
							return 1;
						} else {
							return -1;
						}
					} else {
						return pathCompare;
					}
				} catch (CVSException e) {
					return resource1.getName().compareTo(resource2.getName());
				}
			}
		});

		// Create a progress monitor suitable for the visit
		int resourceHint = 64;
		monitor.beginTask(null, resourceHint);
		this.monitor = Policy.infiniteSubMonitorFor(monitor, resourceHint);
		try {
			// Visit all the resources
			this.monitor.beginTask(null, resourceHint);
			session.setSendFileTitleKey(getSendFileTitleKey());
			for (int i = 0; i < resourceList.size(); i++) {
				((ICVSResource)resourceList.get(i)).accept(this);
			}
		} finally {
			monitor.done();
		}
	}
	
	protected String getSendFileTitleKey() {
		return "AbstractStructureVisitor.sendingFile"; //$NON-NLS-1$
	}
}
