package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.core.sync.LocalSyncElement;
import org.eclipse.team.core.sync.RemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.util.Assert;

public class CVSRemoteSyncElement extends RemoteSyncElement {

	CVSLocalSyncElement localSync;
	IRemoteResource remote;

	public CVSRemoteSyncElement(IResource local, IRemoteResource base, IRemoteResource remote, IManagedFolder parent) {
		localSync = new CVSLocalSyncElement(local, base, parent);
		this.remote = remote;				
	}

	/*
	 * @see RemoteSyncElement#create(IResource, IRemoteResource, IRemoteResource)
	 */
	public IRemoteSyncElement create(IResource local, IRemoteResource base, IRemoteResource remote, Object data) {
		return new CVSRemoteSyncElement(local, base, remote, (IManagedFolder)data);
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

		IManagedResource cvsResource = localSync.getCVSResource();
		if(cvsResource != null && !cvsResource.isFolder()) {
			try {
				FileProperties info = ((IManagedFile)cvsResource).getFileInfo();
				if(info != null) {
					hasBase = true;
				}
			} catch(CVSException e) {
				return true;
			}
		}
		boolean hasRemote = remote != null;
		
		if(hasBase && hasRemote) {
			IManagedFile file = (IManagedFile)localSync.getCVSResource();
			try {
				// at this point remote and file can't be null
				Assert.isNotNull(remote);
				Assert.isNotNull(file);
				return ! ((ICVSRemoteFile)remote).getRevision().equalsIgnoreCase(file.getFileInfo().getVersion());
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
}