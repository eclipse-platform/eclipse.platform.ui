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

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;

/**
 * A <code>ResourceVariantTree</code> that optimizes the memory footprint
 * of a remote resource variant tree by only storing those bytes that
 * differ from a base resource variant tree. This class should only be used 
 * for cases where the base and remote are on the same line-of-descent. 
 * For example, when the remote tree represents the current state of a branch
 * and the base represents the state of the same branch when the local workspace
 * as last refreshed.
 * <p>
 * This class also contains the logic that allows subclasses to determine if
 * bytes stored in the remote tree are on a different line-of-descent than the base.
 * This is necessary because it is possible for the base tree to change in ways that 
 * invalidate the stored remote variants. For example, if the local resources are moved
 * from the main trunck to a branch, any cached remote resource variants would be stale.

 */
public abstract class DescendantResourceVariantTree extends ResourceVariantTree {
	ResourceVariantTree baseCache, remoteCache;

	public DescendantResourceVariantTree(ResourceVariantTree baseCache, ResourceVariantTree remoteCache) {
		this.baseCache = baseCache;
		this.remoteCache = remoteCache;
	}
	
	/**
	 * This method will dispose the remote cache but not the base cache.
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree#dispose()
	 */
	public void dispose() {
		remoteCache.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree#getBytes(org.eclipse.core.resources.IResource)
	 */
	public byte[] getBytes(IResource resource) throws TeamException {
		byte[] remoteBytes = remoteCache.getBytes(resource);
		byte[] baseBytes = baseCache.getBytes(resource);
		if (baseBytes == null) {
			// There is no base so use the remote bytes
			return remoteBytes;
		}
		if (remoteBytes == null) {
			if (isVariantKnown(resource)) {
				// The remote is known to not exist
				// TODO: The check for NO_REMOTE does not take into consideration the line-of-descent
				return remoteBytes;
			} else {
				// The remote was either never queried or was the same as the base.
				// In either of these cases, the base bytes are used.
				return baseBytes;
			}
		}
		if (isDescendant(resource, baseBytes, remoteBytes)) {
			// Only use the remote bytes if they are later on the same line-of-descent as the base
			return remoteBytes;
		}
		// Use the base sbytes since the remote bytes must be stale (i.e. are
		// not on the same line-of-descent
		return baseBytes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree#setBytes(org.eclipse.core.resources.IResource, byte[])
	 */
	public boolean setBytes(IResource resource, byte[] bytes) throws TeamException {
		byte[] baseBytes = baseCache.getBytes(resource);
		if (baseBytes != null && equals(baseBytes, bytes)) {
			// Remove the existing bytes so the base will be used (thus saving space)
			return remoteCache.removeBytes(resource, IResource.DEPTH_ZERO);
		} else {
			return remoteCache.setBytes(resource, bytes);
		}	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree#removeBytes(org.eclipse.core.resources.IResource, int)
	 */
	public boolean removeBytes(IResource resource, int depth) throws TeamException {
		return remoteCache.removeBytes(resource, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree#isVariantKnown(org.eclipse.core.resources.IResource)
	 */
	public boolean isVariantKnown(IResource resource) throws TeamException {
		return remoteCache.isVariantKnown(resource);
	}

	/**
	 * This method indicates whether the remote bytes are a later revision or version
	 * on the same line-of-descent as the base. A line of descent may be a branch or a fork
	 * (depending on the terminology used by the versioing server). If this method returns
	 * <code>false</code> then the remote bytes will be ignored by this tree.
	 * @param resource the local resource
	 * @param baseBytes the base bytes for the local resoource
	 * @param remoteBytes the remote bytes for the local resoource
	 * @return whether the remote bytes are later on the same line-of-descent as the base bytes
	 */
	protected abstract boolean isDescendant(IResource resource, byte[] baseBytes, byte[] remoteBytes) throws TeamException;
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree#setVariantDoesNotExist(org.eclipse.core.resources.IResource)
	 */
	public boolean setVariantDoesNotExist(IResource resource) throws TeamException {
		return remoteCache.setVariantDoesNotExist(resource);
	}

	/**
	 * Return the base tree from which the remote is descendant.
	 * @return Returns the base tree.
	 */
	protected ResourceVariantTree getBaseTree() {
		return baseCache;
	}

	/**
	 * Return the remote tree which contains bytes only for the resource variants
	 * that differ from those in the base tree.
	 * @return Returns the remote tree.
	 */
	protected ResourceVariantTree getRemoteTree() {
		return remoteCache;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantTree#members(org.eclipse.core.resources.IResource)
	 */
	public IResource[] members(IResource resource) throws TeamException {
		return getRemoteTree().members(resource);
	}

}
