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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;

/**
 * A resource synchronizer is responsible for managing synchronization information for
 * CVS resources.
 */
public abstract class ResourceSynchronizer {
	
	protected abstract QualifiedName getSyncName();
	
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
				return RemoteFolder.fromBytes((IContainer)resource, remoteBytes);
			}
		}
	}

	public abstract byte[] getSyncBytes(IResource resource) throws CVSException;

	/**
	 * Refreshes the contents of the resource synchronizer and returns the list
	 * of resources whose remote sync state changed. The <code>cacheFileContentsHint</code>
	 * indicates that the user of this synchronizer will be using the file contents. Subclasses can decide
	 * whether to cache file contents during the refresh or to allow them to be fetched when request.
	 * @param resources
	 * @param depth
	 * @param cacheFileContentsHint a hint which indicates whether file contents will be used
	 * @param monitor
	 * @return
	 * @throws TeamException
	 */
	public IResource[] refresh(IResource[] resources, int depth, boolean cacheFileContentsHint, IProgressMonitor monitor) throws TeamException {
		try {
			monitor.beginTask(null, 100);
			return new IResource[0];
		} finally {
			monitor.done();
		}
	}

}
