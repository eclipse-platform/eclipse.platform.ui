package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
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
		try {
			if(base == null && cvsResource.isManaged()) {
				if(cvsResource.isFolder()) {
					FolderSyncInfo syncInfo = ((ICVSFolder)cvsResource).getFolderSyncInfo();
					base = new RemoteFolder(null, CVSProvider.getInstance().getRepository(syncInfo.getRoot()), new Path(syncInfo.getRepository()), syncInfo.getTag());		
				} else {
					ResourceSyncInfo info = cvsResource.getSyncInfo();
					if(!info.isDeleted() || !info.isAdded()) {
						ICVSFolder parentFolder = cvsResource.getParent();
						FolderSyncInfo syncInfo = parentFolder.getFolderSyncInfo();
						RemoteFolder parent =  new RemoteFolder(null, CVSProvider.getInstance().getRepository(syncInfo.getRoot()), new Path(syncInfo.getRepository()), syncInfo.getTag());
						base = RemoteFile.getBase(parent, (ICVSFile)cvsResource);
					}
				}
			}
		} catch(CVSException e) {
			TeamPlugin.log(IStatus.ERROR, "CVS error creating the base resource", e);
			return null;
		}
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
					if(cvsResource.getSyncInfo()==null) {
						return false;
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