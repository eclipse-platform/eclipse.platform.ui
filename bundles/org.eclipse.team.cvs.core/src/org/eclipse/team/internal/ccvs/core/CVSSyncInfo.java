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
package org.eclipse.team.internal.ccvs.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * CVSSyncInfo
 */
public class CVSSyncInfo extends SyncInfo {

	public CVSSyncInfo(IResource local, IRemoteResource base, IRemoteResource remote, TeamSubscriber subscriber, IProgressMonitor monitor) throws TeamException {
		super(local, base, remote, subscriber, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.SyncInfo#computeSyncKind(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected int calculateKind(IProgressMonitor progress) throws TeamException {
		// special handling for folders, the generic sync algorithm doesn't work well
		// with CVS because folders are not in namespaces (e.g. they exist in all versions
		// and branches).
		IResource local = getLocal();
		if(local.getType() != IResource.FILE && getSubscriber().isThreeWay()) {
			int folderKind = SyncInfo.IN_SYNC;
			ICVSRemoteFolder remote = (ICVSRemoteFolder)getRemote();
			ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)local);
			boolean isCVSFolder = false;
			try {
				isCVSFolder = cvsFolder.isCVSFolder();
			} catch (CVSException e) {
				// Assume the folder is not a CVS folder
			}
			if(!local.exists()) {
				if(remote != null) {
					if (isCVSFolder) {
						// TODO: This assumes all CVS folders are in-sync even if they have been pruned!
						folderKind = SyncInfo.IN_SYNC;
					} else {
						folderKind = SyncInfo.INCOMING | SyncInfo.ADDITION;
					}
				} else {
					// ignore conflicting deletion to keep phantom sync info
				}
			} else {
				if(remote == null) {
					if(isCVSFolder) {
						// TODO: This is not really an incoming deletion
						// The folder will be pruned once any children are commited
						folderKind = SyncInfo.IN_SYNC;
						//folderKind = SyncInfo.INCOMING | SyncInfo.DELETION;
					} else {
						folderKind = SyncInfo.OUTGOING | SyncInfo.ADDITION;
					}
				} else if(!isCVSFolder) {
					folderKind = SyncInfo.CONFLICTING | SyncInfo.ADDITION;
				} else {
					// folder exists both locally and remotely and are considered in sync, however 
					// we aren't checking the folder mappings to ensure that they are the same.
				}
			}
			return folderKind;
		}
	
		// 1. Run the generic sync calculation algorithm, then handle CVS specific
		// sync cases.
		int kind = super.calculateKind(progress);
	
		// 2. Set the CVS specific sync type based on the workspace sync state provided
		// by the CVS server.
		IRemoteResource remote = getRemote();
		if(remote!=null && (kind & SyncInfo.PSEUDO_CONFLICT) == 0) {
			RemoteResource cvsRemote = (RemoteResource)remote;
			int type = cvsRemote.getWorkspaceSyncState();
			switch(type) {
				// the server compared both text files and decided that it cannot merge
				// them without line conflicts.
				case Update.STATE_CONFLICT: 
					return kind | SyncInfo.MANUAL_CONFLICT;

				// the server compared both text files and decided that it can safely merge
				// them without line conflicts. 
				case Update.STATE_MERGEABLE_CONFLICT: 
					return kind | SyncInfo.AUTOMERGE_CONFLICT;				
			}			
		}
	
		// 3. unmanage delete/delete conflicts and return that they are in sync
		kind = handleDeletionConflicts(kind);
	
