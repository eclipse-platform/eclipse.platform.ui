/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

//
public class CopyVisitor implements IUnifiedTreeVisitor {
	protected FileSystemStore localStore;

	/** root destination */
	protected IResource rootDestination;

	/** root destination local location */
	protected IPath rootDestinationLocalLocation;

	/** reports progress */
	protected IProgressMonitor monitor;

	/** update flags */
	protected int updateFlags;

	/** force flag */
	protected boolean force;

	/** deep copy flag */
	protected boolean isDeep;

	/** segments to drop from the source name */
	protected int segmentsToDrop;

	/** stores problems encountered while copying */
	protected MultiStatus status;

	/** visitor to refresh unsynchronized nodes */
	protected RefreshLocalVisitor refreshLocalVisitor;

	public CopyVisitor(IResource rootSource, IResource destination, int updateFlags, IProgressMonitor monitor) {
		this.rootDestination = destination;
		this.rootDestinationLocalLocation = destination.getLocation();
		this.updateFlags = updateFlags;
		this.isDeep = (updateFlags & IResource.SHALLOW) == 0;
		this.force = (updateFlags & IResource.FORCE) != 0;
		this.monitor = monitor;
		this.segmentsToDrop = rootSource.getFullPath().segmentCount();
		this.status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IStatus.INFO, Policy.bind("localstore.copyProblem"), null); //$NON-NLS-1$
	}

	protected boolean copy(UnifiedTreeNode node) {
		Resource source = (Resource) node.getResource();
		IPath sufix = source.getFullPath().removeFirstSegments(segmentsToDrop);
		Resource destination = getDestinationResource(source, sufix);
		if (!copyProperties(source, destination))
			return false;
		return copyContents(node, source, destination);
	}

	protected boolean copyContents(UnifiedTreeNode node, Resource source, Resource destination) {
		try {
			if (!isDeep && source.isLinked()) {
				destination.createLink(source.getRawLocation(), updateFlags & IResource.ALLOW_MISSING_LOCAL, null);
				return false;
			}
			if (destination.getType() == IResource.FOLDER) {
				((IFolder) destination).create(updateFlags, true, null);
				CoreFileSystemLibrary.copyAttributes(node.getLocalLocation(), destination.getLocation().toOSString(), false);
				return true;
			}
			// XXX: should use transfer streams in order to report better progress
			((IFile) destination).create(((IFile) source).getContents(false), updateFlags, null);
			// update the destination timestamp on disk
			long lastModified = node.getLastModified();
			ResourceInfo destinationInfo = destination.getResourceInfo(false, true);
			destinationInfo.setLocalSyncInfo(lastModified);
			//if it's a deep copy of a link, need to clear the linked flag
			destinationInfo.clear(ICoreConstants.M_LINK);
			IPath destinationLocation = destination.getLocation();
			destinationLocation.toFile().setLastModified(lastModified);
			//update timestamps on aliases
			//XXX: this will update aliases twice because setContents above will also do it
			((Workspace) rootDestination.getWorkspace()).getAliasManager().updateAliases(destination, destinationLocation, IResource.DEPTH_ZERO, monitor);
			// update file attributes
			CoreFileSystemLibrary.copyAttributes(node.getLocalLocation(), destinationLocation.toOSString(), false);
			destination.getLocalManager().getHistoryStore().copyHistory(source, destination);
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
		return false;
	}

	protected boolean copyProperties(Resource target, Resource destination) {
		try {
			target.getPropertyManager().copy(target, destination, IResource.DEPTH_ZERO);
			return true;
		} catch (CoreException e) {
			status.add(e.getStatus());
			return false;
		}
	}

	protected Resource getDestinationResource(Resource source, IPath sufix) {
		IPath destinationPath = rootDestination.getFullPath().append(sufix);
		return getWorkspace().newResource(destinationPath, source.getType());
	}

	/**
	 * This is done in order to generate less garbage.
	 */
	protected RefreshLocalVisitor getRefreshLocalVisitor() {
		if (refreshLocalVisitor == null)
			refreshLocalVisitor = new RefreshLocalVisitor(Policy.monitorFor(null));
		return refreshLocalVisitor;
	}

	public IStatus getStatus() {
		return status;
	}

	protected FileSystemStore getStore() {
		if (localStore == null)
			localStore = new FileSystemStore();
		return localStore;
	}

	protected Workspace getWorkspace() {
		return (Workspace) rootDestination.getWorkspace();
	}

	protected boolean isSynchronized(UnifiedTreeNode node) {
		/* does the resource exist in workspace and file system? */
		if (!node.existsInWorkspace() || !node.existsInFileSystem())
			return false;
		/* we don't care about folder last modified */
		if (node.isFolder() && node.getResource().getType() == IResource.FOLDER)
			return true;
		/* is lastModified different? */
		Resource target = (Resource) node.getResource();
		long lastModifed = target.getResourceInfo(false, false).getLocalSyncInfo();
		if (lastModifed != node.getLastModified())
			return false;
		return true;
	}

	protected void synchronize(UnifiedTreeNode node) throws CoreException {
		getRefreshLocalVisitor().visit(node);
	}

	public boolean visit(UnifiedTreeNode node) throws CoreException {
		Policy.checkCanceled(monitor);
		int work = 1;
		try {
			//location can be null if based on an undefined variable
			if (node.getLocalLocation() == null) {
				//should still be a best effort copy
				IPath path = node.getResource().getFullPath();
				String message = Policy.bind("localstore.locationUndefined", path.toString()); //$NON-NLS-1$
				status.add(new ResourceStatus(IResourceStatus.FAILED_READ_LOCAL, path, message, null));
				return false;
			}
			boolean wasSynchronized = isSynchronized(node);
			if (force && !wasSynchronized) {
				synchronize(node);
				// If not synchronized, the monitor did not take this resource into account.
				// So, do not report work on it.
				work = 0;
				//if source still doesn't exist, then fail because we can't copy a missing resource
				if (!node.existsInFileSystem()) {
					IPath path = node.getResource().getFullPath();
					String message = Policy.bind("resources.mustExist", path.toString()); //$NON-NLS-1$
					status.add(new ResourceStatus(IResourceStatus.RESOURCE_NOT_FOUND, path, message, null));
					return false;
				}
			}
			if (!force && !wasSynchronized) {
				IPath path = node.getResource().getFullPath();
				String message = Policy.bind("localstore.resourceIsOutOfSync", path.toString()); //$NON-NLS-1$
				status.add(new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, path, message, null));
				return true;
			}
			return copy(node);
		} finally {
			monitor.worked(work);
		}
	}

}