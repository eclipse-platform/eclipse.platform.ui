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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;

/**
 * A resource synchronizer is responsible for managing synchronization information for
 * CVS resources.
 */
public abstract class ResourceSynchronizer {
	
	public abstract byte[] getSyncBytes(IResource resource) throws CVSException;
	
	/**
	 * 
	 * @param resource
	 * @return
	 * @throws TeamException
	 */
	public IRemoteResource getRemoteResource(IResource resource) throws TeamException {
		byte[] remoteBytes = getSyncBytes(resource);
		if (remoteBytes == null) {
			// There is no remote handle for this resource
			return null;
		} else {
			// TODO: This code assumes that the type of the remote resource
			// matches that of the local resource. This may not be true.
			// TODO: This is rather complicated. There must be a better way!
			if (resource.getType() == IResource.FILE) {
				return RemoteFile.fromBytes(resource, remoteBytes, getSyncBytes(resource.getParent()));
			} else {
				return RemoteFolder.fromBytes((IContainer)resource, remoteBytes);
			}
		}
	}

	/**
	 * Refreshes the contents of the resource synchronizer and returns the list
	 * of resources whose remote sync state changed.
	 * @param resources
	 * @param depth
	 * @param monitor
	 * @return
	 */
	public IResource[] refresh(IResource[] resources, int depth, IProgressMonitor monitor) throws TeamException {
		return new IResource[0];
	}
}
