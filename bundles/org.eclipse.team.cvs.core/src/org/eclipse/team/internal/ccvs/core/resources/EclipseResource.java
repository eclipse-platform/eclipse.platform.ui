package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.Team;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.FileNameMatcher;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Represents handles to CVS resource on the local file system. Synchronization
 * information is taken from the CVS subdirectories. 
 * 
 * @see LocalFolder
 * @see LocalFile
 */
abstract class EclipseResource implements ICVSResource, Comparable {

	 // The separator that must be used when creating CVS resource paths. Never use
	 // the platform default separator since it is not compatible with CVS resources.
	protected static final String SEPARATOR = Session.SERVER_SEPARATOR;
	protected static final String CURRENT_LOCAL_FOLDER = Session.CURRENT_LOCAL_FOLDER;
		
	/*
	 * The local resource represented by this handle
	 */
	IResource resource;
	
	/*
	 * Creates a CVS handle to the provided resource
	 */
	protected EclipseResource(IResource resource) {
		Assert.isNotNull(resource);
		this.resource = resource;
	}
	
	/*
	 * Get the extention of the path of resource relative to the path of root
	 * 
	 * @throws CVSException if root is not a root-folder of resource
	 */
	public String getRelativePath(ICVSFolder root) throws CVSException {
		try {
			EclipseResource rootFolder;
			String result;
			rootFolder = (EclipseResource)root;
			result = Util.getRelativePath(rootFolder.getPath(), getPath());
			if (result.length() == 0) return CURRENT_LOCAL_FOLDER;
			return result;	
		} catch (ClassCastException e) {
			throw new CVSException(Policy.bind("EclipseResource.invalidResourceClass"), e); //$NON-NLS-1$
		}
	}

	/*
	 * @see ICVSResource#delete()
	 */
	public void delete() throws CVSException {
		try {
			resource.delete(false /*force*/, null);
		} catch(CoreException e) {
			throw new CVSException(e.getStatus());
		}
	}

	/*
	 * @see ICVSResource#exists()
	 */
	public boolean exists() {
		return resource.exists();
	}

	/*
	 * Returns the parent folder of this resource of <code>null</code> if resource
	 * the resource.
	 * 
	 * @see ICVSResource#getParent()
	 */
	public ICVSFolder getParent() {
		IContainer parent = resource.getParent();
		if (parent==null) {
			return null;
		}
		return new EclipseFolder(parent);
	}

	/*
	 * @see ICVSResource#getName()
	 */
	public String getName() {
		return resource.getName();
	}

	/*
	 * @see ICVSResource#isIgnored()
	 */
	public boolean isIgnored() throws CVSException {
		// a managed resource is never ignored
		if(isManaged() || resource.getType()==IResource.ROOT || resource.getType()==IResource.PROJECT) {
			return false;
		}
		
		// If the resource is a derived or linked resource, it is ignored
		if (resource.isDerived() || resource.isLinked()) {
			return true;
		}
		
		// always ignore CVS
		String name = getName();
		if (name.equals("CVS")) return true; //$NON-NLS-1$
		
		// check the global ignores from Team
		if (Team.isIgnoredHint(resource)) return true;
		
		// check ignore patterns from the .cvsignore file.
		if(EclipseSynchronizer.getInstance().isIgnored(resource)) {
			return true;
		}
		
		// check the parent, if the parent is ignored or mapped to CVSROOT/Emptydir
		// then this resource is ignored also
		ICVSFolder parent = getParent();
		if(parent==null) return false;
		if (parent.isIgnored()) return true;
		FolderSyncInfo info = parent.getFolderSyncInfo();
		if (info == null) return false;
		return info.getRepository().equals(FolderSyncInfo.VIRTUAL_DIRECTORY);
	}

	/*
	 * @see ICVSResource#setIgnored()
	 */
	public void setIgnored() throws CVSException {
		EclipseSynchronizer.getInstance().addIgnored(resource.getParent(), resource.getName());
	}
	
	/*
	 * @see ICVSResource#setIgnoredAs(String)
	 */
	public void setIgnoredAs(final String pattern) throws CVSException {
		run(new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				EclipseSynchronizer.getInstance().addIgnored(resource.getParent(), pattern);
			}
		}, null);
	}

	/*
	 * @see ICVSResource#isManaged()
	 */
	public boolean isManaged() throws CVSException {
		return isManaged(getSyncBytes());
	}
	
	/*
	 * Helper method that captures the sematics of isManaged given a ResourceSyncInfo
	 */
	public boolean isManaged(byte[] syncBytes) {
		return syncBytes != null;
	}
	
	/**
	 * Two ManagedResources are equal, if there cvsResources are
	 * equal (and that is, if the point to the same file)
	 */
	public boolean equals(Object obj) {
		
		if (!(obj instanceof EclipseResource)) {
			return false;
		} else {
			return getPath().equals(((EclipseResource) obj).getPath());
		}
	}
			
	/*
	 * @see ICVSResource#getPath()
	 */
	public String getPath() {
		return resource.getFullPath().toString();
	}	
	
	/*
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}
	
	/*
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#getSyncBytes()
	 */
	public byte[] getSyncBytes() throws CVSException {
		return EclipseSynchronizer.getInstance().getSyncBytes(getIResource());
	}
	
	/*
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFile#setSyncBytes(byte[])
	 */
	public void setSyncBytes(byte[] syncBytes) throws CVSException {
		if (getParent().isCVSFolder()) {
			EclipseSynchronizer.getInstance().setSyncBytes(getIResource(), syncBytes);
		}
	}
	
	/*
	 * @see ICVSResource#getSyncInfo()
	 */
	public ResourceSyncInfo getSyncInfo() throws CVSException {
		return EclipseSynchronizer.getInstance().getResourceSync(resource);
	}
	
	/*
	 * Implement the hashcode on the underlying strings, like it is done in the equals.
	 */
	public int hashCode() {
		return getPath().hashCode();
	}	
	
	/*
	 * Give the pathname back
	 */
	public String toString() {
		return getPath();
	}
	
	/*
	 * @see ICVSResource#unmanage()
	 */
	public void unmanage(IProgressMonitor monitor) throws CVSException {
		EclipseSynchronizer.getInstance().deleteResourceSync(resource);
	}
	
	/*
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object arg0) {
		EclipseResource other = (EclipseResource)arg0;
		return resource.getFullPath().toString().compareTo(other.resource.getFullPath().toString());
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSResource#getIResource()
	 */
	public IResource getIResource() {
		return resource;
	}

	/**
	 * Called by a resource change listener when a resource is changed or added. This allows
	 * CVS resources to adjust any internal state based on the change.
	 * 
	 * @param forAddition modification is an addition
	 * @throws CVSException
	 */
	public abstract void handleModification(boolean forAddition) throws CVSException;
	
	/*
	 * Run method which obtains both the CVS synchronizer lock and the workspace
	 * lock
	 */
	protected abstract void run(final ICVSRunnable job, IProgressMonitor monitor) throws CVSException;

	/**
	 * Sets the modified status of the receiver. This is done to ensure that any
	 * cached state kept by it or its parents is updated properly. The invoked method
	 * (setDirtyIndicator) will adjust the parent dirty state if the modification
	 * state of the resource has changed. 
	 */
	protected void setModified(boolean modified) throws CVSException {
		EclipseSynchronizer.getInstance().setDirtyIndicator(getIResource(), modified);
	}
}