package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Policy;
//
public class CopyVisitor implements IUnifiedTreeVisitor {
	protected FileSystemStore localStore;

	/** root destination */
	protected IResource rootDestination;

	/** root destination local location */
	protected IPath rootDestinationLocalLocation;

	/** reports progress */
	protected IProgressMonitor monitor;

	/** force flag */
	protected boolean force;

	/** segments to drop from the source name */
	protected int segmentsToDrop;

	/** stores problems encountered while copying */
	protected MultiStatus status;

	/** visitor to refresh unsynchronized nodes */
	protected RefreshLocalVisitor refreshLocalVisitor;
public CopyVisitor(IResource rootSource, IResource destination, boolean force, IProgressMonitor monitor) {
	this.rootDestination = destination;
	this.rootDestinationLocalLocation = destination.getLocation();
	this.force = force;
	this.monitor = monitor;
	this.segmentsToDrop = rootSource.getFullPath().segmentCount();
	status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INFO, Policy.bind("localstore.copyProblem"), null);
}
protected void copy(UnifiedTreeNode node) {
	Resource source = (Resource) node.getResource();
	IPath sufix = getDestinationSufix(node);
	Resource destination = getDestinationResource(source, sufix);
	if (!copyProperties(source, destination))
		return;
	copyContents(node, source, destination);
}
protected void copyContents(UnifiedTreeNode node, Resource source, Resource destination) {
	try {
		if (destination.getType() == IResource.FOLDER)
			 ((IFolder) destination).create(force, true, null);
		else {
			// XXX: should use transfer streams in order to report better progress
			((IFile) destination).create(((IFile) source).getContents(false), force, null);
			// update the destination timestamp on disk
			long lastModified = node.getLastModified();
			destination.getResourceInfo(false, true).setLocalSyncInfo(lastModified);
			destination.getLocation().toFile().setLastModified(lastModified);
			// update file attributes
			CoreFileSystemLibrary.copyAttributes(source.getLocation().toOSString(), destination.getLocation().toOSString(), false);
		}
	} catch (CoreException e) {
		status.add(e.getStatus());
	}
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
protected IPath getDestinationSufix(UnifiedTreeNode node) {
	IPath source = node.getResource().getFullPath();
	return source.removeFirstSegments(segmentsToDrop);
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
		boolean wasSynchronized = isSynchronized(node);
		if (force && !wasSynchronized) {
			synchronize(node);
			// If not synchronized, the monitor did not take this resource into account.
			// So, do not report work on it.
			work = 0;
		}
		if (!force && !wasSynchronized) {
			IPath path = node.getResource().getFullPath();
			String message = Policy.bind("localstore.resourceIsOutOfSync", path.toString());
			status.add(new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, path, message, null));
			return true;
		}
		copy(node);
		return true;
	} finally {
		monitor.worked(work);
	}
}
}