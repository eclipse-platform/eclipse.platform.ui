/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.core.target;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;

public abstract class TargetProvider {
	/**
	 * Answers the site which this target is associated with
	 * @return a printable string
	 */	
	public abstract Site getSite();

	/**
	 * Answers the full path where the provider stores/retrieves to/from.
	 * @return a printable string
	 */	
	public abstract URL getURL();

	/**
	 * Updates the local resource to have the same content as the corresponding remote
	 * resource. Where the local resource does not exist, this method will create it.
	 * Where, the remote resource does not exist and the local does, this method will delete
	 * the local resource.
	 * <p>
	 * If the remote resource is a container (e.g. folder or project) this operation is equivalent 
	 * to getting each member of the remote resource, thereby updating the
	 * content of existing local members, creating local members to receive new remote resources,
	 * and deleting local members that no longer have a corresponding remote resource.</p>
	 * <p>
	 * Interrupting the method (via the progress monitor) may lead to partial, but consistent, results.</p>
	 * 
	 * @param resources an array of local resources to update from the corresponding remote
	 * resources.
	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * @throws TeamException if there is a problem getting one or more of the resources.  The
	 * exception will contain multiple statuses, one for each resource in the <code>resources</code>
	 * array. 
	 */
	public abstract void get(IResource[] resources, IProgressMonitor progress) throws TeamException;
	
	/**
	 * Updates the local resource to have the same content as the given remote
	 * resource. Where the local resource does not exist, this method will create it.
	 * <p>
	 * If the remote resource is a container (e.g. folder or project) this operation is equivalent 
	 * to getting each non-container member of the remote resource, thereby updating the
	 * content of existing local members, creating local members to receive new remote resources,
	 * and deleting local members that no longer have a corresponding remote resource.</p>
	 * <p>
	 * Interrupting the method (via the progress monitor) may lead to partial, but consistent, results.</p>
	 * 
	 * @param resources an array of local resources to update from the corresponding remote
	 * resources.
	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * @throws TeamException if there is a problem getting one or more of the resources.  The
	 * exception will contain multiple statuses, one for each resource in the <code>resources</code>
	 * array. 
	 */
	public abstract void get(IResource resource, IRemoteTargetResource remote, IProgressMonitor progress) throws TeamException;

	/**
	 * Transfers the content of the local resource to the corresponding remote resource.
	 * <p>
	 * If a remote resource does not exist this method creates a new remote resource with the same content
	 * as the given local resource.  The local resource is said to <i>correspond</i> to the new remote resource.
	 * If the local resource doesn't exist and a remote does, the remote resource will be deleted.
	 * </p>
	 * <p>
	 * If the local resource is a container (e.g. folder or project) this operation is equivalent 
	 * to putting each member of the local resource, thereby updating the
	 * content of existing remote members, creating remote members to receive new local resources,
	 * and deleting remote members that no longer have a corresponding local resource.</p>
	 * <p>
	 * @param resources an array of local resources to be put.
	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * @throws TeamException if there is a problem put'ing one or more of the resources.
	 * The exception will contain multiple status', one for each resource in the <code>resources</code>
	 * array.
	 */
	public abstract void put(IResource[] resources, IProgressMonitor progress) throws TeamException;
	
	/**
	 * Returns a remote resource handle at the path of the given local resource.
	 * 
	 * @param resource local resource path to be used to construct the remote handle's path
	 * @return a handle to a remote resource that may or may not exist
	 */
	public abstract IRemoteTargetResource getRemoteResourceFor(IResource resource);
	
	/**
	 * Returns a remote resource handle at the path of this target provider's URL.
	 * @return a handle to a remote resource that may or may not exist
	 */
	public abstract IRemoteTargetResource getRemoteResource();
	
	/**
	 * Replies true if its believed possible to get the given resource.
	 * This intended to be a relatively quick operation, presumably based on local state of the provider
	 * or it may be used to indicate that the provider only supports data transfer in one direction.
	 * It is not intended to indicate the existance of a remote resource. Hence, this method may return true
	 * even if there is no remote resource.
	 */
	public boolean canGet(IResource resource) {
		return true;
	}

	/**
	 * Replies true if its believed possible to put the given resource.
	 * This intended to be a relatively quick operation, presumably based on local state of the provider
	 * or it may be used to indicate that the provider only supports data transfer in one direction.
	 */
	public boolean canPut(IResource resource) {
		return true;
	}
	
	/**
	 * Answers true if the base identifier of the given resource is different to the
	 * current released state of the resource. Also answer true if there was not 
	 * previously a correspondance between the local and remote resource.
	 * 
	 * @param resource the resource to test
	 * @param monitor a progress monitor
	 * 
	 * @throws TeamException if there is a problem getting this information from the server.
	 */
	public abstract boolean isOutOfDate(IResource resource, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Answer true if the local resource currently has a different timestamp to the
	 * base timestamp for this resource. Answer true for local resources that no longer exist
	 * (i.e. the local resource did exist and corresponded to a remote resource) and false if the 
	 * local resource does not exist and was never mapped to a remote resource.
	 * 
	 * @param resource the resource to test
	 */
	public abstract boolean isDirty(IResource resource);

	public abstract void deregister(IProject project);
	
	/**
	 * This method provides a mechanism for allowing a target provider to optimize connections
	 * over a set of operations. The default behavior of the method is to run the provided runnable
	 * and to supply it with the provided progress monitor. Specific target providers may override this
	 * method in order to ensure that the number of connections made to a remote server is minimized.
	 * Calls to this method are nestable and it is up to the target provider to ensure that this is 
	 * supported.
	 */
	public void run(ITargetRunnable runnable, IProgressMonitor monitor) throws TeamException {
		runnable.run(monitor);
	}
}
