package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.LocalSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProvider;

public class CVSLocalSyncElement extends LocalSyncElement {

	protected ICVSResource cvsResource;
	protected IRemoteResource base;
	protected IResource local;

	public CVSLocalSyncElement(IResource local, IRemoteResource base) {

		this.local = local;
		this.base = base;
		
		File file = new File(local.getLocation().toOSString());
		if(local.getType() != IResource.FILE) {
			this.cvsResource = new LocalFolder(file);
		} else {
			this.cvsResource = new LocalFile(file);
		}
	}

	/*
	 * @see RemoteSyncElement#create(IResource, IRemoteResource, IRemoteResource)
	 */
	public ILocalSyncElement create(IResource local, IRemoteResource base, Object data) {
		return new CVSLocalSyncElement(local, base);
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
					ResourceSyncInfo info = cvsResource.getSyncInfo();
					if(info==null) {
						return false;
					}
					if(base!=null) {
						boolean sameRevisions = ((RemoteFile)base).getRevision().equals(info.getRevision());
						if(!sameRevisions) {
							return true;
						}
					}
					return ((ICVSFile)cvsResource).isDirty();
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
		return null;
	}
	
	/*
	 * Answers the CVS resource for this sync element
	 */
	 public ICVSResource getCVSResource() {
	 	return cvsResource;
	 }	 
	/*
	 * @see LocalSyncElement#isIgnored(IResource)
	 */
	protected boolean isIgnored(IResource child) {
		if(cvsResource==null || !cvsResource.isFolder() ) {
			return false;
		} else {
			try {
				ICVSResource managedChild = ((ICVSFolder)cvsResource).getChild(child.getName());
				return managedChild.isIgnored();
			} catch(CVSException e) {
				return false;		
			}
		}
	}
}