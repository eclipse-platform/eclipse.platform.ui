package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.sync.ILocalSyncElement;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.LocalSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

public class CVSLocalSyncElement extends LocalSyncElement {

	protected IRemoteResource base;
	protected IResource local;

	public CVSLocalSyncElement(IResource local, IRemoteResource base) {
		this.local = local;
		this.base = base;						
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
		ICVSResource cvsResource = getCVSResourceFor(getLocal());
		if(cvsResource == null) {
			return false;
		} else {
			// a folder is dirty if it is managed but is not a CVS folder. This can
			// easily happen if someone deletes a folder from the file system but
			// doesn't unmanage it.
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
		return getLocal() != null;
	}

	/*
	 * @see ILocalSyncElement#hasRemote()
	 */
	public boolean hasRemote() {
		return getLocal() != null;
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
	 	return getCVSResourceFor(getLocal());
	 }	 

	/*
	 * @see LocalSyncElement#isIgnored(IResource)
	 */
	protected boolean isIgnored(IResource child) {
		ICVSResource cvsResource = getCVSResourceFor(getLocal());
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
	
	private ICVSResource getCVSResourceFor(IResource resource) {
		if(resource.getType() != IResource.FILE) {
			return new EclipseFolder((IContainer)resource);
		} else {
			return new EclipseFile((IFile)resource);
		}
	}
}