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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.RemoteBytesSynchronizer;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;

/**
 * CVS specific remote synchronizer behavior
 */
public abstract class CVSRemoteSynchronizer extends RemoteBytesSynchronizer {

	public static final String SYNC_KEY_QUALIFIER = "org.eclipse.team.cvs"; //$NON-NLS-1$
	
	public CVSRemoteSynchronizer(String id) {
		super(new QualifiedName(SYNC_KEY_QUALIFIER, id));
	}

	public IRemoteResource getRemoteResource(IResource resource) throws TeamException {
		byte[] remoteBytes = getSyncBytes(resource);
		if (remoteBytes == null) {
			// There is no remote handle for this resource
			return null;
		} else {
			// TODO: This code assumes that the type of the remote resource
			// matches that of the local resource. This may not be true.
			if (resource.getType() == IResource.FILE) {
				byte[] parentBytes = getSyncBytes(resource.getParent());
				if (parentBytes == null) {
					CVSProviderPlugin.log(new CVSException( 
						Policy.bind("ResourceSynchronizer.missingParentBytesOnGet", getSyncName().toString(), resource.getFullPath().toString()))); //$NON-NLS-1$
					// Assume there is no remote and the problem is a programming error
					return null;
				}
				return RemoteFile.fromBytes(resource, remoteBytes, parentBytes);
			} else {
				return RemoteFolder.fromBytes(resource, remoteBytes);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.syncinfo.RemoteSynchronizer#setSyncBytes(org.eclipse.core.resources.IResource, byte[])
	 */
	public void setSyncBytes(IResource resource, byte[] bytes) throws TeamException {
		super.setSyncBytes(resource, bytes);
		if (getSyncBytes(resource) != null && !parentHasSyncBytes(resource)) {
			// Log a warning if there is no sync bytes available for the resource's
			// parent but there is valid sync bytes for the child
			CVSProviderPlugin.log(new TeamException(Policy.bind("ResourceSynchronizer.missingParentBytesOnSet", getSyncName().toString(), resource.getFullPath().toString()))); //$NON-NLS-1$
		}
	}

	/**
	 * Indicates whether the parent of the given local resource has sync bytes for its
	 * corresponding remote resource. The parent bytes of a remote resource are required
	 * (by CVS) to create a handle to the remote resource.
	 */
	protected boolean parentHasSyncBytes(IResource resource) throws TeamException {
		if (resource.getType() == IResource.PROJECT) return true;
		return (getSyncBytes(resource.getParent()) != null);
	}
	
	/**
	 * Return the sync bytes associated with the remote resource. A return
	 * value of <code>null</code> indicates that the remote resource does not exist.
	 */
	protected byte[] getRemoteSyncBytes(IResource local, IRemoteResource remote) throws TeamException {
		if (remote != null) {
			return ((RemoteResource)remote).getSyncBytes();
		} else {
			return null;
		}
	}
}
