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
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.properties.PropertyManager;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ILock;

/**
 * @since 2.0
 * 
 * Implementation note: Since the move/delete hook involves running third
 * party code, the workspace lock is not held.  This means the workspace
 * lock must be reacquired whenever we need to manipulate the workspace
 * in any way.  All entry points from third party code back into the tree must
 * be done in an acquire/release pair.
 */
class ResourceTree implements IResourceTree {

	/**
	 * The lock to acquire when the workspace needs to be manipulated
	 */
	private ILock lock;
	private MultiStatus multistatus;
	private int updateFlags;
	private boolean isValid = true;

	/**
	 * Constructor for this class.
	 */
	public ResourceTree(ILock lock, MultiStatus status, int updateFlags) {
		super();
		this.lock = lock;
		this.multistatus = status;
		this.updateFlags = updateFlags;
	}

	/**
	 * The specific operation for which this tree was created has completed and this tree
	 * should not be used anymore. Ensure that this is the case by making it invalid. This
	 * is checked by all API methods.
	 */
	void makeInvalid() {
		this.isValid = false;
	}

	/**
	 * @see IResourceTree#addToLocalHistory(IFile)
	 */
	public void addToLocalHistory(IFile file) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			if (!file.exists())
				return;
			IPath path = file.getLocation();
			if (path == null || !path.toFile().exists())
				return;
			long lastModified = internalComputeTimestamp(path.toOSString());
			((Resource) file).getLocalManager().getHistoryStore().addState(file.getFullPath(), path, lastModified, false);
		} finally {
			lock.release();
		}
	}

	/**
	 * Copies the local history of source to destination.  Note that if source
	 * is an IFolder, it is assumed that the same structure exists under destination
	 * and the local history of any IFile under source will be copied to the
	 * associated IFile under destination.
	 */
	private void copyLocalHistory(IResource source, IResource destination) {
		((Resource) destination).getLocalManager().getHistoryStore().copyHistory(source.getFullPath(), destination.getFullPath());
	}

	/**
	 * @see IResourceTree#movedFile(IFile, IFile)
	 */
	public void movedFile(IFile source, IFile destination) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			// Do nothing if the resource doesn't exist.
			if (!source.exists())
				return;
			// If the destination already exists then we have a problem.
			if (destination.exists()) {
				String message = Policy.bind("resources.mustNotExist", destination.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, destination.getFullPath(), message);
				// log the status but don't return until we try and move the rest of the resource information.
				failed(status);
			}

			// Move the resource's persistent properties.
			PropertyManager propertyManager = ((Resource) source).getPropertyManager();
			try {
				propertyManager.copy(source, destination, IResource.DEPTH_ZERO);
				propertyManager.deleteProperties(source, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorPropertiesMove", source.getFullPath().toString(), destination.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
				// log the status but don't return until we try and move the rest of the resource information.
				failed(status);
			}

			// Move the node in the workspace tree.
			Workspace workspace = (Workspace) source.getWorkspace();
			try {
				workspace.move((Resource) source, destination.getFullPath(), IResource.DEPTH_ZERO, updateFlags, false);
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorMoving", source.getFullPath().toString(), destination.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
				// log the status but don't return until we try and move the rest of the resource information.
				failed(status);
			}

			// Generate the marker deltas.
			try {
				workspace.getMarkerManager().moved(source, destination, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorMarkersDelete", source.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
				failed(status);
			}

			// Copy the local history information
			copyLocalHistory(source, destination);
		} finally {
			lock.release();
		}
	}

	/**
	 * @see IResourceTree#movedFolderSubtree(IFolder, IFolder)
	 */
	public void movedFolderSubtree(IFolder source, IFolder destination) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			// Do nothing if the source resource doesn't exist.
			if (!source.exists())
				return;
			// If the destination already exists then we have an error.
			if (destination.exists()) {
				String message = Policy.bind("resources.mustNotExist", destination.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, destination.getFullPath(), message);
				failed(status);
				return;
			}

			// Move the folder properties.
			int depth = IResource.DEPTH_INFINITE;
			PropertyManager propertyManager = ((Resource) source).getPropertyManager();
			try {
				propertyManager.copy(source, destination, depth);
				propertyManager.deleteProperties(source, depth);
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorPropertiesMove", source.getFullPath().toString(), destination.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
				// log the status but don't return until we try and move the rest of the resource info
				failed(status);
			}

			// Create the destination node in the tree.
			Workspace workspace = (Workspace) source.getWorkspace();
			try {
				workspace.move((Resource) source, destination.getFullPath(), depth, updateFlags, false);
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorMoving", source.getFullPath().toString(), destination.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
				// log the status but don't return until we try and move the rest of the resource info
				failed(status);
			}

			// Generate the marker deltas.
			try {
				workspace.getMarkerManager().moved(source, destination, depth);
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorMarkersDelete", source.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
				failed(status);
			}

			// Copy the local history for this folder
			copyLocalHistory(source, destination);
		} finally {
			lock.release();
		}
	}

	/**
	 * @see IResourceTree#movedProjectSubtree(IProject, IProjectDescription)
	 */
	public boolean movedProjectSubtree(IProject project, IProjectDescription destDescription) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			// Do nothing if the source resource doesn't exist.
			if (!project.exists())
				return true;

			Project source = (Project) project;
			Project destination = (Project) source.getWorkspace().getRoot().getProject(destDescription.getName());
			Workspace workspace = (Workspace) source.getWorkspace();
			int depth = IResource.DEPTH_INFINITE;

			// If the name of the source and destination projects are not the same then 
			// rename the meta area and make changes in the tree.
			if (isNameChange(source, destDescription)) {
				if (destination.exists()) {
					String message = Policy.bind("resources.mustNotExist", destination.getFullPath().toString()); //$NON-NLS-1$
					IStatus status = new ResourceStatus(IStatus.ERROR, destination.getFullPath(), message);
					failed(status);
					return false;
				}

				// Rename the project metadata area. Close the property store to flush everything to disk
				try {
					source.getPropertyManager().closePropertyStore(source);
				} catch (CoreException e) {
					String message = Policy.bind("properties.couldNotClose", source.getFullPath().toString()); //$NON-NLS-1$
					IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
					// log the status but don't return until we try and move the rest of the resource info
					failed(status);
				}
				java.io.File oldMetaArea = workspace.getMetaArea().locationFor(source).toFile();
				java.io.File newMetaArea = workspace.getMetaArea().locationFor(destination).toFile();
				try {
					source.getLocalManager().getStore().move(oldMetaArea, newMetaArea, false, new NullProgressMonitor());
				} catch (CoreException e) {
					String message = Policy.bind("resources.moveMeta", oldMetaArea.toString(), newMetaArea.toString()); //$NON-NLS-1$
					IStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_METADATA, destination.getFullPath(), message, e);
					// log the status but don't return until we try and move the rest of the resource info
					failed(status);
				}

				// Move the workspace tree.
				try {
					workspace.move(source, destination.getFullPath(), depth, updateFlags, true);
				} catch (CoreException e) {
					String message = Policy.bind("resources.errorMoving", source.getFullPath().toString(), destination.getFullPath().toString()); //$NON-NLS-1$
					IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
					// log the status but don't return until we try and move the rest of the resource info
					failed(status);
				}

				// Clear stale state on the destination project.
				ProjectInfo info = (ProjectInfo) destination.getResourceInfo(false, true);
				info.clearNaturesAndBuilders();
				// note that the property store instance will be recreated lazily
				info.setPropertyStore(null);

				// Generate marker deltas.
				try {
					workspace.getMarkerManager().moved(source, destination, depth);
				} catch (CoreException e) {
					String message = Policy.bind("resources.errorMarkersMove", source.getFullPath().toString(), destination.getFullPath().toString()); //$NON-NLS-1$
					IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
					// log the status but don't return until we try and move the rest of the resource info
					failed(status);
				}
				// Copy the local history
				copyLocalHistory(source, destination);
			}

			// Write the new project description on the destination project.
			try {
				//moving linked resources may have modified the description in memory
				((ProjectDescription) destDescription).setLinkDescriptions(destination.internalGetDescription().getLinks());
				destination.internalSetDescription(destDescription, true);
				destination.writeDescription(IResource.FORCE);
			} catch (CoreException e) {
				String message = Policy.bind("resources.projectDesc"); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, destination.getFullPath(), message, e);
				failed(status);
			}

			// write the private project description, including the project location
			try {
				workspace.getMetaArea().writePrivateDescription(destination);
			} catch (CoreException e) {
				failed(e.getStatus());
			}

			// Do a refresh on the destination project to pick up any newly discovered resources
			try {
				destination.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorRefresh", destination.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, destination.getFullPath(), message, e);
				failed(status);
				return false;
			}
			return true;
		} finally {
			lock.release();
		}
	}

	/**
	 * Returns the status object held onto by this resource tree.
	 */
	protected IStatus getStatus() {
		return multistatus;
	}

	/**
	 * @see IResourceTree#getTimestamp(IFile)
	 */
	public long getTimestamp(IFile file) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			if (!file.exists())
				return NULL_TIMESTAMP;
			ResourceInfo info = ((File) file).getResourceInfo(false, false);
			return info == null ? NULL_TIMESTAMP : info.getLocalSyncInfo();
		} finally {
			lock.release();
		}
	}

	/**
	 * @see IResourceTree#deletedFile(IFile)
	 */
	public void deletedFile(IFile file) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			// Do nothing if the resource doesn't exist.
			if (!file.exists())
				return;
			try {
				// Delete properties, generate marker deltas, and remove the node from the workspace tree.
				((Resource) file).deleteResource(true, null);
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorDeleting", file.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, file.getFullPath(), message, e);
				failed(status);
			}
		} finally {
			lock.release();
		}
	}

	/**
	 * @see IResourceTree#deletedFolder(IFolder)
	 */
	public void deletedFolder(IFolder folder) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			// Do nothing if the resource doesn't exist.
			if (!folder.exists())
				return;
			try {
				// Delete properties, generate marker deltas, and remove the node from the workspace tree.
				((Resource) folder).deleteResource(true, null);
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorDeleting", folder.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, folder.getFullPath(), message, e);
				failed(status);
			}
		} finally {
			lock.release();
		}
	}

	/**
	 * @see IResourceTree#deletedProject(IProject)
	 */
	public void deletedProject(IProject target) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			// Do nothing if the resource doesn't exist.
			if (!target.exists())
				return;
			Project project = (Project) target;
			Workspace workspace = (Workspace) project.getWorkspace();

			// Delete properties, generate marker deltas, and remove the node from the workspace tree.
			try {
				project.deleteResource(false, null);
			} catch (CoreException e) {
				String message = Policy.bind("resources.errorDeleting", project.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, project.getFullPath(), message, e);
				// log the status but don't return until we try and delete the rest of the project info
				failed(status);
			}

			// Delete the project metadata.
			try {
				workspace.getMetaArea().delete(project);
			} catch (CoreException e) {
				String message = Policy.bind("resources.deleteMeta", project.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_METADATA, project.getFullPath(), message, e);
				// log the status but don't return until we try and delete the rest of the project info
				failed(status);
			}

			// Clear the history store.
			try {
				project.clearHistory(null);
			} catch (CoreException e) {
				String message = Policy.bind("history.problemsRemoving", project.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, project.getFullPath(), message, e);
				failed(status);
			}
		} finally {
			lock.release();
		}
	}

	/**
	 * This operation has failed for the given reason. Add it to this
	 * resource tree's status.
	 */
	public void failed(IStatus reason) {
		Assert.isLegal(isValid);
		multistatus.add(reason);
	}

	/**
	 * Return <code>true</code> if there is a change in the name of the project.
	 */
	private boolean isNameChange(IProject project, IProjectDescription description) {
		return !project.getName().equals(description.getName());
	}

	/**
	 * Return <code>true</code> if there is a change in the content area for the project.
	 */
	private boolean isContentChange(IProject project, IProjectDescription destinationDescription) {
		IProjectDescription sourceDescription = ((Project) project).internalGetDescription();
		if (sourceDescription.getLocation() == null || destinationDescription.getLocation() == null)
			return true;
		return !sourceDescription.getLocation().equals(destinationDescription.getLocation());
	}

	/**
	 * @see IResourceTree#isSynchronized(IResource, int)
	 */
	public boolean isSynchronized(IResource resource, int depth) {
		try {
			lock.acquire();
			return ((Resource) resource).getLocalManager().isSynchronized(resource, depth);
		} finally {
			lock.release();
		}
	}

	/**
	 * @see IResourceTree#computeTimestamp(IFile)
	 */
	public long computeTimestamp(IFile file) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			if (!file.getProject().exists())
				return NULL_TIMESTAMP;
			return internalComputeTimestamp(file.getLocation().toOSString());
		} finally {
			lock.release();
		}
	}

	/**
	 * Return the timestamp of the file at the given location.
	 */
	protected long internalComputeTimestamp(String location) {
		return CoreFileSystemLibrary.getLastModified(location);
	}

	/**
	 * @see IResourceTree#standardDeleteFile(IFile, int, IProgressMonitor)
	 */
	public void standardDeleteFile(IFile file, int updateFlags, IProgressMonitor monitor) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			internalDeleteFile(file, updateFlags, monitor);
		} finally {
			lock.release();
		}
	}

	/**
	 * Helper method for #standardDeleteFile. Returns a boolean indicating whether or
	 * not the delete was successful. 
	 */
	private boolean internalDeleteFile(IFile file, int updateFlags, IProgressMonitor monitor) {
		try {
			String message = Policy.bind("resources.deleting", file.getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(message, Policy.totalWork);

			// Do nothing if the file doesn't exist in the workspace.
			if (!file.exists()) {
				// Indicate that the delete was successful.
				return true;
			}
			// Don't delete contents if this is a linked resource
			if (file.isLinked()) {
				deletedFile(file);
				return true;
			}
			// If the file doesn't exist on disk then signal to the workspace to delete the
			// file and return.
			java.io.File fileOnDisk = file.getLocation().toFile();
			if (!fileOnDisk.exists()) {
				deletedFile(file);
				// Indicate that the delete was successful.
				return true;
			}

			boolean keepHistory = (updateFlags & IResource.KEEP_HISTORY) != 0;
			boolean force = (updateFlags & IResource.FORCE) != 0;

			// Add the file to the local history if requested by the user.
			if (keepHistory)
				addToLocalHistory(file);
			monitor.worked(Policy.totalWork / 4);

			// We want to fail if force is false and the file is not synchronized with the 
			// local file system.
			if (!force) {
				boolean inSync = isSynchronized(file, IResource.DEPTH_ZERO);
				// only want to fail if the file still exists.
				if (!inSync && file.getLocation().toFile().exists()) {
					message = Policy.bind("localstore.resourceIsOutOfSync", file.getFullPath().toString()); //$NON-NLS-1$
					IStatus status = new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, file.getFullPath(), message);
					failed(status);
					// Indicate that the delete was unsuccessful.
					return false;
				}
			}
			monitor.worked(Policy.totalWork / 4);

			// Try to delete the file from the file system.
			boolean success = fileOnDisk.delete();
			monitor.worked(Policy.totalWork / 4);

			// If the file was successfully deleted from the file system the
			// workspace tree should be updated accordingly. Otherwise
			// we need to signal that a problem occurred.
			if (success) {
				deletedFile(file);
				// Indicate that the delete was successful.
				return true;
			}
			message = Policy.bind("resources.couldnotDelete", file.getLocation().toOSString()); //$NON-NLS-1$
			IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, file.getFullPath(), message);
			failed(status);
			// Indicate that the delete was unsuccessful.
			return false;
		} finally {
			monitor.done();
		}
	}

	/**
	 * @see IResourceTree#standardDeleteFolder(IFolder, int, IProgressMonitor)
	 */
	public void standardDeleteFolder(IFolder folder, int updateFlags, IProgressMonitor monitor) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			String message = Policy.bind("resources.deleting", folder.getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(message, Policy.totalWork);

			// Do nothing if the folder doesn't exist in the workspace.
			if (!folder.exists())
				return;

			// Don't delete contents if this is a linked resource
			if (folder.isLinked()) {
				deletedFolder(folder);
				return;
			}

			// If the folder doesn't exist on disk then update the tree and return.
			java.io.File folderOnDisk = folder.getLocation().toFile();
			if (!folderOnDisk.exists()) {
				deletedFolder(folder);
				return;
			}

			// Check to see if we are synchronized with the local file system. If we are in sync then
			// we can short circuit this operation and delete all the files on disk, otherwise we have
			// to recursively try and delete them doing best-effort, thus leaving only the ones which
			// were out of sync.
			boolean force = (updateFlags & IResource.FORCE) != 0;
			if (!force && !isSynchronized(folder, IResource.DEPTH_INFINITE)) {
				// we are not in sync and force is false so delete via best effort
				internalDeleteFolder(folder, updateFlags, monitor);
				return;
			}

			// Add the contents of the files to the local history if so requested by the user.
			boolean keepHistory = (updateFlags & IResource.KEEP_HISTORY) != 0;
			if (keepHistory)
				addToLocalHistory(folder, IResource.DEPTH_INFINITE);

			boolean success;
			try {
				FileSystemResourceManager localManager = ((Folder) folder).getLocalManager();
				localManager.delete(folder, force, true, false, monitor);
				java.io.File folderLocation = folder.getLocation().toFile();
				success = Workspace.clear(folderLocation);
			} catch (CoreException ce) {
				message = Policy.bind("localstore.couldnotDelete", folder.getFullPath().toString()); //$NON-NLS-1$					
				MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_DELETE_LOCAL, message, ce);
				if (ce.getStatus() != null)
					status.merge(ce.getStatus());
				failed(status);
				return;
			}

			// If the folder was successfully deleted from the file system the
			// workspace tree should be updated accordingly. Otherwise
			// we need to signal that a problem occurred.
			if (success) {
				deletedFolder(folder);
			} else {
				message = Policy.bind("resources.couldnotDelete", folder.getLocation().toOSString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, folder.getFullPath(), message);
				failed(status);
			}
		} finally {
			lock.release();
			monitor.done();
		}
	}

	/**
	 * Add this resource and all child files to the local history. Only adds content for
	 * resources of type <code>IResource.FILE</code>.
	 */
	private void addToLocalHistory(IResource root, int depth) {
		IResourceVisitor visitor = new IResourceVisitor() {
			public boolean visit(IResource resource) {
				if (resource.getType() == IResource.FILE)
					addToLocalHistory((IFile) resource);
				return true;
			}
		};
		try {
			root.accept(visitor, depth, false);
		} catch (CoreException e) {
			// We want to ignore any exceptions thrown by the history store because
			// they aren't enough to fail the operation as a whole. 
		}
	}

	/**
	 * Helper method for #standardDeleteFolder. Returns a boolean indicating 
	 * whether or not the deletion of this folder was successful. Does a best effort
	 * delete of this resource and its children.
	 */
	private boolean internalDeleteFolder(IFolder folder, int updateFlags, IProgressMonitor monitor) {

		// Recursively delete each member of the folder.
		IResource[] members = null;
		try {
			members = folder.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
		} catch (CoreException e) {
			String message = Policy.bind("resources.errorMembers", folder.getFullPath().toString()); //$NON-NLS-1$
			IStatus status = new ResourceStatus(IStatus.ERROR, folder.getFullPath(), message, e);
			failed(status);
			// Indicate that the delete was unsuccessful.
			return false;
		}
		boolean deletedChildren = true;
		for (int i = 0; i < members.length; i++) {
			IResource child = members[i];
			switch (child.getType()) {
				case IResource.FILE :
					deletedChildren &= internalDeleteFile((IFile) child, updateFlags, Policy.subMonitorFor(monitor, Policy.totalWork / members.length));
					break;
				case IResource.FOLDER :
					deletedChildren &= internalDeleteFolder((IFolder) child, updateFlags, Policy.subMonitorFor(monitor, Policy.totalWork / members.length));
					break;
			}
		}
		// Check to see if the children were deleted ok. If there was a problem
		// just return as the problem should have been logged by the recursive
		// call to the child.
		if (!deletedChildren) {
			// Indicate that the delete was unsuccessful.
			return false;
		}

		// Try to delete the folder from the local file system. This will fail
		// if the folder is not empty. No need to check the force flag since this is
		// an internal method and force is always false when we are here.
		java.io.File folderOnDisk = folder.getLocation().toFile();
		boolean success = folderOnDisk.delete();
		if (!success && !folderOnDisk.exists()) {
			// Indicate that the delete was successful.
			success = true;
		}

		if (success) {
			deletedFolder(folder);
			// Indicate that the delete was successful.
			return true;
		}
		String message = Policy.bind("resources.couldnotDelete", folder.getLocation().toOSString()); //$NON-NLS-1$
		IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, folder.getFullPath(), message);
		failed(status);
		// Indicate that the delete was unsuccessful.
		return false;
	}

	/**
	 * @see IResourceTree#standardDeleteProject(IProject, int, IProgressMonitor)
	 */
	public void standardDeleteProject(IProject project, int updateFlags, IProgressMonitor monitor) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			String message = Policy.bind("resources.deleting", project.getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(message, Policy.totalWork);
			// Do nothing if the project doesn't exist in the workspace tree.
			if (!project.exists())
				return;

			boolean alwaysDeleteContent = (updateFlags & IResource.ALWAYS_DELETE_PROJECT_CONTENT) != 0;
			// don't take force into account if we are always deleting the content
			boolean force = alwaysDeleteContent ? true : (updateFlags & IResource.FORCE) != 0;
			boolean neverDeleteContent = (updateFlags & IResource.NEVER_DELETE_PROJECT_CONTENT) != 0;
			boolean success = true;

			// Delete project content.  Don't do anything if the user specified explicitly asked
			// not to delete the project content or if the project is closed and
			// ALWAYS_DELETE_PROJECT_CONTENT was not specified.
			if (alwaysDeleteContent || (project.isOpen() && !neverDeleteContent)) {
				// Check to see if we are synchronized with the local file system. If we are in sync then
				// we can short circuit this operation and delete all the files on disk, otherwise we have
				// to recursively try and delete them doing best-effort, thus leaving only the ones which
				// were out of sync.
				if (!force && !isSynchronized(project, IResource.DEPTH_INFINITE)) {
					// we are not in sync and force is false so delete via best effort
					success = internalDeleteProject(project, updateFlags, monitor);
					if (success) {
						deletedProject(project);
					} else {
						message = Policy.bind("resources.couldnotDelete", project.getLocation().toOSString()); //$NON-NLS-1$
						IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, project.getFullPath(), message);
						failed(status);
					}
					return;
				}

				// If the content area is in the default location then delete the directory and all its
				// children. If it is specified by the user then leave the directory itself but delete the children.
				IProjectDescription description = ((Project) project).internalGetDescription();
				boolean defaultLocation = description == null || description.getLocation() == null;
				java.io.File projectLocation = project.getLocation().toFile();
				// if the project is open, we must perform a best-effort deletion			
				if (project.isOpen()) {
					try {
						FileSystemResourceManager localManager = ((Project) project).getLocalManager();
						localManager.delete(project, force, false, false, Policy.subMonitorFor(monitor, Policy.totalWork * 3 / 4));
						// if the project location is not the default we want to keep the root directory (it is empty now)
						if (defaultLocation)
							success = Workspace.clear(projectLocation);
					} catch (CoreException ce) {
						message = Policy.bind("localstore.couldnotDelete", project.getFullPath().toString()); //$NON-NLS-1$					
						MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_DELETE_LOCAL, message, ce);
						if (ce.getStatus() != null)
							status.merge(ce.getStatus());
						failed(status);
						return;
					}
				} else {
					success = defaultLocation ? Workspace.clear(projectLocation) : Workspace.clearChildren(projectLocation);
					monitor.worked(Policy.totalWork * 3 / 4);
				}
			}

			// Signal that the workspace tree should be updated that the project
			// has been deleted.
			if (success)
				deletedProject(project);
			else {
				message = Policy.bind("localstore.couldnotDelete", project.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, project.getFullPath(), message);
				failed(status);
			}
		} finally {
			lock.release();
			monitor.done();
		}
	}

	/**
	 * Helper method for moving the project content. Determines the content location
	 * based on the project description. (default location or user defined?)
	 */
	private void moveProjectContent(IProject source, IProjectDescription destDescription, int updateFlags, IProgressMonitor monitor) throws CoreException {
		try {
			String message = Policy.bind("resources.moving", source.getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(message, 10);
			IProjectDescription srcDescription = source.getDescription();
			// If the locations are the same (and non-default) then there is nothing to do.
			if (srcDescription.getLocation() != null && srcDescription.getLocation().equals(destDescription))
				return;

			IPath srcLocation = source.getLocation();
			IPath destLocation = destDescription.getLocation();

			// Use the default area if necessary for the destination. The source project
			// should already have a location assigned to it.
			if (destLocation == null)
				destLocation = Platform.getLocation().append(destDescription.getName());

			java.io.File destinationFile = destLocation.toFile();
			// Move the contents on disk.
			try {
				moveInFileSystem(srcLocation.toFile(), destinationFile, updateFlags, monitor);
			} catch (CoreException ce) {
				// did the fail occur after copying to the destination?
				boolean failedDeletingSource = ce instanceof ResourceException && ce.getStatus().getCode() == IResourceStatus.FAILED_DELETE_LOCAL && destinationFile.exists();
				// if not, then rethrow the exception to abort the move operation
				if (!failedDeletingSource)
					throw ce;
				// if so, we should log the failure and proceed with moving the tree
				failed(ce.getStatus());
			}
			monitor.worked(9);

			//if this is a deep move, move the contents of any linked resources
			if ((updateFlags & IResource.SHALLOW) == 0) {
				IResource[] children = source.members();
				for (int i = 0; i < children.length; i++) {
					if (children[i].isLinked()) {
						message = Policy.bind("resources.moving", children[i].getFullPath().toString()); //$NON-NLS-1$
						monitor.subTask(message);
						java.io.File sourceFile = children[i].getLocation().toFile();
						java.io.File destFile = destLocation.append(children[i].getName()).toFile();
						try {
							moveInFileSystem(sourceFile, destFile, updateFlags, Policy.monitorFor(null));
						} catch (CoreException ce) {
							//log the failure, but keep trying on remaining links
							failed(ce.getStatus());
						}
					}
				}
			}
			monitor.worked(1);
		} finally {
			monitor.done();
		}
	}

	/**
	 * @see IResourceTree#standardMoveFile(IFile, IFile, int, IProgressMonitor)
	 */
	public void standardMoveFile(IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			String message = Policy.bind("resources.moving", source.getFullPath().toString()); //$NON-NLS-1$
			monitor.subTask(message);

			// These pre-conditions should all be ok but just in case...
			if (!source.exists() || destination.exists() || !destination.getParent().isAccessible())
				throw new IllegalArgumentException();

			boolean force = (updateFlags & IResource.FORCE) != 0;
			boolean keepHistory = (updateFlags & IResource.KEEP_HISTORY) != 0;
			boolean isDeep = (updateFlags & IResource.SHALLOW) == 0;

			// If the file is not in sync with the local file system and force is false,
			// then signal that we have an error.
			if (!force && !isSynchronized(source, IResource.DEPTH_INFINITE)) {
				message = Policy.bind("localstore.resourceIsOutOfSync", source.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, source.getFullPath(), message);
				failed(status);
				return;
			}
			monitor.worked(Policy.totalWork / 4);

			// Add the file contents to the local history if requested by the user.	
			if (keepHistory)
				addToLocalHistory(source);
			monitor.worked(Policy.totalWork / 4);

			//for shallow move of linked resources, nothing needs to be moved in the file system
			if (!isDeep && source.isLinked()) {
				movedFile(source, destination);
				return;
			}

			java.io.File sourceFile = source.getLocation().toFile();
			java.io.File destFile = destination.getLocation().toFile();
			// If the file was successfully moved in the file system then the workspace
			// tree needs to be updated accordingly. Otherwise signal that we have an error.
			try {
				moveInFileSystem(sourceFile, destFile, updateFlags, monitor);
			} catch (CoreException e) {
				failed(e.getStatus());
				// did the fail occur after copying to the destination?									
				boolean failedDeletingSource = e instanceof ResourceException && e.getStatus().getCode() == IResourceStatus.FAILED_DELETE_LOCAL && destFile.exists();
				// if so, we should proceed
				if (!failedDeletingSource)
					return;
			}
			movedFile(source, destination);
			updateMovedFileTimestamp(destination, computeTimestamp(destination));
			monitor.worked(Policy.totalWork / 4);
			return;
		} finally {
			lock.release();
			monitor.done();
		}
	}

	/**
	 * @see IResourceTree#standardMoveFolder(IFolder, IFolder, int, IProgressMonitor)
	 */
	public void standardMoveFolder(IFolder source, IFolder destination, int updateFlags, IProgressMonitor monitor) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			String message = Policy.bind("resources.moving", source.getFullPath().toString()); //$NON-NLS-1$
			monitor.subTask(message);

			// These pre-conditions should all be ok but just in case...
			if (!source.exists() || destination.exists() || !destination.getParent().isAccessible())
				throw new IllegalArgumentException();

			// Check to see if we are synchronized with the local file system. If we are in sync then we can
			// short circuit this method and do a file system only move. Otherwise we have to recursively
			// try and move all resources, doing it in a best-effort manner.
			boolean force = (updateFlags & IResource.FORCE) != 0;
			if (!force && !isSynchronized(source, IResource.DEPTH_INFINITE)) {
				message = Policy.bind("localstore.resourceIsOutOfSync", source.getFullPath().toString());//$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message);
				failed(status);
				return;
			}

			// keep history
			boolean keepHistory = (updateFlags & IResource.KEEP_HISTORY) != 0;
			if (keepHistory)
				addToLocalHistory(source, IResource.DEPTH_INFINITE);

			//for linked resources, nothing needs to be moved in the file system
			boolean isDeep = (updateFlags & IResource.SHALLOW) == 0;
			if (!isDeep && source.isLinked()) {
				movedFolderSubtree(source, destination);
				return;
			}

			// Move the resources in the file system. Only the FORCE flag is valid here so don't
			// have to worry about clearing the KEEP_HISTORY flag.
			java.io.File sourceFile = source.getLocation().toFile();
			java.io.File destinationFile = destination.getLocation().toFile();
			try {
				moveInFileSystem(sourceFile, destinationFile, updateFlags, monitor);
			} catch (CoreException e) {
				failed(e.getStatus());
				// did the fail occur after copying to the destination?
				boolean failedDeletingSource = e instanceof ResourceException && e.getStatus().getCode() == IResourceStatus.FAILED_DELETE_LOCAL && destinationFile.exists();
				// if so, we should proceed
				if (!failedDeletingSource)
					return;
			}
			boolean success = destinationFile.exists();
			if (success) {
				movedFolderSubtree(source, destination);
				updateTimestamps(destination, isDeep);
			} else {
				message = Policy.bind("localstore.couldNotCreateFolder", destination.getLocation().toOSString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_LOCAL, destination.getFullPath(), message);
				failed(status);
			}
		} finally {
			lock.release();
			monitor.done();
		}
	}

	/**
	 * Helper method to update all the timestamps in the tree to match
	 * those in the file system. Used after a #move.
	 */
	private void updateTimestamps(IResource root, final boolean isDeep) {
		IResourceVisitor visitor = new IResourceVisitor() {
			public boolean visit(IResource resource) {
				boolean isLinked = resource.isLinked();
				if (isLinked && !isDeep)
					//don't need to visit children because they didn't move
					return false;
				if (resource.getType() == IResource.FILE) {
					IFile file = (IFile) resource;
					updateMovedFileTimestamp(file, computeTimestamp(file));
				} else {
					if (isLinked) {
						//clear the linked resource bit, if any
						ResourceInfo info = ((Resource) resource).getResourceInfo(false, true);
						info.clear(ICoreConstants.M_LINK);
					}
				}
				return true;
			}
		};
		try {
			root.accept(visitor, IResource.DEPTH_INFINITE, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
		} catch (CoreException e) {
			// No exception should be thrown.
		}
	}

	/**
	 * Does a best-effort delete on this resource and all its children.
	 */
	private boolean internalDeleteProject(IProject project, int updateFlags, IProgressMonitor monitor) {

		// Recursively delete each member of the project.
		IResource[] members = null;
		try {
			members = project.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
		} catch (CoreException e) {
			String message = Policy.bind("resources.errorMembers", project.getFullPath().toString()); //$NON-NLS-1$
			IStatus status = new ResourceStatus(IStatus.ERROR, project.getFullPath(), message, e);
			failed(status);
			// Indicate that the delete was unsuccessful.
			return false;
		}
		boolean deletedChildren = true;
		for (int i = 0; i < members.length; i++) {
			IResource child = members[i];
			switch (child.getType()) {
				case IResource.FILE :
					if (child.getName().equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
						// ignore the .project file for now and delete it last
					} else {
						deletedChildren &= internalDeleteFile((IFile) child, updateFlags, Policy.subMonitorFor(monitor, Policy.totalWork / members.length));
					}
					break;
				case IResource.FOLDER :
					deletedChildren &= internalDeleteFolder((IFolder) child, updateFlags, Policy.subMonitorFor(monitor, Policy.totalWork / members.length));
					break;
			}
		}
		// Check to see if the children were deleted ok. If there was a problem
		// just return as the problem should have been logged by the recursive
		// call to the child.
		if (deletedChildren) {
			IResource file = project.findMember(IProjectDescription.DESCRIPTION_FILE_NAME);
			if (file == null) {
				//the .project have may have been recreated on disk automatically by snapshot
				IPath location = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME).getLocation();
				if (location != null)
					location.toFile().delete();
			} else {
				if (file.getType() != IResource.FILE) {
					// We should never get here since the only reason we skipped it above was because
					// it was a file named .project.
					String message = Policy.bind("resources.couldnotDelete", file.getFullPath().toString()); //$NON-NLS-1$
					IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, file.getFullPath(), message);
					failed(status);
					// Indicate that the delete was unsuccessful.
					return false;
				}
				boolean deletedProjectFile = internalDeleteFile((IFile) file, updateFlags, Policy.monitorFor(null));
				if (!deletedProjectFile) {
					String message = Policy.bind("resources.couldnotDelete", file.getFullPath().toString()); //$NON-NLS-1$
					IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, file.getFullPath(), message);
					failed(status);
					// Indicate that the delete was unsuccessful.
					return false;
				}
			}
		} else {
			// Indicate that the delete was unsuccessful.
			return false;
		}

		// If the content area is in the default location then delete the directory and all its
		// children. If it is specified by the user then leave the directory itself but delete the children.
		// No need to check the force flag since this is an internal method and by the time we
		// get here we know that force is false.
		java.io.File root = project.getLocation().toFile();
		IProjectDescription description = ((Project) project).internalGetDescription();
		// If we have a user-defined location delete the directory, otherwise just see if its empty
		boolean success;
		if (description == null || description.getLocation() == null) {
			success = root.delete();
			if (!success && !root.exists()) {
				success = true;
			}
		} else {
			String[] children = root.list();
			success = children == null || children.length == 0;
		}

		if (success) {
			deletedProject(project);
			// Indicate that the delete was successful.
			return true;
		}
		String message = Policy.bind("resources.couldnotDelete", project.getLocation().toOSString()); //$NON-NLS-1$
		IStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, project.getFullPath(), message);
		failed(status);
		// Indicate that the delete was unsuccessful.
		return false;
	}

	/**
	 * @see IResourceTree#standardMoveProject(IProject, IProjectDescription, int, IProgressMonitor)
	 */
	public void standardMoveProject(IProject source, IProjectDescription description, int updateFlags, IProgressMonitor monitor) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			String message = Policy.bind("resources.moving", source.getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(message, Policy.totalWork);

			// Double-check this pre-condition.
			if (!source.isAccessible())
				throw new IllegalArgumentException();

			// If there is nothing to do on disk then signal to make the workspace tree
			// changes.
			if (!isContentChange(source, description)) {
				movedProjectSubtree(source, description);
				return;
			}

			// Check to see if we are synchronized with the local file system. 
			boolean force = (updateFlags & IResource.FORCE) != 0;
			if (!force && !isSynchronized(source, IResource.DEPTH_INFINITE)) {
				// FIXME: make this a best effort move?
				message = Policy.bind("localstore.resourceIsOutOfSync", source.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IResourceStatus.OUT_OF_SYNC_LOCAL, source.getFullPath(), message);
				failed(status);
				return;
			}

			// Move the project content in the local file system.
			try {
				moveProjectContent(source, description, updateFlags, Policy.subMonitorFor(monitor, Policy.totalWork * 3 / 4));
			} catch (CoreException e) {
				message = Policy.bind("localstore.couldNotMove", source.getFullPath().toString()); //$NON-NLS-1$
				IStatus status = new ResourceStatus(IStatus.ERROR, source.getFullPath(), message, e);
				failed(status);
				return;
			}

			// If we got this far the project content has been moved on disk (if necessary)
			// and we need to update the workspace tree.
			movedProjectSubtree(source, description);
			monitor.worked(Policy.totalWork * 1 / 8);
			boolean isDeep = (updateFlags & IResource.SHALLOW) == 0;
			updateTimestamps(source.getWorkspace().getRoot().getProject(description.getName()), isDeep);
			monitor.worked(Policy.totalWork * 1 / 8);
		} finally {
			lock.release();
			monitor.done();
		}
	}

	/**
	 * Move the contents of the specified file from the source location to the destination location.
	 * If the source points to a directory then move that directory and all its contents.
	 * 
	 * <code>IResource.FORCE</code> is the only valid flag.
	 */
	private void moveInFileSystem(java.io.File source, java.io.File destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		Assert.isLegal(isValid);
		FileSystemStore store = ((Resource) ResourcesPlugin.getWorkspace().getRoot()).getLocalManager().getStore();
		boolean force = (updateFlags & IResource.FORCE) != 0;
		store.move(source, destination, force, monitor);
	}

	/**
	 * @see IResourceTree#updateMovedFileTimestamp(IFile, long)
	 */
	public void updateMovedFileTimestamp(IFile file, long timestamp) {
		Assert.isLegal(isValid);
		try {
			lock.acquire();
			// Do nothing if the file doesn't exist in the workspace tree.
			if (!file.exists())
				return;
			// Update the timestamp in the tree.
			ResourceInfo info = ((Resource) file).getResourceInfo(false, true);
			// The info should never be null since we just checked that the resource exists in the tree.
			((Resource) file).getLocalManager().updateLocalSync(info, timestamp);
			//remove the linked bit since this resource has been moved in the file system
			info.clear(ICoreConstants.M_LINK);
		} finally {
			lock.release();
		}
	}
}