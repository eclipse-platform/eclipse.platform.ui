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
package org.eclipse.team.internal.core.subscribers.caches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.IResourceVariant;

/**
 * This class provides the logic for refreshing a resource variant tree that
 * is cached in a byte store. 
 * It provides the logic to traverse the local resource and variant resource trees in 
 * order to update the bytes stored in
 * a <code>ResourceVariantByteStore</code>. It also accumulates and returns all local resources 
 * for which the corresponding resource variant has changed.
 */
public abstract class ResourceVariantTree extends AbstractResourceVariantTree {
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.IResourceVariantTree#members(org.eclipse.core.resources.IResource)
	 */
	public IResource[] members(IResource resource) throws TeamException {
		return getByteStore().members(resource);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.IResourceVariantTree#hasResourceVariant(org.eclipse.core.resources.IResource)
	 */
	public boolean hasResourceVariant(IResource resource) throws TeamException {
		return getByteStore().getBytes(resource) != null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.AbstractResourceVariantTree#setVariant(org.eclipse.core.resources.IResource, org.eclipse.team.core.synchronize.IResourceVariant)
	 */
	protected boolean setVariant(IResource local, IResourceVariant remote) throws TeamException {
		ResourceVariantByteStore cache = getByteStore();
		byte[] newRemoteBytes = getBytes(local, remote);
		boolean changed;
		if (newRemoteBytes == null) {
			changed = cache.deleteBytes(local);
		} else {
			changed = cache.setBytes(local, newRemoteBytes);
		}
		return changed;
	}
	
	/**
	 * Get the bytes to be stored in the <code>ResourceVariantByteStore</code> 
	 * from the given resource variant.
	 * @param local the local resource
	 * @param remote the corresponding resource variant handle
	 * @return the bytes for the resource variant.
	 */
	protected abstract byte[] getBytes(IResource local, IResourceVariant remote) throws TeamException;

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
	 * Flush any variants for the given resource to the depth specified
	 * @param resource the local resource
	 * @param depth the depth of the flush
	 * @throws TeamException
	 */
	private void flushVariants(IResource resource, int depth) throws TeamException {
		getByteStore().flushBytes(resource, depth);
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
				// TODO: Not very generic! 
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
	
	/**
	 * Get the byte store that is used to cache the serialization bytes
	 * for the resource variants of this tree. A byte store is used
	 * to reduce the memory footprint of the tree.
	 * @return the resource variant tree that is being refreshed.
	 */
	protected abstract ResourceVariantByteStore getByteStore();
	
	/**
	 * @param project
	 */
	public void removeRoot(IResource resource) throws TeamException {
		getByteStore().flushBytes(resource, IResource.DEPTH_INFINITE);
	}
}
