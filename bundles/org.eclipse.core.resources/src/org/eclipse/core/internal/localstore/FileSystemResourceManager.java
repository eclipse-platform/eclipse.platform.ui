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

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Manages the synchronization between the workspace's view and the file system.  
 */
public class FileSystemResourceManager implements ICoreConstants, IManager {

	protected Workspace workspace;
	protected IHistoryStore historyStore;
	protected FileSystemStore localStore;

	public FileSystemResourceManager(Workspace workspace) {
		this.workspace = workspace;
		localStore = new FileSystemStore();
	}

	/**
	 * Returns the workspace paths of all resources that may correspond to
	 * the given file system location.  Returns an empty ArrayList if there are no 
	 * such paths.  This method does not consider whether resources actually 
	 * exist at the given locations.
	 */
	protected ArrayList allPathsForLocation(IPath location) {
		IProject[] projects = getWorkspace().getRoot().getProjects();
		final ArrayList results = new ArrayList();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			//check the project location
			IPath testLocation = project.getLocation();
			IPath suffix;
			if (testLocation != null && testLocation.isPrefixOf(location)) {
				suffix = location.removeFirstSegments(testLocation.segmentCount());
				results.add(project.getFullPath().append(suffix));
			}
			if (!project.isAccessible())
				continue;
			IResource[] children = null;
			try {
				children = project.members();
			} catch (CoreException e) {
				//ignore projects that cannot be accessed
			}
			if (children == null)
				continue;
			for (int j = 0; j < children.length; j++) {
				IResource child = children[j];
				if (child.isLinked()) {
					testLocation = child.getLocation();
					if (testLocation != null && testLocation.isPrefixOf(location)) {
						//add the full workspace path of the corresponding child of the linked resource
						suffix = location.removeFirstSegments(testLocation.segmentCount());
						results.add(child.getFullPath().append(suffix));
					}
				}
			}
		}
		return results;
	}

	/**
	 * Returns all resources that correspond to the given file system location,
	 * including resources under linked resources.  Returns an empty array
	 * if there are no corresponding resources.
	 * @param location the file system location
	 * @param files resources that may exist below the project level can
	 * be either files or folders.  If this parameter is true, files will be returned,
	 * otherwise containers will be returned.
	 */
	public IResource[] allResourcesFor(IPath location, boolean files) {
		ArrayList result = allPathsForLocation(location);
		int count = 0;
		for (int i = 0, imax = result.size(); i < imax; i++) {
			//replace the path in the list with the appropriate resource type
			IResource resource = resourceFor((IPath) result.get(i), files);
			result.set(i, resource);
			//count actual resources - some paths won't have a corresponding resource
			if (resource != null)
				count++;
		}
		//convert to array and remove null elements
		IResource[] toReturn = files ? (IResource[]) new IFile[count] : (IResource[]) new IContainer[count];
		count = 0;
		for (Iterator it = result.iterator(); it.hasNext();) {
			IResource resource = (IResource) it.next();
			if (resource != null)
				toReturn[count++] = resource;
		}
		return toReturn;
	}

	/**
	 * Returns a container for the given file system location or null if there
	 * is no mapping for this path. If the path has only one segment, then an 
	 * <code>IProject</code> is returned.  Otherwise, the returned object
	 * is a <code>IFolder</code>.  This method does NOT check the existence
	 * of a folder in the given location. Location cannot be null.
	 */
	public IContainer containerForLocation(IPath location) {
		IPath path = pathForLocation(location);
		return path == null ? null : (IContainer) resourceFor(path, false);
	}

	public void copy(IResource target, IResource destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			int totalWork = ((Resource) target).countResources(IResource.DEPTH_INFINITE, false);
			String title = Policy.bind("localstore.copying", target.getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(title, totalWork);
			// use locationFor() instead of getLocation() to avoid null 
			IPath location = locationFor(destination);
			if (location == null) {
				String message = Policy.bind("localstore.locationUndefined", target.getFullPath().toString()); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
			}
			if (location.toFile().exists()) {
				String message = Policy.bind("localstore.resourceExists", destination.getFullPath().toString()); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, destination.getFullPath(), message, null);
			}
			CopyVisitor visitor = new CopyVisitor(target, destination, updateFlags, monitor);
			UnifiedTree tree = new UnifiedTree(target);
			tree.accept(visitor, IResource.DEPTH_INFINITE);
			IStatus status = visitor.getStatus();
			if (!status.isOK())
				throw new ResourceException(status);
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Factory method for creating history stores. 
	 */
	private static IHistoryStore createHistoryStore(Workspace workspace, IPath location, int limit) {
		return new HistoryStore(workspace, location, limit);
	}	

	public void delete(IResource target, boolean force, boolean convertToPhantom, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			Resource resource = (Resource) target;
			int totalWork = resource.countResources(IResource.DEPTH_INFINITE, false);
			totalWork *= 2;
			String title = Policy.bind("localstore.deleting", resource.getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(title, totalWork);
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_DELETE_LOCAL, Policy.bind("localstore.deleteProblem"), null); //$NON-NLS-1$
			List skipList = null;
			UnifiedTree tree = new UnifiedTree(target);
			if (!force) {
				IProgressMonitor sub = Policy.subMonitorFor(monitor, totalWork / 2);
				sub.beginTask("", 10000); //$NON-NLS-1$
				CollectSyncStatusVisitor refreshVisitor = new CollectSyncStatusVisitor(Policy.bind("localstore.deleteProblem"), sub); //$NON-NLS-1$
				tree.accept(refreshVisitor, IResource.DEPTH_INFINITE);
				status.merge(refreshVisitor.getSyncStatus());
				skipList = refreshVisitor.getAffectedResources();
			}
			DeleteVisitor deleteVisitor = new DeleteVisitor(skipList, force, convertToPhantom, keepHistory, Policy.subMonitorFor(monitor, force ? totalWork : (totalWork / 2)));
			tree.accept(deleteVisitor, IResource.DEPTH_INFINITE);
			status.merge(deleteVisitor.getStatus());
			if (!status.isOK())
				throw new ResourceException(status);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns an IFile for the given file system location or null if there
	 * is no mapping for this path. This method does NOT check the existence
	 * of a file in the given location. Location cannot be null.
	 */
	public IFile fileForLocation(IPath location) {
		IPath path = pathForLocation(location);
		return path == null ? null : (IFile) resourceFor(path, true);
	}

	/**
	 * The project description file is the only metadata file stored outside the
	 * metadata area.  It is stored as a file directly under the project location.
	 * Returns null if the project location could not be resolved.
	 */
	public IPath getDescriptionLocationFor(IProject target) {
		IPath projectLocation = locationFor(target);
		return projectLocation == null ? null : projectLocation.append(IProjectDescription.DESCRIPTION_FILE_NAME);
	}

	/**
	 * @deprecated
	 */
	public int getEncoding(File target) throws CoreException {
		// thread safety: (the location can be null if the project for this file does not exist)
		IPath location = locationFor(target);
		if (location == null)
			((Project) target.getProject()).checkExists(NULL_FLAG, true);
		//location can be null if based on an undefined variable
		if (location == null) {
			String message = Policy.bind("localstore.locationUndefined", target.getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
		}
		java.io.File localFile = location.toFile();
		if (!localFile.exists()) {
			String message = Policy.bind("localstore.fileNotFound", localFile.getAbsolutePath()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
		}
		return getStore().getEncoding(localFile);
	}

	public IHistoryStore getHistoryStore() {
		return historyStore;
	}

	protected IPath getProjectDefaultLocation(IProject project) {
		return Platform.getLocation().append(project.getFullPath());
	}

	public FileSystemStore getStore() {
		return localStore;
	}

	protected Workspace getWorkspace() {
		return workspace;
	}

	public boolean hasSavedProject(IProject project) {
		IPath location = getDescriptionLocationFor(project);
		return location == null ? false : location.toFile().exists();
	}

	/**
	 * The target must exist in the workspace.  This method must only ever
	 * be called from Project.writeDescription(), because that method ensures
	 * that the description isn't then immediately discovered as a new change.
	 * @return true if a new description was written, and false if it wasn't written
	 * because it was unchanged
	 */
	public boolean internalWrite(IProject target, IProjectDescription description, int updateFlags, boolean hasPublicChanges, boolean hasPrivateChanges) throws CoreException {
		IPath location = locationFor(target);
		if (location == null) {
			String message = Policy.bind("localstore.locationUndefined", target.getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, target.getFullPath(), message, null);
		}
		getStore().writeFolder(location.toFile());
		//write the project's private description to the metadata area
		if (hasPrivateChanges)
			getWorkspace().getMetaArea().writePrivateDescription(target);
		if (!hasPublicChanges)
			return false;
		//can't do anything if there's no description
		if (description == null)
			return false;

		//write the model to a byte array
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			new ModelObjectWriter().write(description, out);
		} catch (IOException e) {
			String msg = Policy.bind("resources.writeMeta", target.getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, target.getFullPath(), msg, e);
		}
		byte[] newContents = out.toByteArray();

		//write the contents to the IFile that represents the description
		IFile descriptionFile = target.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		if (!descriptionFile.exists())
			workspace.createResource(descriptionFile, false);
		else {
			//if the description has not changed, don't write anything
			if (!descriptionChanged(descriptionFile, newContents))
				return false;
		}
		ByteArrayInputStream in = new ByteArrayInputStream(newContents);
		if (descriptionFile.isReadOnly()) {
			IStatus result = getWorkspace().validateEdit(new IFile[] {descriptionFile}, null);
			if (!result.isOK())
				throw new ResourceException(result);
		}
		descriptionFile.setContents(in, updateFlags, null);

		//update the timestamp on the project as well so we know when it has
		//been changed from the outside
		long lastModified = ((Resource) descriptionFile).getResourceInfo(false, false).getLocalSyncInfo();
		ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
		updateLocalSync(info, lastModified);

		//for backwards compatibility, ensure the old .prj file is deleted
		getWorkspace().getMetaArea().clearOldDescription(target);
		return true;
	}

	/**
	 * Returns true if the description on disk is different from the given byte array,
	 * and false otherwise.
	 */
	private boolean descriptionChanged(IFile descriptionFile, byte[] newContents) {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(descriptionFile.getContents());
			int newLength = newContents.length;
			byte[] oldContents = new byte[newLength];
			int read = stream.read(oldContents);
			if (read != newLength)
				return true;
			//if the stream still has bytes available, then the description is changed
			if (stream.read() >= 0)
				return true;
			return !Arrays.equals(newContents, oldContents);
		} catch (Exception e) {
			//if we failed to compare, just write the new contents
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (IOException e1) {
				//ignore failure to close the file
			}
		}
		return true;
	}

	/**
	 * Returns true if the given project's description is synchronized with
	 * the project description file on disk, and false otherwise.
	 */
	public boolean isDescriptionSynchronized(IProject target) {
		//sync info is stored on the description file, and on project info.
		//when the file is changed by someone else, the project info modification
		//stamp will be out of date
		IFile descriptionFile = target.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		ResourceInfo projectInfo = ((Resource) target).getResourceInfo(false, false);
		if (projectInfo == null)
			return false;
		return projectInfo.getLocalSyncInfo() == CoreFileSystemLibrary.getLastModified(descriptionFile.getLocation().toOSString());
	}

	/* (non-Javadoc)
	 * Returns true if the given resource is synchronized with the filesystem
	 * to the given depth.  Returns false otherwise.
	 * 
	 * @see IResource#isSynchronized(int)
	 */
	public boolean isSynchronized(IResource target, int depth) {
		switch (target.getType()) {
			case IResource.ROOT :
				if (depth == IResource.DEPTH_ZERO)
					return true;
				//check sync on child projects.
				depth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : depth;
				IProject[] projects = ((IWorkspaceRoot) target).getProjects();
				for (int i = 0; i < projects.length; i++) {
					if (!isSynchronized(projects[i], depth))
						return false;
				}
				return true;
			case IResource.PROJECT :
				if (!target.isAccessible())
					return true;
				break;
			case IResource.FILE :
				if (fastIsSynchronized((File) target))
					return true;
				break;
		}
		IsSynchronizedVisitor visitor = new IsSynchronizedVisitor(Policy.monitorFor(null));
		UnifiedTree tree = new UnifiedTree(target);
		try {
			tree.accept(visitor, depth);
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
			return false;
		} catch (IsSynchronizedVisitor.ResourceChangedException e) {
			//visitor throws an exception if out of sync
			return false;
		}
		return true;
	}

	public void link(Resource target, IPath localLocation) {
		//resource already exists when linking -- just need to update sync info
		long lastModified = 0;
		//a relative path means location is relative to an undefined variable
		if (localLocation.isAbsolute())
			lastModified = CoreFileSystemLibrary.getLastModified(localLocation.toFile().getAbsolutePath());
		ResourceInfo info = target.getResourceInfo(false, true);
		updateLocalSync(info, lastModified);
	}

	/**
	 * Returns the resolved, absolute file system location of the given resource.
	 * Returns null if the location could not be resolved.
	 */
	public IPath locationFor(IResource target) {
		//note: this method is a critical performance path,
		//code may be inlined to prevent method calls
		switch (target.getType()) {
			case IResource.ROOT :
				return Platform.getLocation();
			case IResource.PROJECT :
				Project project = (Project) target;
				ProjectDescription description = project.internalGetDescription();
				if (description != null && description.getLocation() != null) {
					IPath resolved = workspace.getPathVariableManager().resolvePath(description.getLocation());
					//if path is still relative then path variable could not be resolved
					return resolved != null && resolved.isAbsolute() ? resolved : null;
				}
				return getProjectDefaultLocation(project);
			default :
				//check if the resource is a linked resource
				IPath targetPath = target.getFullPath();
				int numSegments = targetPath.segmentCount();
				IResource linked = target;
				if (numSegments > 2) {
					//parent could be a linked resource
					linked = workspace.getRoot().getFolder(targetPath.removeLastSegments(numSegments - 2));
				}
				description = ((Project) target.getProject()).internalGetDescription();
				if (linked.isLinked()) {
					IPath location = description.getLinkLocation(linked.getName());
					//location may have been deleted from the project description between sessions
					if (location != null) {
						location = workspace.getPathVariableManager().resolvePath(location);
						//if path is still relative then path variable could not be resolved
						if (!location.isAbsolute())
							return null;
						return location.append(targetPath.removeFirstSegments(2));
					}
				}
				//not a linked resource -- get location of project
				if (description != null && description.getLocation() != null) {
					IPath resolved = workspace.getPathVariableManager().resolvePath(description.getLocation());
					//if path is still relative then path variable could not be resolved
					if (!resolved.isAbsolute())
						return null;
					return resolved.append(target.getProjectRelativePath());
				}
				return Platform.getLocation().append(target.getFullPath());
		}
	}

	/**
	 * Returns a resource path to the given local location. Returns null if
	 * it is not under a project's location.
	 */
	protected IPath pathForLocation(IPath location) {
		if (Platform.getLocation().equals(location))
			return Path.ROOT;
		IProject[] projects = getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			IPath projectLocation = project.getLocation();
			if (projectLocation != null && projectLocation.isPrefixOf(location)) {
				int segmentsToRemove = projectLocation.segmentCount();
				return project.getFullPath().append(location.removeFirstSegments(segmentsToRemove));
			}
		}
		return null;
	}

	/**
	 * Optimized sync check for files.  Returns true if the file exists and is in sync, and false
	 * otherwise.  The intent is to let the default implementation handle the complex
	 * cases like gender change, case variants, etc.
	 */
	public boolean fastIsSynchronized(File target) {
		ResourceInfo info = target.getResourceInfo(false, false);
		if (target.exists(target.getFlags(info), true)) {
			IPath location = target.getLocation();
			if (location != null) {
				long stat = CoreFileSystemLibrary.getStat(location.toString());
				if (CoreFileSystemLibrary.isFile(stat) && info.getLocalSyncInfo() == CoreFileSystemLibrary.getLastModified(stat))
					return true;
			}
		}
		return false;
	}

	public InputStream read(IFile target, boolean force, IProgressMonitor monitor) throws CoreException {
		// thread safety: (the location can be null if the project for this file does not exist)
		IPath location = locationFor(target);
		if (location == null)
			((Project) target.getProject()).checkExists(NULL_FLAG, true);
		//location can be null if based on an undefined variable
		if (location == null) {
			String message = Policy.bind("localstore.locationUndefined", target.getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
		}
		java.io.File localFile = location.toFile();
		if (!localFile.exists()) {
			String message = Policy.bind("localstore.fileNotFound", localFile.getAbsolutePath()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
		}
		if (!force) {
			ResourceInfo info = ((Resource) target).getResourceInfo(true, false);
			int flags = ((Resource) target).getFlags(info);
			((Resource) target).checkExists(flags, true);
			if (CoreFileSystemLibrary.getLastModified(localFile.getAbsolutePath()) != info.getLocalSyncInfo()) {
				String message = Policy.bind("localstore.resourceIsOutOfSync", target.getFullPath().toString()); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message, null);
			}
		}
		return getStore().read(localFile);
	}

	/**
	 * Reads and returns the project description for the given project.
	 * Never returns null.
	 * @param target the project whose description should be read.
	 * @param creation true if this project is just being created, in which
	 * case the private project information (including the location) needs to be read 
	 * from disk as well.
	 * @exception CoreException if there was any failure to read the project
	 * description, or if the description was missing.
	 */
	public ProjectDescription read(IProject target, boolean creation) throws CoreException {
		//read the project location if this project is being created
		IPath projectLocation = null;
		ProjectDescription privateDescription = null;
		if (creation) {
			privateDescription = new ProjectDescription();
			getWorkspace().getMetaArea().readPrivateDescription(target, privateDescription);
			projectLocation = privateDescription.getLocation();
		} else {
			IProjectDescription description = ((Project) target).internalGetDescription();
			if (description != null && description.getLocation() != null) {
				projectLocation = description.getLocation();
			}
		}
		final boolean isDefaultLocation = projectLocation == null;
		if (isDefaultLocation) {
			projectLocation = getProjectDefaultLocation(target);
		}
		IPath descriptionPath = workspace.getPathVariableManager().resolvePath(projectLocation).append(IProjectDescription.DESCRIPTION_FILE_NAME);
		ProjectDescription description = null;

		if (!descriptionPath.toFile().exists()) {
			//try the legacy location in the meta area
			description = getWorkspace().getMetaArea().readOldDescription(target);
			if (description == null) {
				String msg = Policy.bind("resources.missingProjectMeta", target.getName()); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, null);
			}
			return description;
		}
		//hold onto any exceptions until after sync info is updated, then throw it
		ResourceException error = null;
		try {
			description = new ProjectDescriptionReader().read(descriptionPath);
		} catch (IOException e) {
			String msg = Policy.bind("resources.readProjectMeta", target.getName()); //$NON-NLS-1$
			error = new ResourceException(IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, e);
		}
		if (error == null && description == null) {
			String msg = Policy.bind("resources.readProjectMeta", target.getName()); //$NON-NLS-1$
			error = new ResourceException(IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, null);
		}
		if (description != null) {
			//don't trust the project name in the description file
			description.setName(target.getName());
			if (!isDefaultLocation)
				description.setLocation(projectLocation);
			if (creation && privateDescription != null)
				description.setDynamicReferences(privateDescription.getDynamicReferences(false));
		}
		long lastModified = CoreFileSystemLibrary.getLastModified(descriptionPath.toOSString());
		IFile descriptionFile = target.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
		//don't get a mutable copy because we might be in restore which isn't an operation
		//it doesn't matter anyway because local sync info is not included in deltas
		ResourceInfo info = ((Resource) descriptionFile).getResourceInfo(false, false);
		if (info == null) {
			//create a new resource on the sly -- don't want to start an operation
			info = getWorkspace().createResource(descriptionFile, false);
			updateLocalSync(info, lastModified);
		}
		//if the project description has changed between sessions, let it remain
		//out of sync -- that way link changes will be reconciled on next refresh
		if (!creation)
			updateLocalSync(info, lastModified);

		//update the timestamp on the project as well so we know when it has
		//been changed from the outside
		info = ((Resource) target).getResourceInfo(false, true);
		updateLocalSync(info, lastModified);

		if (error != null)
			throw error;
		return description;
	}

	public boolean refresh(IResource target, int depth, boolean updateAliases, IProgressMonitor monitor) throws CoreException {
		switch (target.getType()) {
			case IResource.ROOT :
				return refreshRoot((IWorkspaceRoot) target, depth, updateAliases, monitor);
			case IResource.PROJECT :
				if (!target.isAccessible())
					return false;
			//fall through
			case IResource.FOLDER :
			case IResource.FILE :
				return refreshResource(target, depth, updateAliases, monitor);
		}
		return false;
	}

	protected boolean refreshResource(IResource target, int depth, boolean updateAliases, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		int totalWork = RefreshLocalVisitor.TOTAL_WORK;
		String title = Policy.bind("localstore.refreshing", target.getFullPath().toString()); //$NON-NLS-1$
		try {
			monitor.beginTask(title, totalWork);
			RefreshLocalVisitor visitor = updateAliases ? new RefreshLocalAliasVisitor(monitor) : new RefreshLocalVisitor(monitor);
			UnifiedTree tree = new UnifiedTree(target);
			tree.accept(visitor, depth);
			IStatus result = visitor.getErrorStatus();
			if (!result.isOK())
				throw new ResourceException(result);
			return visitor.resourcesChanged();
		} finally {
			monitor.done();
		}
	}

	/**
	 * Synchronizes the entire workspace with the local filesystem.
	 * The current implementation does this by synchronizing each of the
	 * projects currently in the workspace.  A better implementation may
	 * be possible.
	 */
	protected boolean refreshRoot(IWorkspaceRoot target, int depth, boolean updateAliases, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		IProject[] projects = target.getProjects();
		int totalWork = projects.length;
		String title = Policy.bind("localstore.refreshingRoot"); //$NON-NLS-1$
		try {
			monitor.beginTask(title, totalWork);
			// if doing depth zero, there is nothing to do (can't refresh the root).  
			// Note that we still need to do the beginTask, done pair.
			if (depth == IResource.DEPTH_ZERO)
				return false;
			boolean changed = false;
			// drop the depth by one level since processing the root counts as one level.
			depth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : depth;
			for (int i = 0; i < projects.length; i++)
				changed |= refresh(projects[i], depth, updateAliases, Policy.subMonitorFor(monitor, 1));
			return changed;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns the resource corresponding to the given workspace path.  The 
	 * "files" parameter is used for paths of two or more segments.  If true,
	 * a file is returned, otherwise a folder is returned.  Returns null if files is true
	 * and the path is not of sufficient length.
	 */
	protected IResource resourceFor(IPath path, boolean files) {
		int numSegments = path.segmentCount();
		if (files && numSegments < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH)
			return null;
		IWorkspaceRoot root = getWorkspace().getRoot();
		if (path.isRoot())
			return root;
		if (numSegments == 1)
			return root.getProject(path.segment(0));
		return files ? (IResource) root.getFile(path) : (IResource) root.getFolder(path);
	}

	/* (non-javadoc)
	 * @see IResouce.setLocalTimeStamp
	 */
	public long setLocalTimeStamp(IResource target, ResourceInfo info, long value) throws CoreException {
		IPath location = target.getLocation();
		if (location == null) {
			String message = Policy.bind("localstore.locationUndefined", target.getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, target.getFullPath(), message, null);
		}
		java.io.File localFile = location.toFile();
		localFile.setLastModified(value);
		long actualValue = CoreFileSystemLibrary.getLastModified(localFile.getAbsolutePath());
		updateLocalSync(info, actualValue);
		return actualValue;
	}

	public void shutdown(IProgressMonitor monitor) throws CoreException {
		historyStore.shutdown(monitor);
	}

	public void startup(IProgressMonitor monitor) throws CoreException {
		IPath location = getWorkspace().getMetaArea().getHistoryStoreLocation();
		location.toFile().mkdirs();
		historyStore = createHistoryStore(getWorkspace(), location, 256);
		historyStore.startup(monitor);
	}

	/**
	 * The ResourceInfo must be mutable.
	 */
	public void updateLocalSync(ResourceInfo info, long localSyncInfo) {
		info.setLocalSyncInfo(localSyncInfo);
		if (localSyncInfo == I_NULL_SYNC_INFO)
			info.clear(M_LOCAL_EXISTS);
		else
			info.set(M_LOCAL_EXISTS);
	}

	/**
	 * The target must exist in the workspace. The content InputStream is
	 * closed even if the method fails. If the force flag is false we only write
	 * the file if it does not exist or if it is already local and the timestamp
	 * has NOT changed since last synchronization, otherwise a CoreException
	 * is thrown.
	 */
	public void write(IFile target, IPath location, InputStream content, boolean force, boolean keepHistory, boolean append, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(null);
		try {
			//location can be null if based on an undefined variable
			if (location == null) {
				String message = Policy.bind("localstore.locationUndefined", target.getFullPath().toString()); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, target.getFullPath(), message, null);
			}
			final String locationString = location.toOSString();
			long stat = CoreFileSystemLibrary.getStat(locationString);
			if (CoreFileSystemLibrary.isReadOnly(stat)) {
				String message = Policy.bind("localstore.couldNotWriteReadOnly", target.getFullPath().toString()); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, target.getFullPath(), message, null);
			}
			long lastModified = CoreFileSystemLibrary.getLastModified(stat);
			final java.io.File localFile = new java.io.File(locationString);
			if (force) {
				if (append && !target.isLocal(IResource.DEPTH_ZERO) && !localFile.exists()) {
					// force=true, local=false, existsInFileSystem=false
					String message = Policy.bind("resources.mustBeLocal", target.getFullPath().toString()); //$NON-NLS-1$
					throw new ResourceException(IResourceStatus.RESOURCE_NOT_LOCAL, target.getFullPath(), message, null);
				}
			} else {
				if (target.isLocal(IResource.DEPTH_ZERO)) {
					// test if timestamp is the same since last synchronization
					ResourceInfo info = ((Resource) target).getResourceInfo(true, false);
					if (lastModified != info.getLocalSyncInfo()) {
						String message = Policy.bind("localstore.resourceIsOutOfSync", target.getFullPath().toString()); //$NON-NLS-1$
						throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message, null);
					}
				} else {
					if (localFile.exists()) {
						String message = Policy.bind("localstore.resourceExists", target.getFullPath().toString()); //$NON-NLS-1$
						throw new ResourceException(IResourceStatus.EXISTS_LOCAL, target.getFullPath(), message, null);
					}
					if (append) {
						String message = Policy.bind("resources.mustBeLocal", target.getFullPath().toString()); //$NON-NLS-1$
						throw new ResourceException(IResourceStatus.RESOURCE_NOT_LOCAL, target.getFullPath(), message, null);
					}
				}
			}
			// add entry to History Store.
			IFileState state = null; // file we just added to the history
			if (keepHistory && localFile.exists())
				//never move to the history store, because then the file is missing if write fails
				state = historyStore.addState(target.getFullPath(), location.toFile(), lastModified, false);
			getStore().write(localFile, content, append, monitor);
			// get the new last modified time and stash in the info
			lastModified = CoreFileSystemLibrary.getLastModified(locationString);
			ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
			updateLocalSync(info, lastModified);
			if (state != null)
				CoreFileSystemLibrary.copyAttributes(historyStore.getFileFor(state).getAbsolutePath(), locationString, false);
		} finally {
			try {
				content.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * If force is false, this method fails if there is already a resource in
	 * target's location.
	 */
	public void write(IFolder target, boolean force, IProgressMonitor monitor) throws CoreException {
		IPath location = locationFor(target);
		//location can be null if based on an undefined variable
		if (location == null) {
			String message = Policy.bind("localstore.locationUndefined", target.getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, target.getFullPath(), message, null);
		}
		java.io.File file = location.toFile();
		if (!force) {
			if (file.isDirectory()) {
				String message = Policy.bind("localstore.resourceExists", target.getFullPath().toString()); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.EXISTS_LOCAL, target.getFullPath(), message, null);
			}
			if (file.exists()) {
				String message = Policy.bind("localstore.fileExists", target.getFullPath().toString()); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message, null);
			}
		}
		getStore().writeFolder(file);
		long lastModified = CoreFileSystemLibrary.getLastModified(file.getAbsolutePath());
		ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
		updateLocalSync(info, lastModified);
	}

	/**
	 * Write the .project file without modifying the resource tree.  This is called
	 * during save when it is discovered that the .project file is missing.  The tree
	 * cannot be modified during save.
	 */
	public void writeSilently(IProject target) throws CoreException {
		IPath location = locationFor(target);
		//if the project location cannot be resolved, we don't know if a description file exists or not
		if (location == null)
			return;
		getStore().writeFolder(location.toFile());
		//can't do anything if there's no description
		IProjectDescription desc = ((Project) target).internalGetDescription();
		if (desc == null)
			return;
		//write the project's private description to the meta-data area
		getWorkspace().getMetaArea().writePrivateDescription(target);

		//write the file that represents the project description
		java.io.File file = location.append(IProjectDescription.DESCRIPTION_FILE_NAME).toFile();
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(file);
			new ModelObjectWriter().write(desc, fout);
		} catch (IOException e) {
			String msg = Policy.bind("resources.writeMeta", target.getFullPath().toString()); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, target.getFullPath(), msg, e);
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					// ignore failure to close stream
				}
			}
		}
		//for backwards compatibility, ensure the old .prj file is deleted
		getWorkspace().getMetaArea().clearOldDescription(target);
	}

	/** 
	 * Returns the real name of the resource on disk. Returns null if no local
	 * file exists by that name.  This is useful when dealing with
	 * case insensitive file systems.
	 */
	public String getLocalName(java.io.File target) {
		java.io.File root = target.getParentFile();
		String[] list = root.list();
		if (list == null)
			return null;
		String targetName = target.getName();
		for (int i = 0; i < list.length; i++)
			if (targetName.equalsIgnoreCase(list[i]))
				return list[i];
		return null;
	}
}