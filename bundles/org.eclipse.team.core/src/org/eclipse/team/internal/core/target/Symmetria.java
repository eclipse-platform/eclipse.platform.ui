/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.core.target;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * Synchronizes the given folder between the workspace and provider.
 */
public class Symmetria {

	/**
	 * Remote knows best.
	 */
	public void get(
		ResourceState resourceState,
		int depth,
		IProgressMonitor progress) throws TeamException {

		IResource localResource = resourceState.getLocal();

		// If remote does not exist then simply ensure no local resource exists.
		if (!resourceState.hasRemote()) {
			deleteLocal(localResource, progress);
			return;
		}
		
		// If the remote resource is a file.
		if (resourceState.getRemoteType() == IResource.FILE) {
			// Replace any existing local resource with a copy of the remote file.
			deleteLocal(localResource, progress);
			resourceState.download(progress);
			return;
		}

		// The remote resource is a container.

		// If the local resource is a file, we must remove it first.
		if (localResource.getType() == IResource.FILE)
			deleteLocal(localResource, progress); // May not exist.

		// If the local resource does not exist then it is created as a container.
		if (!localResource.exists()) {
			// Create a corresponding local directory.
			mkLocalDirs(localResource, progress);
		}

		// Finally, resolve the collection membership based upon the depth parameter.
		switch (depth) {
			case IResource.DEPTH_ZERO :
				// If we are not considering members of the collection then we are done.
				return;
			case IResource.DEPTH_ONE :
				// If we are considering only the immediate members of the collection
				getFolderShallow(resourceState, progress);
				return;
			case IResource.DEPTH_INFINITE :
				// We are going in deep.
				getFolderDeep(resourceState, progress);
				return;
			default :
				// We have covered all the legal cases.
				Assert.isLegal(false);
				return; // Never reached.
		} // end switch
	}

	/**
	 * Synch the remote and local folder to depth.
	 */
	protected void getFolderDeep(
		ResourceState collection,
		IProgressMonitor progress) throws TeamException {

		// Could throw if problem getting the folder at this level.
		ResourceState[] childFolders = getFolderShallow(collection, progress);

		// If there are no further children then we are done.
		if (childFolders.length == 0)
			return;

		// Collect the responses in the multistatus.
		for (int i = 0; i < childFolders.length; i++)
			get(childFolders[i], IResource.DEPTH_INFINITE, progress);

		return;
	}

	/**
	 * Synchronize from the remote provider to the workspace.
	 * Assume that the 'remote' folder is correct, and change the local
	 * folder to look like the remote folder.
	 * 
	 * returns an array of children of the remote resource that are themselves
	 * collections.
	 */
	protected ResourceState[] getFolderShallow(
		ResourceState containerState,
		IProgressMonitor progress) throws TeamException {

		// We are assuming that the resource is a container.
		Assert.isLegal(containerState.getLocal() instanceof IContainer);
		IContainer localContainer = (IContainer)containerState.getLocal();

		// Get list of all _remote_ children.
		ResourceState[] remoteChildren = containerState.getRemoteChildren();

		// This will be the list of remote children that are themselves containers.
		Set remoteChildFolders = new HashSet();

		// Make a list of _local_ children that have not yet been processed,
		IResource[] localChildren = getLocalChildren(localContainer);
		Set surplusLocalChildren = new HashSet(localChildren.length);
		surplusLocalChildren.addAll(Arrays.asList(localChildren));

		// For each remote child that is a file, make the local file content equivalent.
		for (int i = 0; i < remoteChildren.length; i++) {
			ResourceState remoteChildState = remoteChildren[i];
			// If the remote child is a container add it to the list, and ensure that the local child
			// is a folder if it exists.
			if (remoteChildState.getRemoteType() == IResource.FILE) {
				// The remote resource is a file.  Copy the content of the remote file
				// to the local file, overwriting any existing content that may exist, and
				// creating the file if it doesn't.
				remoteChildState.download(progress);
				// Remember that we have processed this child.
				surplusLocalChildren.remove(remoteChildState.getLocal());
			} else {
				// The remote resource is a container.
				remoteChildFolders.add(remoteChildState);
				// If the local child is not a container then it must be deleted.
				IResource localChild = remoteChildState.getLocal();
				if (localChild.exists() && (!(localChild instanceof IContainer)))
					deleteLocal(localChild, progress);
			} // end if
		} // end for

		// Remove each local child that does not have a corresponding remote resource.
		Iterator childrenItr = surplusLocalChildren.iterator();
		while (childrenItr.hasNext()) {
			IResource unseenChild = (IResource) childrenItr.next();
			deleteLocal(unseenChild, progress);
		} // end-while

		// Answer the array of children seen on the remote collection that are
		// themselves collections (to support depth operations).
		return (ResourceState[]) remoteChildFolders.toArray(
			new ResourceState[remoteChildFolders.size()]);
	}

	/**
	 * Delete the local resource represented by the resource state.  Do not complain if the resource does not exist.
	 */
	protected void deleteLocal(
		IResource resource,
		IProgressMonitor progress) throws TeamException {

		try {
			resource.delete(true, progress);
		} catch (CoreException exception) {
			throw TeamPlugin.wrapException(exception);
		}
	}

	/**
	 * Make local directories matching the description of the local resource state.
	 * XXX There has to be a better way.
	 */
	protected void mkLocalDirs(IResource resource, IProgressMonitor progress) throws TeamException {	
		IContainer project = resource.getProject();
		IPath path = resource.getProjectRelativePath();
		IFolder folder = project.getFolder(path);

		try {
			folder.create(false, true, progress);	// No force, yes make local.
		} catch (CoreException exception) {
			// The creation failed.
			throw TeamPlugin.wrapException(exception);
		}	
	}
	
	/**
	 * Get an array of local children of the given container, or an empty array if the
	 * container does not exist or has no children.
	 */
	protected IResource[] getLocalChildren(IContainer container) throws TeamException {
		if (container.exists())
			try {
				return container.members();
			} catch (CoreException exception) {
				throw new TeamException(ITeamStatusConstants.IO_FAILED_STATUS);
			}
		return new IResource[0];
	}
}