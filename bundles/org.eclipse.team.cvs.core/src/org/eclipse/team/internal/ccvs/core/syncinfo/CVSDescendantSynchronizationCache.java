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
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.subscribers.caches.*;
import org.eclipse.team.internal.ccvs.core.*;

/**
 * CVS sycnrhonization cache that ignores stale remote bytes
 */
public class CVSDescendantSynchronizationCache extends DescendantResourceVariantByteStore {

	public CVSDescendantSynchronizationCache(ResourceVariantByteStore baseCache, PersistantResourceVariantByteStore remoteCache) {
		super(baseCache, remoteCache);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.DescendantSynchronizationCache#isDescendant(org.eclipse.core.resources.IResource, byte[], byte[])
	 */
	protected boolean isDescendant(IResource resource, byte[] baseBytes, byte[] remoteBytes) throws TeamException {
		if (resource.getType() != IResource.FILE) return true;
		try {
			return ResourceSyncInfo.isLaterRevisionOnSameBranch(remoteBytes, baseBytes);
		} catch (CVSException e) {
			throw TeamException.asTeamException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.helpers.SynchronizationCache#setSyncBytes(org.eclipse.core.resources.IResource, byte[])
	 */
	public boolean setBytes(IResource resource, byte[] bytes) throws TeamException {
		boolean changed = super.setBytes(resource, bytes);
		if (resource.getType() == IResource.FILE && getBytes(resource) != null && !parentHasSyncBytes(resource)) {
			// Log a warning if there is no sync bytes available for the resource's
			// parent but there is valid sync bytes for the child
			CVSProviderPlugin.log(new TeamException(Policy.bind("ResourceSynchronizer.missingParentBytesOnSet", ((PersistantResourceVariantByteStore)getRemoteTree()).getSyncName().toString(), resource.getFullPath().toString()))); //$NON-NLS-1$
		}
		return changed;
	}

	/**
	 * Indicates whether the parent of the given local resource has sync bytes for its
	 * corresponding remote resource. The parent bytes of a remote resource are required
	 * (by CVS) to create a handle to the remote resource.
	 */
	protected boolean parentHasSyncBytes(IResource resource) throws TeamException {
		if (resource.getType() == IResource.PROJECT) return true;
		return (getBytes(resource.getParent()) != null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.caches.ResourceVariantByteStore#isVariantKnown(org.eclipse.core.resources.IResource)
	 */
	public boolean isVariantKnown(IResource resource) throws TeamException {
		return ((PersistantResourceVariantByteStore)getRemoteTree()).isVariantKnown(resource);
	}
}
