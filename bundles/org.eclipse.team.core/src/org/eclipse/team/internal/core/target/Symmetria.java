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

/**
 * Synchronizes the given folder between the workspace and provider.
 */
public class Symmetria {

	/**
	 * Remote knows best.
	 */
	public IStatus get(
		ResourceState resourceState,
		int depth,
		IProgressMonitor progress) {

		IResource localResource = resourceState.getLocal();

		// If remote does not exist then simply ensure no local resource exists.
		if (!resourceState.hasRemote())
			return deleteLocal(localResource, progress);

		// If the remote resource is a file.
		if (resourceState.getRemoteType() == IResource.FILE) {
			// Replace any existing local resource with a copy of the remote file.
			IStatus deleteStatus = deleteLocal(localResource, progress);
			if (!deleteStatus.isOK())
				return deleteStatus;
			return resourceState.download(progress);
		}

		// The remote resource is a container.

		// If the local resource is a file, we must remove it first.
		if (localResource.getType() == IResource.FILE) {
			IStatus deleteStatus = deleteLocal(localResource, progress); // May not exist.
			if (!deleteStatus.isOK())
				return deleteStatus;
		}

		// If the local resource does not exist then it is created as a container.
		if (!localResource.exists()) {
			// Create a corresponding local directory.
			IStatus mkdirsStatus = mkLocalDirs(localResource, progress);
			if (!mkdirsStatus.isOK())
				return mkdirsStatus;
		}

		// Finally, resolve the collection membership based upon the depth parameter.
		switch (depth) {
			case IResource.DEPTH_ZERO :
				// If we are not considering members of the collection then we are done.
				return ITeamStatusConstants.OK_STATUS;
			case IResource.DEPTH_ONE :
				// If we are considering only the immediate members of the collection
				try {
					getFolderShallow(resourceState, progress);
				} catch (TeamException exception) {
					return exception.getStatus();
				}
				return ITeamStatusConstants.OK_STATUS;
			case IResource.DEPTH_INFINITE :
				// We are going in deep.
				return getFolderDeep(resourceState, progress);
			default :
				// We have covered all the legal cases.
				Assert.isLegal(false);
				return null; // Never reached.
		} // end switch
	}

	/**
	 * Synch the remote and local folder to depth.
	 */
	protected IStatus getFolderDeep(
		ResourceState collection,
		IProgressMonitor progress) {

		ResourceState[] childFolders;
		try {
			childFolders = getFolderShallow(collection, progress);
		} catch (TeamException exception) {
			// Problem getting the folder at this level.
			return exception.getStatus();
		}

		// If there are no further children then we are done.
		if (childFolders.length == 0)
			return ITeamStatusConstants.OK_STATUS;

		// There are children and we are going deep, the response will be a multi-status.
		MultiStatus multiStatus =
			new MultiStatus(
				ITeamStatusConstants.OK_STATUS.getPlugin(),
				ITeamStatusConstants.OK_STATUS.getCode(),
				ITeamStatusConstants.OK_STATUS.getMessage(),
				ITeamStatusConstants.OK_STATUS.getException());

		// Collect the responses in the multistatus.
		for (int i = 0; i < childFolders.length; i++)
			multiStatus.add(get(childFolders[i], IResource.DEPTH_INFINITE, progress));

		return multiStatus;
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
				IStatus downloadStatus = remoteChildState.download(progress);
				if (!downloadStatus.isOK())
					throw new TeamException(downloadStatus);
				// Remember that we have processed this child.
				surplusLocalChildren.remove(remoteChildState.getLocal());
			} else {
				// The remote resource is a container.
				remoteChildFolders.add(remoteChildState);
				// If the local child is not a container then it must be deleted.
				IResource localChild = remoteChildState.getLocal();
				if (localChild.exists() && (!(localChild instanceof IContainer)))
					checkedDeleteLocal(localChild, progress);
			} // end if
		} // end for

		// Remove each local child that does not have a corresponding remote resource.
		Iterator childrenItr = surplusLocalChildren.iterator();
		while (childrenItr.hasNext()) {
			IResource unseenChild = (IResource) childrenItr.next();
			checkedDeleteLocal(unseenChild, progress);
		} // end-while

		// Answer the array of children seen on the remote collection that are
		// themselves collections (to support depth operations).
		return (ResourceState[]) remoteChildFolders.toArray(
			new ResourceState[remoteChildFolders.size()]);
	}

	/**
	 * Calls delete local and throws an exceptionif a problem arises.
	 */
	protected void checkedDeleteLocal(
		IResource resource,
		IProgressMonitor progress) throws TeamException {
			
		IStatus deleteStatus = deleteLocal(resource, progress);
		if (!deleteStatus.isOK())
			throw new TeamException(ITeamStatusConstants.CONFLICT_STATUS);
	}
		
	/**
	 * Delete the local resource represented by the resource state.  Do not complain if the resource does not exist.
	 */
	protected IStatus deleteLocal(
		IResource resource,
		IProgressMonitor progress) {

		try {
			resource.delete(true, progress);
		} catch (CoreException exception) {
			//todo: we need to return the real exception
			return ITeamStatusConstants.IO_FAILED_STATUS;
		}
		
		// The delete succeeded.
		return ITeamStatusConstants.OK_STATUS;
	}

	/**
	 * Make local directories matching the description of the local resource state.
	 * XXX There has to be a better way.
	 */
	protected IStatus mkLocalDirs(IResource resource, IProgressMonitor progress) {
		
		IContainer project = resource.getProject();
		IPath path = resource.getProjectRelativePath();
		IFolder folder = project.getFolder(path);

		try {
			folder.create(false, true, progress);	// No force, yes make local.
		} catch (CoreException exception) {
			// The creation failed.
			return ITeamStatusConstants.IO_FAILED_STATUS;
		}	
		return ITeamStatusConstants.OK_STATUS;
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