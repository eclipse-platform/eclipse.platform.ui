package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.core.sync.RemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
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
				
		// XXX gender changes?
		if(isContainer()) {
			return false;
		}
				
		boolean hasBase = false;

		ICVSResource cvsResource = localSync.getCVSResource();
		if(cvsResource != null && !cvsResource.isFolder()) {
			try {
				ResourceSyncInfo info = cvsResource.getSyncInfo();
				if ((info != null) && !info.isAdded()) {
					hasBase = true;
				}
			} catch(CVSException e) {
				return true;
			}
		}
		boolean hasRemote = remote != null;
		
		if(hasBase && hasRemote) {
			ICVSFile file = (ICVSFile)localSync.getCVSResource();
			try {
				// at this point remote and file can't be null
				Assert.isNotNull(remote);
				Assert.isNotNull(file);
				ResourceSyncInfo info = file.getSyncInfo();
				return ! ((ICVSRemoteFile)remote).getRevision().equalsIgnoreCase(info.getRevision());
			} catch(CVSException e) {
				return true;
			} catch(TeamException e) {
				return true;
			}
		} else if(hasBase && !hasRemote) {
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
	 * Update the sync info of the local resource in response to the remote changes being merged with the local.
	 * 
	 * The purpose of this method is to update the local sync info so the local resource can be committed.
	 * However, it also clears the sync info for conflicting deletions.
	 * 
	 * It is only valid to invoke this message on sync elements that have conflicts.
	 * We shold never have conflicts on folders.
	 */
	public void merged(IProgressMonitor monitor) throws TeamException {
		
		// XXX should we add asserts for conflict and !folder?
		
		boolean syncChanged = false;
		try {
			ICVSResource local = localSync.getCVSResource();
			
			// If both the local and remote exists, we need to merge the remote sync info into the local
			if (local.exists()) {
				ResourceSyncInfo info;
				String revision;
				if (hasRemote()) {
					if (hasBase()) {
						info = local.getSyncInfo();
						revision = ((RemoteResource)getRemote()).getSyncInfo().getRevision();
					} else {
						// We need to fetch the contents of the remote to get all the relevant information (timestamp, permissions)
						getRemote().getContents(Policy.monitorFor(monitor));
						info = ((RemoteResource)getRemote()).getSyncInfo();
						revision = info.getRevision();
					}
				} else if (hasBase()) {
					info = local.getSyncInfo();
					revision = ResourceSyncInfo.ADDED_REVISION;
				} else {
					// There's a local, no base and no remote. This is invalid
					throw new CVSException(Policy.bind("CVSRemoteSyncElement.invalidMergedRequest"));
				} 
				info = new ResourceSyncInfo(info.getName(), revision, info.getTimeStamp(), info.getKeywordMode(), local.getParent().getFolderSyncInfo().getTag(), info.getPermissions());
				local.setSyncInfo(info);
				syncChanged = true;
			} else {
				// There is no local
				if (hasRemote()) {
					// XXX Do we simply make sure that the local sync has an outgoing deletions
				} else {
					local.setSyncInfo(null);
					syncChanged = true;
				}
			}
		} finally {
			if (syncChanged)
				Synchronizer.getInstance().save(Policy.monitorFor(monitor));
		}
	}
}