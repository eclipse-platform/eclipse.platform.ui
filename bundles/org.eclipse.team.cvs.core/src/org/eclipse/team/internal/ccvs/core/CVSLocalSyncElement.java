package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.LocalSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;

public class CVSLocalSyncElement extends LocalSyncElement {

	protected IManagedFolder folder;
	protected IManagedResource cvsResource;

	protected IRemoteResource base;
	protected IResource local;

	public CVSLocalSyncElement(IResource local, IRemoteResource base, IManagedFolder parent) {
		this.folder = parent;
		
		this.local = local;
		this.base = base;
		
		try {
			this.cvsResource = folder.getChild(local.getFullPath().removeFirstSegments(1).toString());
		} catch(CVSException e) {
			this.cvsResource = null;
		}
	}

	/*
	 * @see RemoteSyncElement#create(IResource, IRemoteResource, IRemoteResource)
	 */
	public ILocalSyncElement create(IResource local, IRemoteResource base, Object data) {
		return new CVSLocalSyncElement(local, base, (IManagedFolder)data);
	}

	/*
	 * @see ILocalSyncElement#getLocal()
	 */
	public IResource getLocal() {
		return local;
	}

	/*
	 * @see ILocalSyncElement#getBase()
	 */
	public IRemoteResource getBase() {
		return base;
	}

	/*
	 * @see ILocalSyncElement#isDirty()
	 */
	public boolean isDirty() {
		if(cvsResource == null) {
			return false;
		} else {
			if(cvsResource.isFolder()) {
				return false;
			} else {
				try {
					return ((IManagedFile)cvsResource).isDirty();
				} catch(CVSException e) {
					return true;
				}
			}
		}
	}

	/*
	 * @see ILocalSyncElement#isCheckedOut()
	 */
	public boolean isCheckedOut() {
		return cvsResource != null;
	}

	/*
	 * @see ILocalSyncElement#hasRemote()
	 */
	public boolean hasRemote() {
		return cvsResource != null;
	}
	
	/*
	 * @see RemoteSyncElement#getData()
	 */
	protected Object getData() {
		return folder;
	}
	
	/*
	 * Answers the CVS resource for this sync element
	 */
	 public IManagedResource getCVSResource() {
	 	return cvsResource;
	 }	 
}