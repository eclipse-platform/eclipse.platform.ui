package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.core.sync.RemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;

public class CVSRemoteSyncElement extends RemoteSyncElement {

	CVSLocalSyncElement localSync;
	IRemoteResource remote;
	boolean isThreeWay = true;

	public CVSRemoteSyncElement(boolean isThreeWay, IResource local, IRemoteResource base, IRemoteResource remote) {
		localSync = new CVSLocalSyncElement(local, base);
		this.remote = remote;	
		this.isThreeWay = isThreeWay;		
	}

	/*
	 * @see RemoteSyncElement#create(IResource, IRemoteResource, IRemoteResource)
	 */
	public IRemoteSyncElement create(boolean isThreeWay, IResource local, IRemoteResource base, IRemoteResource remote, Object data) {
		return new CVSRemoteSyncElement(isThreeWay, local, base, remote);
	}

	/*
	 * @see IRemoteSyncElement#getRemote()
	 */
	public IRemoteResource getRemote() {
		return remote;
	}

	/*
	 * @see IRemoteSyncElement#isOutOfDate()
	 */
	public boolean isOutOfDate() {
		IRemoteResource base = getBase();
		if(base!=null && remote!=null) {
			ICVSRemoteResource remoteCvs = (ICVSRemoteResource)remote;
			ICVSRemoteResource baseCvs = (ICVSRemoteResource)base;
			return ! remoteCvs.equals(baseCvs);
		} else if(base!=null && remote==null) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * @see LocalSyncElement#getData()
	 */
	protected Object getData() {
		return localSync.getData();
	}

	/*
	 * @see ILocalSyncElement#getLocal()
	 */
	public IResource getLocal() {
		return localSync.getLocal();
	}

	/*
	 * @see ILocalSyncElement#getBase()
	 */
	public IRemoteResource getBase() {
		return localSync.getBase();
	}

	/*
	 * @see ILocalSyncElement#isDirty()
	 */
	public boolean isDirty() {
		return localSync.isDirty();
	}

	/*
	 * @see ILocalSyncElement#isCheckedOut()
	 */
	public boolean isCheckedOut() {
		return localSync.isCheckedOut();
	}

	/*
	 * Local helper to indicate if the corresponding local resource has a base
	 * 
	 * XXX Should this be part of the interface?
	 */
	public boolean hasBase() {
		return getBase() != null;
	}
	
	/*
	 * @see ILocalSyncElement#hasRemote()
	 */
	public boolean hasRemote() {
		return remote != null;
	}
	
	/*
	 * @see LocalSyncElement#create(IResource, IRemoteResource, Object)
	 */
	public ILocalSyncElement create(IResource local, IRemoteResource base, Object data) {
		return localSync.create(local, base, data);
	}
	/*
	 * @see LocalSyncElement#isIgnored(IResource)
	 */
	protected boolean isIgnored(IResource resource) {
		return localSync.isIgnored(resource);
	}
	/*
	 * @see IRemoteSyncElement#ignoreBaseTree()
	 */
	public boolean isThreeWay() {
		return isThreeWay;
	}
	
	/*
	 * Update the sync info of the local resource in such a way that the local changes can be committed.
	 */
	public void makeOutgoing(IProgressMonitor monitor) throws TeamException {
		
		int syncKind = getSyncKind(GRANULARITY_TIMESTAMP, monitor);
		boolean incoming = (syncKind & DIRECTION_MASK) == INCOMING;
		boolean outgoing = (syncKind & DIRECTION_MASK) == OUTGOING;

		ICVSResource local = localSync.getCVSResource();
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
			if (hasRemote()) {
				if (hasBase()) {
					// We have a conflicting change, Update the local revision
					info.setRevision(remote.getSyncInfo().getRevision());
				} else {
					// We have conflictin additions.
					// We need to fetch the contents of the remote to get all the relevant information (timestamp, permissions)
					remote.getContents(Policy.monitorFor(monitor));
					info = remote.getSyncInfo().cloneMutable();
				}
			} else if (hasBase()) {
				// We have a remote deletion. Make the local an addition
				info.setAdded();
			} else {
				// There's a local, no base and no remote. We can't possible have a conflict!
				Assert.isTrue(false);
			} 
		} else {
			// We have a conflict and there is no local!
			if (hasRemote()) {
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
		local.setSyncInfo(info);
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
	 public void makeInSync(IProgressMonitor monitor) throws TeamException {
	 	
	 	// Only work on folders
	 	if (! isContainer()) return;
	 		
	 	int syncKind = getSyncKind(GRANULARITY_TIMESTAMP, monitor);
		boolean outgoing = (syncKind & DIRECTION_MASK) == OUTGOING;
		if (outgoing) return;
		
		ICVSFolder local = (ICVSFolder)localSync.getCVSResource();
		RemoteFolder remote = (RemoteFolder)getRemote();
		
		// The parent must be managed
		if (! local.getParent().isCVSFolder())
			return;
			
		if (! local.exists()) {
			local.mkdir();
		} else {
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
		}
		
		// Since the parent is managed, this will also set the resource sync info. It is
		// impossible for an incoming folder addition to map to another location in the
		// repo, so we assume that using the parent's folder sync as a basis is safe.
		// It is also impossible for an incomming folder to be static.		
		FolderSyncInfo remoteInfo = remote.getFolderSyncInfo();
		FolderSyncInfo localInfo = local.getParent().getFolderSyncInfo();
		local.setFolderSyncInfo(new FolderSyncInfo(remoteInfo.getRepository(), remoteInfo.getRoot(), localInfo.getTag(), false));
	 }	 	
	/*
	 * @see ILocalSyncElement#getSyncKind(int, IProgressMonitor)
	 */
	public int getSyncKind(int granularity, IProgressMonitor progress) {
		
		// special handling for folders, the generic sync algorithm doesn't work well
		// with CVS because folders are not in namespaces (e.g. they exist in all versions
		// and branches).
		if(isContainer()) {
			int folderKind = IRemoteSyncElement.IN_SYNC;
			IResource local = getLocal();
			ICVSRemoteFolder remote = (ICVSRemoteFolder)getRemote();
			ICVSFolder cvsFolder = (ICVSFolder)localSync.getCVSResource();
			if(!local.exists()) {
				if(remote != null) {
					folderKind = IRemoteSyncElement.INCOMING | IRemoteSyncElement.ADDITION;
				} else {
					// conflicting deletion ignore
				}
			} else {
				if(remote == null) { 
					if(cvsFolder.isCVSFolder()) {
						folderKind = IRemoteSyncElement.INCOMING | IRemoteSyncElement.DELETION;
					} else {
						folderKind = IRemoteSyncElement.OUTGOING | IRemoteSyncElement.ADDITION;
					}
				} else if(!cvsFolder.isCVSFolder()) {
					folderKind = IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.ADDITION;
				} else {
					// folder exists both locally and remotely and are considered in sync, however 
					// we aren't checking the folder mappings to ensure that they are the same.
				}
			}
			return folderKind;
		}
		
		// 1. Run the generic sync calculation algorithm, then handle CVS specific
		// sync cases.
		int kind = super.getSyncKind(granularity, progress);
		
		// 2. Set the CVS specific sync type based on the workspace sync state provided
		// by the CVS server.
		if(remote!=null && (kind & IRemoteSyncElement.PSEUDO_CONFLICT) == 0) {
			int type = ((RemoteResource)remote).getWorkspaceSyncState();
			switch(type) {
				// the server compared both text files and decided that it cannot merge
				// them without line conflicts.
				case Update.STATE_CONFLICT: 
					return kind | ILocalSyncElement.MANUAL_CONFLICT;

				// the server compared both text files and decided that it can safely merge
				// them without line conflicts. 
				case Update.STATE_MERGEABLE_CONFLICT: 
					return kind | ILocalSyncElement.AUTOMERGE_CONFLICT;				
			}			
		}
		
		// 3. unmanage delete/delete conflicts and return that they are in sync
		kind = handleDeletionConflicts(kind);
		
		return kind;
	}
	
	/*
	 * If the resource has a delete/delete conflict then ensure that the local is unmanaged so that the 
	 * sync info can be properly flushed.
	 */
	private int handleDeletionConflicts(int kind) {
		if(kind == (IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.DELETION | IRemoteSyncElement.PSEUDO_CONFLICT)) {
			try {
				ICVSResource cvsResource = localSync.getCVSResource();
				if(!isContainer() && cvsResource.isManaged()) {
					cvsResource.unmanage(null);
				}
				return IRemoteSyncElement.IN_SYNC;
			} catch(CVSException e) {
				CVSProviderPlugin.log(e.getStatus());
				return IRemoteSyncElement.CONFLICTING | IRemoteSyncElement.DELETION;
			}
		}
		return kind;
	}
}