		return kind;
	}

	/**
	 * Return true if the provided phantom folder conyains any outgoing file deletions.
	 * We only need to detect if there are any files since a phantom folder can only
	 * contain outgoing filre deletions and other folder.
	 * 
	 * @param cvsFolder a phantom folder
	 * @return boolean
	 */
	private boolean containsOutgoingDeletions(ICVSFolder cvsFolder) {
		final boolean result[] = new boolean[] { false };
		try {
			cvsFolder.accept(new ICVSResourceVisitor() {
				public void visitFile(ICVSFile file) throws CVSException {
					// Do nothing. Files are handled below
				}
				public void visitFolder(ICVSFolder folder) throws CVSException {
					if (folder.members(ICVSFolder.FILE_MEMBERS).length > 0) {
						result[0] = true;
					} else {
						folder.acceptChildren(this);
					}
				}
			});
		} catch (CVSException e) {
			CVSProviderPlugin.log(e);
		}
		return result[0];
	}
	
	/*
	 * If the resource has a delete/delete conflict then ensure that the local is unmanaged so that the 
	 * sync info can be properly flushed.
	 */
	protected int handleDeletionConflicts(int kind) {
		if(kind == (SyncInfo.CONFLICTING | SyncInfo.DELETION | SyncInfo.PSEUDO_CONFLICT)) {
			try {				
				IResource local = getLocal();
				ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(local);
				if(!cvsResource.isFolder() && cvsResource.isManaged()) {
					cvsResource.unmanage(null);
				}
				return SyncInfo.IN_SYNC;
			} catch(CVSException e) {
				CVSProviderPlugin.log(e);
				return SyncInfo.CONFLICTING | SyncInfo.DELETION;
			}
		}
		return kind;
	}

	/*
	 * Update the sync info of the local resource in such a way that the local changes can be committed.
	 */
	public void makeOutgoing(IProgressMonitor monitor) throws TeamException {
		
		// TODO: What is the impact of using whatever the current granularity is?
		// int syncKind = getSyncKind(GRANULARITY_TIMESTAMP , monitor);
		int syncKind = getKind();
		boolean incoming = (syncKind & DIRECTION_MASK) == INCOMING;
		boolean outgoing = (syncKind & DIRECTION_MASK) == OUTGOING;

		ICVSResource local = CVSWorkspaceRoot.getCVSResourceFor(getLocal());
		RemoteResource remote = (RemoteResource)getRemote();
		ResourceSyncInfo origInfo = local.getSyncInfo();
		MutableResourceSyncInfo info = null;
		if(origInfo!=null) {
			info = origInfo.cloneMutable();			
		}
	
		if (outgoing) {
				// The sync info is alright, it's already outgoing!
				return;
		} else if (incoming) {
			// We have an incoming change, addition, or deletion that we want to ignore
			if (local.exists()) {
				// We could have an incoming change or deletion
				if (remote == null) {
					info.setAdded();
				} else {
					// Otherwise change the revision to the remote revision and dirty the file
					info.setRevision(remote.getSyncInfo().getRevision());
					info.setTimeStamp(null);
				}
			} else {
				// We have an incoming add, turn it around as an outgoing delete
				info = remote.getSyncInfo().cloneMutable();
				info.setDeleted(true);
			}
		} else if (local.exists()) {
			// We have a conflict and a local resource!
			if (getRemote() != null) {
				if (getBase() != null) {
					// We have a conflicting change, Update the local revision
					info.setRevision(remote.getSyncInfo().getRevision());
				} else {
					// We have conflictin additions.
					// We need to fetch the contents of the remote to get all the relevant information (timestamp, permissions)
					// TODO: Do we really need to fetch the contents here?
					remote.getContents(Policy.monitorFor(monitor));
					info = remote.getSyncInfo().cloneMutable();
				}
			} else if (getBase() != null) {
				// We have a remote deletion. Make the local an addition
				info.setAdded();
			} else {
				// There's a local, no base and no remote. We can't possible have a conflict!
				Assert.isTrue(false);
			} 
		} else {
			// We have a conflict and there is no local!
			if (getRemote() != null) {
				// We have a local deletion that conflicts with remote changes.
				info.setRevision(remote.getSyncInfo().getRevision());
				info.setDeleted(true);
			} else {
				// We have conflicting deletions. Clear the sync info
				info = null;
				return;
			}
		}
		if(info!=null) {
			info.setTag(local.getParent().getFolderSyncInfo().getTag());
		}
		((ICVSFile)local).setSyncInfo(info, ICVSFile.UNKNOWN);
	}
	
	/*
	 * Update the sync info of the local resource in such a way that the remote resource can be loaded 
	 * ignore any local changes. 
	 */
	public void makeIncoming(IProgressMonitor monitor) throws TeamException {
		// To make outgoing deletions incoming, the local will not exist but
		// it is still important to unmanage (e.g. delete all meta info) for the
		// deletion.
		CVSWorkspaceRoot.getCVSResourceFor(getLocal()).unmanage(null);
	}
	
	/*
	 * Load the resource and folder sync info into the local from the remote
	 * 
	 * This method can be used on incoming folder additions to set the folder sync info properly
	 * without hitting the server again. It also applies to conflicts that involves unmanaged
	 * local resources.
	 * 
	 * If the local folder is already managed and is a cvs folder, this operation
	 * will throw an exception if the mapping does not match that of the remote.
	 */
	 public void makeInSync() throws CVSException {
	 	
		// Only work on folders
		if (getLocal().getType() == IResource.FILE) return;
	 		
		boolean outgoing = (getKind() & DIRECTION_MASK) == OUTGOING;
		if (outgoing) return;
		
		ICVSFolder local = (ICVSFolder)CVSWorkspaceRoot.getCVSFolderFor((IContainer)getLocal());
		RemoteFolder remote = (RemoteFolder)getRemote();
		
		// The parent must be managed
		if (! local.getParent().isCVSFolder())
			return;
		
		// Ensure that the folder exists locally
		if (! local.exists()) {
			local.mkdir();
		}
		
		// If the folder already has CVS info, check that the remote and local match
		if(local.isManaged() && local.isCVSFolder()) {
			// Verify that the root and repository are the same
			FolderSyncInfo remoteInfo = remote.getFolderSyncInfo();
			FolderSyncInfo localInfo = local.getFolderSyncInfo();
			if ( ! localInfo.getRoot().equals(remoteInfo.getRoot())) {
				throw new CVSException(Policy.bind("CVSRemoteSyncElement.rootDiffers", new Object[] {local.getName(), remoteInfo.getRoot(), localInfo.getRoot()}));//$NON-NLS-1$
			} else if ( ! localInfo.getRepository().equals(remoteInfo.getRepository())) {
				throw new CVSException(Policy.bind("CVSRemoteSyncElement.repositoryDiffers", new Object[] {local.getName(), remoteInfo.getRepository(), localInfo.getRepository()}));//$NON-NLS-1$
			}
			// The folders are in sync so just return
			return;
		}
		
		// Since the parent is managed, this will also set the resource sync info. It is
		// impossible for an incoming folder addition to map to another location in the
		// repo, so we assume that using the parent's folder sync as a basis is safe.
		// It is also impossible for an incomming folder to be static.		
		FolderSyncInfo remoteInfo = remote.getFolderSyncInfo();
		FolderSyncInfo localInfo = local.getParent().getFolderSyncInfo();
		local.setFolderSyncInfo(new FolderSyncInfo(remoteInfo.getRepository(), remoteInfo.getRoot(), localInfo.getTag(), false));
	}
	
	public String toString() {
		IResource local = getLocal();
		IRemoteResource base = getBase();
		IRemoteResource remote = getRemote();
		StringBuffer result = new StringBuffer();
		result.append("Local: ");
		result.append(getLocal().toString());
		result.append(" Base: ");
		if (base == null) {
			result.append("none");
		} else {
			result.append(base.toString());
		}
		result.append(" Remote: ");
		if (remote == null) {
			result.append("none");
		} else {
			result.append(remote.toString());
		}
		return result.toString();
	}
}
