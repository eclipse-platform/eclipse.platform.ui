package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.core.sync.RemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.syncinfo.*;
import org.eclipse.team.internal.ccvs.core.util.Assert;

public class CVSRemoteSyncElement extends RemoteSyncElement {

	CVSLocalSyncElement localSync;
	IRemoteResource remote;
	boolean ignoreBaseTree = true;

	public CVSRemoteSyncElement(boolean ignoreBaseTree, IResource local, IRemoteResource base, IRemoteResource remote) {
		localSync = new CVSLocalSyncElement(local, base);
		this.remote = remote;	
		this.ignoreBaseTree = ignoreBaseTree;			
	}

	/*
	 * @see RemoteSyncElement#create(IResource, IRemoteResource, IRemoteResource)
	 */
	public IRemoteSyncElement create(boolean ignoreBaseTree, IResource local, IRemoteResource base, IRemoteResource remote, Object data) {
		return new CVSRemoteSyncElement(ignoreBaseTree, local, base, remote);
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
	public boolean ignoreBaseTree() {
		return ignoreBaseTree;
	}
	
	/*
	 * Update the sync info of the local resource in such a way that the local changes can be committed.
	 */
	public void makeOutgoing(IProgressMonitor monitor) throws TeamException {
		
		int syncKind = getSyncKind(GRANULARITY_TIMESTAMP, monitor);
		boolean conflict = (syncKind & DIRECTION_MASK) == CONFLICTING;
		boolean incoming = (syncKind & DIRECTION_MASK) == INCOMING;
		boolean outgoing = (syncKind & DIRECTION_MASK) == OUTGOING;

		ICVSResource local = localSync.getCVSResource();
		RemoteResource remote = (RemoteResource)getRemote();
		ResourceSyncInfo info = local.getSyncInfo();
		String revision = null;
		
		if (outgoing) {
			// We have an outgoing change that's not a conflict.
			// Make sure the entry is right for additions and deletions
			if (remote == null) {
				// We have an add. Make sure there is an entry for the add
				if (info != null) {
					// The sync info is alright
					return;
				}
				Assert.isTrue(local.exists());
				// XXX We need to create the proper sync info
				info =  new ResourceSyncInfo(local.getName(), ResourceSyncInfo.ADDED_REVISION, "dummy timestamp", CVSProvider.isText(local.getName())?"":"-kb", local.getParent().getFolderSyncInfo().getTag(), null);
				revision = info.getRevision();
			} else {
				Assert.isNotNull(info);
				if (! local.exists() && ! info.isDeleted()) {
					// We have a delete. Update the entry if required
					revision = ResourceSyncInfo.DELETED_PREFIX + info.getRevision();
				} else {
					// The sync info is alright
					return;
				}
			}
		} else if (incoming) {
			// We have an incoming change, addition, or deletion that we want to ignore
			if (local.exists()) {
				// We could have an incoming change or deletion
				if (remote == null) {
					info =  new ResourceSyncInfo(local.getName(), ResourceSyncInfo.ADDED_REVISION, "dummy timestamp", CVSProvider.isText(local.getName())?"":"-kb", local.getParent().getFolderSyncInfo().getTag(), null);
					revision = info.getRevision();
				} else {
					info = remote.getSyncInfo();
					// Otherwise change the revision to the remote revision
					revision = info.getRevision();
					// Use the local sync info for the other info
					info = local.getSyncInfo();
				}
			} else {
				// We have an incoming add, turn it around as an outgoing delete
				info = remote.getSyncInfo();
				revision = ResourceSyncInfo.DELETED_PREFIX + info.getRevision();
			}
		} else if (local.exists()) {
			// We have a conflict and a local resource!
			if (hasRemote()) {
				if (hasBase()) {
					// We have a conflicting change, Update the local revision
					revision = remote.getSyncInfo().getRevision();
				} else {
					// We have conflictin additions.
					// We need to fetch the contents of the remote to get all the relevant information (timestamp, permissions)
					remote.getContents(Policy.monitorFor(monitor));
					info = remote.getSyncInfo();
					revision = info.getRevision();
				}
			} else if (hasBase()) {
				// We have a remote deletion. Make the local an addition
				revision = ResourceSyncInfo.ADDED_REVISION;
			} else {
				// There's a local, no base and no remote. We can't possible have a conflict!
				Assert.isTrue(false);
			} 
		} else {
			// We have a conflict and there is no local!
			if (hasRemote()) {
				// We have a local deletion that conflicts with remote changes.
				revision = ResourceSyncInfo.DELETED_PREFIX + remote.getSyncInfo().getRevision();
			} else {
				// We have conflicting deletions. Clear the sync info
				local.setSyncInfo(null);
				CVSProviderPlugin.getSynchronizer().save(((LocalResource)local).getLocalFile(), Policy.monitorFor(monitor));
				return;
			}
		}
		info = new ResourceSyncInfo(info.getName(), revision, info.getTimeStamp(), info.getKeywordMode(), local.getParent().getFolderSyncInfo().getTag(), info.getPermissions());
		local.setSyncInfo(info);
		CVSProviderPlugin.getSynchronizer().save(((LocalResource)local).getLocalFile(), Policy.monitorFor(monitor));
	}
	
	/*
	 * Update the sync info of the local resource in such a way that the remote resource can be loaded ignore any local changes.
	 * 
	 * XXX This is a quick fix to allow conflicts to be loaded and has not been tested for non-conflict cases
	 */
	public void makeIncoming(IProgressMonitor monitor) throws TeamException {
		
		int syncKind = getSyncKind(GRANULARITY_TIMESTAMP, monitor);
		boolean conflict = (syncKind & DIRECTION_MASK) == CONFLICTING;
		boolean incoming = (syncKind & DIRECTION_MASK) == INCOMING;
		boolean outgoing = (syncKind & DIRECTION_MASK) == OUTGOING;
		
		if (incoming) {
			// No need to do anything
		} else if (outgoing) {
			// For now, just unmanage the local resource so the remote change can be loaded with an update
			Session.getManagedResource(getLocal()).unmanage();
		} else {
			// For now, just unmanage the local resource so the remote change can be loaded with an update
			Session.getManagedResource(getLocal()).unmanage();
		}
	}
	
	/*
	 * Load the resource and folder sync info into the local from the remote
	 * 
	 * This method can be used on incoming folder additions to set the folder sync info properly
	 * without hitting the server again. It also applies to conflicts that involves unmanaged
	 * local resources.
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
			// must not be a managed folder.
			if(local.isManaged() || local.isCVSFolder()) {
				throw new CVSException(IStatus.ERROR, 0, "Error making a remote folder in sync with the server. The local folder is already managed.");
			}
			// can only makesync on non-project folders, on top level folders use checkout instead.
			if(local.getParent() == null || !local.getParent().isCVSFolder()) {
				throw new CVSException(IStatus.ERROR, 0, "Error making a remote folder in sync with the server. The local folder's parent is not a cvs folder.");
			}
		}
		
		// Since the parent is managed, this will also set the resource sync info. It is
		// impossible for an incoming folder addition to map to another location in the
		// repo, so we assume that using the parent's folder sync as a basis is safe.		
		FolderSyncInfo remoteInfo = remote.getFolderSyncInfo();
		FolderSyncInfo localInfo = local.getParent().getFolderSyncInfo();
		local.setFolderSyncInfo(new FolderSyncInfo(remoteInfo.getRepository(), remoteInfo.getRoot(), localInfo.getTag(), localInfo.getIsStatic()));
		CVSProviderPlugin.getSynchronizer().save(((LocalResource)local).getLocalFile(), Policy.monitorFor(monitor));
	 }	 	
}