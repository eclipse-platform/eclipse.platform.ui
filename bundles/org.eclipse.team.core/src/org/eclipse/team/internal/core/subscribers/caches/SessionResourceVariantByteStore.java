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

import java.util.*;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Assert;

/**
 * A <code>ResourceVariantByteStore</code> that caches the variant bytes in memory 
 * and does not persist them over workbench invocations.
 */
public class SessionResourceVariantByteStore extends ResourceVariantByteStore {

	private static final byte[] NO_REMOTE = new byte[0];
	
	private Map syncBytesCache = new HashMap();
	private Map membersCache = new HashMap();

	private Map getSyncBytesCache() {
		return syncBytesCache;
	}
	
	private byte[] internalGetSyncBytes(IResource resource) {
		return (byte[])getSyncBytesCache().get(resource);
	}
	
	private void internalAddToParent(IResource resource) {
		IContainer parent = resource.getParent();
		if (parent == null) return;
		List members = (List)membersCache.get(parent);
		if (members == null) {
			members = new ArrayList();
			membersCache.put(parent, members);
		}
		members.add(resource);
	}
	
	private void internalSetSyncInfo(IResource resource, byte[] bytes) {
		getSyncBytesCache().put(resource, bytes);
		internalAddToParent(resource);
	}

	private void internalRemoveFromParent(IResource resource) {
		IContainer parent = resource.getParent();
		List members = (List)membersCache.get(parent);
		if (members != null) {
			members.remove(resource);
			if (members.isEmpty()) {
				membersCache.remove(parent);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore#dispose()
	 */
	public void dispose() {
		syncBytesCache.clear();
		membersCache.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore#getBytes(org.eclipse.core.resources.IResource)
	 */
	public byte[] getBytes(IResource resource) throws TeamException {
		byte[] syncBytes = internalGetSyncBytes(resource);
		if (syncBytes != null && equals(syncBytes, NO_REMOTE)) {
			// If it is known that there is no remote, return null
			return null;
		}
		return syncBytes;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore#setBytes(org.eclipse.core.resources.IResource, byte[])
	 */
	public boolean setBytes(IResource resource, byte[] bytes) throws TeamException {
		Assert.isNotNull(bytes);
		byte[] oldBytes = internalGetSyncBytes(resource);
		if (oldBytes != null && equals(oldBytes, bytes)) return false;
		internalSetSyncInfo(resource, bytes);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore#removeBytes(org.eclipse.core.resources.IResource, int)
	 */
	public boolean flushBytes(IResource resource, int depth) throws TeamException {
		if (getSyncBytesCache().containsKey(resource)) {
			if (depth != IResource.DEPTH_ZERO) {
				IResource[] members = members(resource);
				for (int i = 0; i < members.length; i++) {
					IResource child = members[i];
					flushBytes(child, (depth == IResource.DEPTH_INFINITE) ? IResource.DEPTH_INFINITE: IResource.DEPTH_ZERO);
				}
			}
			getSyncBytesCache().remove(resource);
			internalRemoveFromParent(resource);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore#members(org.eclipse.core.resources.IResource)
	 */
	public IResource[] members(IResource resource) {
		List members = (List)membersCache.get(resource);
		if (members == null) {
			return new IResource[0];
		}
		return (IResource[]) members.toArray(new IResource[members.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore#setVariantDoesNotExist(org.eclipse.core.resources.IResource)
	 */
	public boolean deleteBytes(IResource resource) throws TeamException {
		return flushBytes(resource, IResource.DEPTH_ZERO);
	}

	/**
	 * Return <code>true</code> if no bytes are contained in this tree.
	 * @return <code>true</code> if no bytes are contained in this tree.
	 */
	public boolean isEmpty() {
		return syncBytesCache.isEmpty();
	}
}
