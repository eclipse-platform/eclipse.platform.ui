/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.PersistantResourceVariantByteStore;
import org.eclipse.team.core.variants.ResourceVariantByteStore;
import org.eclipse.team.core.variants.ResourceVariantTree;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;

/**
 * CVS Specific refresh operation
 */
public class CVSResourceVariantTree extends ResourceVariantTree {

	private CVSTag tag;
	private boolean cacheFileContentsHint;
	private CVSSyncTreeSubscriber subscriber;

	public CVSResourceVariantTree(ResourceVariantByteStore cache, CVSTag tag, boolean cacheFileContentsHint) {
		super(cache);
		this.tag = tag;
		this.cacheFileContentsHint = cacheFileContentsHint;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.RefreshOperation#getSynchronizationCache()
	 */
	public ResourceVariantByteStore getByteStore() {
		return super.getByteStore();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.RefreshOperation#getRemoteSyncBytes(org.eclipse.core.resources.IResource, org.eclipse.team.core.subscribers.ISubscriberResource)
	 */
	protected byte[] getBytes(IResource local, IResourceVariant remote) throws TeamException {
		if (remote != null) {
			return super.getBytes(local, remote);
		} else {
			if (local.getType() == IResource.FOLDER) {
				// If there is no remote, use the local sync for the folder
				return getBaseBytes((IContainer)local, getTag(local));
			}
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.RefreshOperation#getRemoteChildren(org.eclipse.team.core.subscribers.ISubscriberResource, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IResourceVariant[] fetchMembers(IResourceVariant remote, IProgressMonitor progress) throws TeamException {
		ICVSRemoteResource[] children = remote != null ? (ICVSRemoteResource[])((RemoteResource)remote).members(progress) : new ICVSRemoteResource[0];
		IResourceVariant[] result = new IResourceVariant[children.length];
		for (int i = 0; i < children.length; i++) {
			result[i] = (IResourceVariant)children[i];
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.RefreshOperation#buildRemoteTree(org.eclipse.core.resources.IResource, int, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IResourceVariant fetchVariant(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
		// TODO: we are currently ignoring the depth parameter because the build remote tree is
		// by default deep!
		return (IResourceVariant)CVSWorkspaceRoot.getRemoteTree(resource, getTag(resource), cacheFileContentsHint, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTreeRefreshOperation#collectChanges(org.eclipse.core.resources.IResource, org.eclipse.team.core.synchronize.IResourceVariant, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IResource[] collectChanges(IResource local,
			IResourceVariant remote, int depth, IProgressMonitor monitor)
			throws TeamException {
		return super.collectChanges(local, remote, depth, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.IResourceVariantTree#roots()
	 */
	public IResource[] roots() {
		return subscriber.roots();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.IResourceVariantTree#getResourceVariant(org.eclipse.core.resources.IResource)
	 */
	public IResourceVariant getResourceVariant(IResource resource) throws TeamException {
		byte[] remoteBytes = getByteStore().getBytes(resource);
		if (remoteBytes == null) {
			// There is no remote handle for this resource
			return null;
		} else {
			if (resource.getType() == IResource.FILE) {
				byte[] parentBytes = getParentBytes(resource);
				if (parentBytes == null) {
					IProject project = resource.getProject();
					if (project.exists() && RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId()) != null) {
						CVSProviderPlugin.log(new CVSException( 
								Policy.bind("ResourceSynchronizer.missingParentBytesOnGet", getSyncName(getByteStore()).toString(), resource.getFullPath().toString()))); //$NON-NLS-1$
						// Assume there is no remote and the problem is a programming error
					}
					return null;
				}
				return RemoteFile.fromBytes(resource, remoteBytes, parentBytes);
			} else {
				return RemoteFolder.fromBytes(resource, remoteBytes);
			}
		}
	}

	private String getSyncName(ResourceVariantByteStore cache) {
		if (cache instanceof PersistantResourceVariantByteStore) {
			return ((PersistantResourceVariantByteStore)cache).getSyncName().toString();
		}
		return cache.getClass().getName();
	}
	
	
	private byte[] getParentBytes(IResource resource) throws TeamException {
		IContainer parent = resource.getParent();
		byte[] bytes =  getByteStore().getBytes(parent);
		if (bytes == null ) {
			bytes = getBaseBytes(parent, getTag(resource));
		}
		return bytes;
	}

	private byte[] getBaseBytes(IContainer parent, CVSTag tag) throws CVSException {
		byte[] bytes;
		// Look locally for the folder bytes
		ICVSFolder local = CVSWorkspaceRoot.getCVSFolderFor(parent);
		FolderSyncInfo info = local.getFolderSyncInfo();
		if (info == null) {
			bytes = null;
		} else {
			// Use the folder sync from the workspace and the tag from the store
			FolderSyncInfo newInfo = new FolderSyncInfo(info.getRepository(), info.getRoot(), tag, false);
			bytes = newInfo.getBytes();
		}
		return bytes;
	}
	
	private boolean hasLocalSyncInfo(IContainer folder) {
		ICVSFolder local = CVSWorkspaceRoot.getCVSFolderFor(folder);
		try {
			return local.getFolderSyncInfo() != null;
		} catch (CVSException e) {
			// Say that there is sync info and let the failure occur elsewhere
			return true;
		}
	}

	public CVSTag getTag(IResource resource) {
		return tag;
	}

	/**
	 * Dispose of the underlying byte store
	 */
	public void dispose() {
		getByteStore().dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree#setVariant(org.eclipse.core.resources.IResource, org.eclipse.team.core.synchronize.IResourceVariant)
	 */
	protected boolean setVariant(IResource local, IResourceVariant remote) throws TeamException {
		if (local.getType() == IResource.FOLDER && remote != null 
				&& !hasLocalSyncInfo((IFolder)local)
				&& hasLocalSyncInfo(local.getParent())) {
			// Manage the folder locally since folders exist in all versions, etc
			// Use the info from the remote except get the tag from the locla parent
			CVSTag tag = CVSWorkspaceRoot.getCVSFolderFor(local.getParent()).getFolderSyncInfo().getTag();
			FolderSyncInfo info = FolderSyncInfo.getFolderSyncInfo(remote.asBytes());
			FolderSyncInfo newInfo = new FolderSyncInfo(info.getRepository(), info.getRoot(), tag, info.getIsStatic());
			ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor((IFolder)local);
			cvsFolder.setFolderSyncInfo(newInfo);
		}
		boolean changed = super.setVariant(local, remote);
		if (local.getType() == IResource.FILE && getByteStore().getBytes(local) != null && !parentHasSyncBytes(local)) {
			// Log a warning if there is no sync bytes available for the resource's
			// parent but there is valid sync bytes for the child
			CVSProviderPlugin.log(new TeamException(Policy.bind("ResourceSynchronizer.missingParentBytesOnSet", getSyncName(getByteStore()), local.getFullPath().toString()))); //$NON-NLS-1$
		}
		return changed;
	}
	
	private boolean parentHasSyncBytes(IResource resource) throws TeamException {
		if (resource.getType() == IResource.PROJECT) return true;
		return getParentBytes(resource) != null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.AbstractResourceVariantTree#collectedMembers(org.eclipse.core.resources.IResource, org.eclipse.core.resources.IResource[])
	 */
	protected IResource[] collectedMembers(IResource local, IResource[] members) throws TeamException {
		// Look for resources that have sync bytes but are not in the resources we care about
		IResource[] resources = getStoredMembers(local);
		List children = new ArrayList();
		List changedResources = new ArrayList();
		children.addAll(Arrays.asList(members));
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (!children.contains(resource)) {
				// These sync bytes are stale. Purge them
				flushVariants(resource, IResource.DEPTH_INFINITE);
				changedResources.add(resource);
			}
		}
		return (IResource[]) changedResources.toArray(new IResource[changedResources.size()]);
	}
	
	/**
	 * Return all the members of that have resource variant information associated with them,
	 * such as members that are explicitly flagged as not having a resource variant. This list
	 * is used by the collection algorithm to flush variants for which there is no local and
	 * no remote.
	 * @param local the locla resource
	 * @return the local children that have resource variant information cached
	 * @throws TeamException
	 */
	private IResource[] getStoredMembers(IResource local) throws TeamException {			
		try {
			if (local.getType() != IResource.FILE && (local.exists() || local.isPhantom())) {
				IResource[] allChildren = ((IContainer)local).members(true /* include phantoms */);
				List childrenWithSyncBytes = new ArrayList();
				for (int i = 0; i < allChildren.length; i++) {
					IResource resource = allChildren[i];
					if (getByteStore().getBytes(resource) != null) {
						childrenWithSyncBytes.add(resource);
					}
				}
				return (IResource[]) childrenWithSyncBytes.toArray(
						new IResource[childrenWithSyncBytes.size()]);
			}
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
		return new IResource[0];
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.AbstractResourceVariantTree#refresh(org.eclipse.core.resources.IResource, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IResource[] refresh(IResource resource, int depth, IProgressMonitor monitor) throws TeamException {
		IResource[] changedResources = null;
		monitor.beginTask(null, 100);
		// Wait up to 10 seconds for build to finish
		int count = 0;
		while (count < 10 
				&& (isJobInFamilyRunning(ResourcesPlugin.FAMILY_AUTO_BUILD)
				|| isJobInFamilyRunning(ResourcesPlugin.FAMILY_MANUAL_BUILD))) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Conitinue
			}	
			count++;
			Policy.checkCanceled(monitor);
		}
		ISchedulingRule rule = getSchedulingRule(resource);
		try {
			Platform.getJobManager().beginRule(rule, Policy.subMonitorFor(monitor, 1));
			if (!resource.getProject().isAccessible()) {
				// The project is closed so silently skip it
				return new IResource[0];
			}
			changedResources = super.refresh(resource, depth, Policy.subMonitorFor(monitor, 99));
		} finally {
			Platform.getJobManager().endRule(rule);
			monitor.done();
		}
		if (changedResources == null) return new IResource[0];
		return changedResources;
	}
	
	/**
	 * Return the scheduling rule that should be obtained for the given resource.
	 * This method is invoked from <code>refresh(IResource, int, IProgressMonitor)</code>.
	 * By default, the resource's project is returned. Subclasses may override.
	 * @param resource the resource being refreshed
	 * @return a scheduling rule or <code>null</code>
	 */
	protected ISchedulingRule getSchedulingRule(IResource resource) {
		return resource.getProject();
	}
	
	private boolean isJobInFamilyRunning(Object family) {
		Job[] jobs = Platform.getJobManager().find(family);
		if (jobs != null && jobs.length > 0) {
			for (int i = 0; i < jobs.length; i++) {
				Job job = jobs[i];
				if (job.getState() != Job.NONE) {
					return true;
				}
			}
		}
		return false;
	}
}
