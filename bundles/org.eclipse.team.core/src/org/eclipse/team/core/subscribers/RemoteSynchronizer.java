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
package org.eclipse.team.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;

/**
 * A remote synchronizer provides API to access a handle for a resource resource
 * associated with a local resource (that may or may not exist locally).
 * API is also provided to trigger a refresh in order to cache the remote state
 * for later retrieval.
 */
public abstract class RemoteSynchronizer {
	
	/**
	 * Return a remote resource handle created from the remote sync bytes associated
	 * with the local resource for this synchronizer.
	 * 
	 * @param resource the local resource
	 * @return the IRemoteResource handle for a remote resource
	 * @throws TeamException
	 */
	public abstract IRemoteResource getRemoteResource(IResource resource) throws TeamException;

	/**
	 * Return whether the given resource has a corresponding remote resource that
	 * is known to exist (at the last point in time that a refresh was performed).
	 * 
	 * @param resource the local resource handle
	 * @return <code>true</code> if a corrrespondin remote resource is know to exist
	 * @throws TeamException
	 */
	public abstract boolean hasRemote(IResource resource) throws TeamException;

	/**
	 * Refreshes the contents of the resource synchronizer and returns the list
	 * of resources whose remote synchronization state changed since the last refresh. 
	 * The <code>cacheFileContentsHint</code> indicates that the user of this synchronizer 
	 * will be using the file contents. Subclasses can decide
	 * whether to cache file contents during the refresh or to
	 * allow them to be fetched when request.
	 * 
	 * @param resources the resources to refresh
	 * @param depth the depth of the operation
	 * @param cacheFileContentsHint a hint which indicates whether file contents will be used
	 * @param monitor the progress monitor
	 * @return the resources whose remote has changed since the last refresh
	 * @throws TeamException
	 */
	public abstract IResource[] refresh(IResource[] resources, int depth, boolean cacheFileContentsHint, IProgressMonitor monitor) throws TeamException;

}
