/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.syncinfo;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.IResourceVariant;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore;
import org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree;

/**
 * CVS Specific refresh operation
 */
public class CVSRefreshOperation extends ResourceVariantTree {

	private ResourceVariantByteStore cache, baseCache;
	private CVSTag tag;
	private boolean cacheFileContentsHint;
	private CVSSyncTreeSubscriber subscriber;

	public CVSRefreshOperation(ResourceVariantByteStore cache, ResourceVariantByteStore baseCache, CVSTag tag, boolean cacheFileContentsHint) {
		this.tag = tag;
		this.cache = cache;
		this.baseCache = cache;
		this.cacheFileContentsHint = cacheFileContentsHint;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.RefreshOperation#getSynchronizationCache()
	 */
	protected ResourceVariantByteStore getByteStore() {
		return cache;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.RefreshOperation#getRemoteSyncBytes(org.eclipse.core.resources.IResource, org.eclipse.team.core.subscribers.ISubscriberResource)
	 */
	protected byte[] getBytes(IResource local, IResourceVariant remote) throws TeamException {
		if (remote != null) {
			return ((RemoteResource)remote).getSyncBytes();
		} else {
			if (local.getType() == IResource.FOLDER && baseCache != null) {
				// If there is no remote, use the local sync for the folder
				return baseCache.getBytes(local);
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
		return (IResourceVariant)CVSWorkspaceRoot.getRemoteTree(resource, tag, cacheFileContentsHint, monitor);
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
	 * @see org.eclipse.team.internal.core.subscribers.caches.IResourceVariantTree#getRoots()
	 */
	public IResource[] getRoots() {
		return subscriber.roots();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.IResourceVariantTree#getResourceVariant(org.eclipse.core.resources.IResource)
	 */
	public IResourceVariant getResourceVariant(IResource resource) throws TeamException {
		return subscriber.getRemoteResource(resource, getByteStore());
	}

}
