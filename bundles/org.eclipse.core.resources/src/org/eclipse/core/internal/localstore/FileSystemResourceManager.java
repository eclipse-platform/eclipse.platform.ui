package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;

import java.io.*;
import java.util.*;
/**
 * Manages the synchronization between the workspace's view and the file system.  
 */
public class FileSystemResourceManager implements ICoreConstants, IManager {

	protected Workspace workspace;
	protected HistoryStore historyStore;
	protected FileSystemStore localStore;
	
public FileSystemResourceManager(Workspace workspace) {
	this.workspace = workspace;
	localStore = new FileSystemStore();
}
/**
 * Returns a container for the given file system location or null if there
 * is no mapping for this path. If the path has only one segment, then an 
 * <code>IProject</code> is returned.  Otherwise, the returned object
 * is a <code>IFolder</code>.  This method does NOT check the existence
 * of a folder in the given location. Location cannot be null.
 */
public IContainer containerFor(IPath location) {
	IPath path = resourcePathFor(location);
	if (path == null)
		return null;
	if (path.isRoot())
		return getWorkspace().getRoot();
	if (path.segmentCount() == 1)
		return getWorkspace().getRoot().getProject(path.segment(0));
	else
		return getWorkspace().getRoot().getFolder(path);
}
public void copy(IResource target, IResource destination, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		int totalWork = ((Resource) target).countResources(IResource.DEPTH_INFINITE, false);
		String title = Policy.bind("localstore.copying", target.getFullPath().toString());
		monitor.beginTask(title, totalWork);
		// use locationFor() instead of getLocation() to avoid null 
		if (locationFor(destination).toFile().exists()) {
			String message = Policy.bind("localstore.resourceExists", destination.getFullPath().toString());
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, destination.getFullPath(), message, null);
		}
		CopyVisitor visitor = new CopyVisitor(target, destination, force, monitor);
		UnifiedTree tree = new UnifiedTree(target);
		tree.accept(visitor, IResource.DEPTH_INFINITE);
		IStatus status = visitor.getStatus();
		if (!status.isOK())
			throw new ResourceException(status);
	} finally {
		monitor.done();
	}
}
public void delete(IResource target, boolean force, boolean convertToPhantom, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		Resource resource = (Resource) target;
		int totalWork = resource.countResources(IResource.DEPTH_INFINITE, false);
		totalWork *= 2;
		String title = Policy.bind("localstore.deleting", resource.getFullPath().toString());
		monitor.beginTask(title, totalWork);
		MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_DELETE_LOCAL, Policy.bind("localstore.deleteProblem"), null);
		List skipList = null;
		UnifiedTree tree = new UnifiedTree(target);
		if (!force) {
			IProgressMonitor sub = Policy.subMonitorFor(monitor, totalWork / 2);
			sub.beginTask("", 10000);
			CollectSyncStatusVisitor refreshVisitor = new CollectSyncStatusVisitor(Policy.bind("localstore.deleteProblem"), sub);
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
public IFile fileFor(IPath location) {
	IPath path = resourcePathFor(location);
	return path != null ? getWorkspace().getRoot().getFile(path) : null;
}
/**
 * The project description file is the only metadata file stored
 * outside the metadata area.  It is stored as a file directly 
 * under the project location.
 */
public IPath getDescriptionLocationFor(IProject target) {
	//can't use IResource#getLocation() because it returns null if it doesn't exist
	return ((Project)target).getLocalManager().locationFor(target).append(IProjectDescription.DESCRIPTION_FILE_NAME);
}
public HistoryStore getHistoryStore() {
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
	return getDescriptionLocationFor(project).toFile().exists();
}
/**
 * The target must exist in the workspace.  This method must only ever
 * be called from Project.writeDescription(), because that method ensures
 * that the description isn't then immediately discovered as a new change.
 */
public void internalWrite(IProject target, IProjectDescription description, int updateFlags) throws CoreException {
	IPath location = locationFor(target);
	getStore().writeFolder(location.toFile());
	//write the project location to the meta-data area
	getWorkspace().getMetaArea().writeLocation(target);
	//can't do anything if there's no description
	if (description == null)
		return;

	//write the model to a byte array
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try {
		new ModelObjectWriter().write(description, out);
	} catch (IOException e) {
		String msg = Policy.bind("resources.writeMeta", target.getFullPath().toString());
		throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, target.getFullPath(), msg, e);
	}
	ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

	//write the contents to the IFile that represents the description
	IFile descriptionFile = target.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
	if (!descriptionFile.exists())
		workspace.createResource(descriptionFile, false);
	descriptionFile.setContents(in, updateFlags, null);

	//update the timestamp on the project as well so we know when it has
	//been changed from the outside
	long lastModified = ((Resource)descriptionFile).getResourceInfo(false, false).getLocalSyncInfo();
	ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
	updateLocalSync(info, lastModified, true);

	//for backwards compatibility, ensure the old .prj file is deleted
	getWorkspace().getMetaArea().clearOldDescription(target);
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
	ResourceInfo projectInfo = ((Resource)target).getResourceInfo(false, false);
	if (projectInfo == null)
		return false;
	return projectInfo.getLocalSyncInfo() == CoreFileSystemLibrary.getLastModified(descriptionFile.getLocation().toOSString());
}
/**
 * Returns true if the given resource is synchronized with the filesystem
 * to the given depth.  Returns false otherwise.
 * @see IResource.isSynchronized
 */
public boolean isSynchronized(IResource resource, int depth) {
	switch (resource.getType()) {
		case IResource.ROOT:
			if (depth == IResource.DEPTH_ZERO)
				return true;
			//check sync on child projects.
			depth = depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : depth;
			IProject[] projects = ((IWorkspaceRoot)resource).getProjects();
			for (int i = 0; i < projects.length; i++) {
				if (!isSynchronized(projects[i], depth))
					return false;
			}
			return true;
		case IResource.PROJECT:
			if (!resource.isAccessible())
				return true;
				//fall through
		default:
			IsSynchronizedVisitor visitor = new IsSynchronizedVisitor(Policy.monitorFor(null));
			UnifiedTree tree = new UnifiedTree(resource);
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
}
public IPath locationFor(IResource target) {
	switch (target.getType()) {
		case IResource.ROOT :
			return Platform.getLocation();
		case IResource.PROJECT :
			Project project = (Project) target.getProject();
			IProjectDescription description = project.internalGetDescription();
			if (description != null && description.getLocation() != null) {
				return description.getLocation();
			}
			return getProjectDefaultLocation(project);
		default:
			//first get the location of the project (without the project name)
			description = ((Project)target.getProject()).internalGetDescription();
			if (description != null && description.getLocation() != null) {
				return description.getLocation().append(target.getProjectRelativePath());
			} else {
				return Platform.getLocation().append(target.getFullPath());
			}
	}
}
public void move(IResource target, IPath destination, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("localstore.moving", target.getFullPath().toString()), Policy.totalWork);
		IResource resource = null;
		switch (target.getType()) {
			case IResource.PROJECT :
				return; // do nothing
			case IResource.FOLDER :
				resource = getWorkspace().getRoot().getFolder(destination);
				break;
			case IResource.FILE :
				resource = getWorkspace().getRoot().getFile(destination);
				break;
		}
		IPath sourceLocation = locationFor(target);
		IPath destinationLocation = locationFor(resource);
		if (keepHistory) {
			if (target.getType() == IResource.FOLDER) {
				// don't keep history for team private members.
				IResource[] children = ((IFolder) target).members();
				destinationLocation.toFile().mkdirs();
				int work = Policy.totalWork / Math.max(children.length, 1);
				for (int i = 0; i < children.length; i++)
					move(children[i], destination.append(children[i].getName()), keepHistory, Policy.subMonitorFor(monitor, work));
				if (!sourceLocation.toFile().delete()) {
					String message = Policy.bind("localstore.couldnotDelete", sourceLocation.toString());
					throw new ResourceException(IResourceStatus.FAILED_DELETE_LOCAL, sourceLocation, message, null);
				}
			} else {
				long lastModified = target.getLocation().toFile().lastModified();
				getHistoryStore().addState(target.getFullPath(), sourceLocation, lastModified, false);
				getStore().move(sourceLocation.toFile(), destinationLocation.toFile(), false, Policy.subMonitorFor(monitor, Policy.totalWork));
			}
		} else
			getStore().move(sourceLocation.toFile(), destinationLocation.toFile(), false, Policy.subMonitorFor(monitor, Policy.totalWork));
	} finally {
		monitor.done();
	}
}
public InputStream read(IFile target, boolean force, IProgressMonitor monitor) throws CoreException {
	// thread safety: (the location can be null if the project for this file does not exist)
	IPath location = locationFor(target);
	if (location == null)
		 ((Project) target.getProject()).checkExists(NULL_FLAG, true);
	java.io.File localFile = location.toFile();
	if (!localFile.exists()) {
		String message = Policy.bind("localstore.fileNotFound", localFile.getAbsolutePath());
		throw new ResourceException(IResourceStatus.FAILED_READ_LOCAL, target.getFullPath(), message, null);
	}
	if (!force) {
		ResourceInfo info = ((Resource) target).getResourceInfo(true, false);
		int flags = ((Resource) target).getFlags(info);
		((Resource) target).checkExists(flags, true);
		if (CoreFileSystemLibrary.getLastModified(localFile.getAbsolutePath()) != info.getLocalSyncInfo()) {
			String message = Policy.bind("localstore.resourceIsOutOfSync", target.getFullPath().toString());
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
 * case the project location needs to be read from disk as well.
 * @exception CoreException if there was any failure to read the project
 * description, or if the description was missing.
 */
public ProjectDescription read(IProject target, boolean creation) throws CoreException {
	//read the project location if this project is being created
	IPath projectLocation = null;
	if (creation) {
		projectLocation = getWorkspace().getMetaArea().readLocation(target);
	} else {
		IProjectDescription description = ((Project)target).internalGetDescription();
		if (description != null && description.getLocation() != null) {
			projectLocation = description.getLocation();
		}
	}
	final boolean isDefaultLocation = projectLocation == null;
	if (isDefaultLocation) {
		projectLocation = getProjectDefaultLocation(target);
	}
	IPath descriptionPath = projectLocation.append(IProjectDescription.DESCRIPTION_FILE_NAME);
	ProjectDescription description = null;

	if (!descriptionPath.toFile().exists()) {
		//try the legacy location in the meta area
		description = getWorkspace().getMetaArea().readOldDescription(target);
		if (description == null) {
			String msg = Policy.bind("resources.missingProjectMeta", target.getName());
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, null);
		}
		return description;
	}
	//hold onto any exceptions until after sync info is updated, then throw it
	ResourceException error = null;
	try {
		description = (ProjectDescription)new ModelObjectReader().read(descriptionPath);
	} catch (IOException e) {
		String msg = Policy.bind("resources.readProjectMeta", target.getName());
		error = new ResourceException(IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, e);
	}
	if (error == null && description == null) {
		String msg = Policy.bind("resources.readProjectMeta", target.getName());
		error = new ResourceException(IResourceStatus.FAILED_READ_METADATA, target.getFullPath(), msg, null);
	}
	if (description != null && !isDefaultLocation)
		description.setLocation(projectLocation);
	//don't trust the project name in the description file
	description.setName(target.getName());
	long lastModified = CoreFileSystemLibrary.getLastModified(descriptionPath.toOSString());
	IFile descriptionFile = target.getFile(IProjectDescription.DESCRIPTION_FILE_NAME);
	//don't get a mutable copy because we might be in restore which isn't an operation
	//it doesn't matter anyway because local sync info is not included in deltas
	ResourceInfo info = ((Resource)descriptionFile).getResourceInfo(false, false);
	if (info == null) {
		//create a new resource on the sly -- don't want to start an operation
		info = getWorkspace().createResource(descriptionFile, false);
	}
	updateLocalSync(info, lastModified, true);
	
	//update the timestamp on the project as well so we know when it has
	//been changed from the outside
	info = ((Resource) target).getResourceInfo(false, true);
	updateLocalSync(info, lastModified, true);

	if (error != null)
		throw error;
	return description;
}
public boolean refresh(IResource target, int depth, IProgressMonitor monitor) throws CoreException {
	switch (target.getType()) {
		case IResource.ROOT :
			return refreshRoot((IWorkspaceRoot) target, depth, monitor);
		case IResource.PROJECT :
			if (!target.isAccessible())
				return false;
			//fall through
		case IResource.FOLDER :
		case IResource.FILE :
			return refreshResource(target, depth, monitor);
	}
	return false;
}
protected boolean refreshResource(IResource target, int depth, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	int totalWork = RefreshLocalVisitor.TOTAL_WORK;
	String title = Policy.bind("localstore.refreshing", target.getFullPath().toString());
	try {
		monitor.beginTask(title, totalWork);
		RefreshLocalVisitor visitor = new RefreshLocalVisitor(monitor);
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
protected boolean refreshRoot(IWorkspaceRoot target, int depth, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	IProject[] projects = target.getProjects();
	int totalWork = projects.length;
	String title = Policy.bind("localstore.refreshing", target.getFullPath().toString());
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
			changed |= refresh(projects[i], depth, Policy.subMonitorFor(monitor, 1));
		return changed;
	} finally {
		monitor.done();
	}
}
/**
 * Returns null if there is no mapping for this path or the resource
 * does not exist in the file system.
 */
public IResource resourceFor(IPath location) throws CoreException {
	IPath resourcePath = resourcePathFor(location);
	if (resourcePath == null)
		return null;
	if (resourcePath.equals(Path.ROOT))
		return getWorkspace().getRoot();
	// check the workspace first
	IResource target = getWorkspace().getRoot().findMember(resourcePath);
	if (target != null)
		return target;

	// couldn't find it in the workspace so look in the filesystem
	if (location.toFile().isFile())
		return getWorkspace().getRoot().getFile(resourcePath);
	if (location.toFile().isDirectory())
		return getWorkspace().getRoot().getFolder(resourcePath);

	// can't find any trace of this resource so return null
	return null;
}
/**
 * Returns a resource path to the given local location. Returns null if
 * it is not under a project's location.
 */
protected IPath resourcePathFor(IPath location) {
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
public void shutdown(IProgressMonitor monitor) throws CoreException {
	historyStore.shutdown(monitor);
}
public void startup(IProgressMonitor monitor) throws CoreException {
	IPath location = getWorkspace().getMetaArea().getHistoryStoreLocation();
	location.toFile().mkdirs();
	historyStore = new HistoryStore(getWorkspace(), location, 256);
	historyStore.startup(monitor);
}
/**
 * The ResourceInfo must be mutable.
 */
public void updateLocalSync(ResourceInfo info, long localSyncInfo, boolean isFile) {
	info.setLocalSyncInfo(localSyncInfo);

	if (localSyncInfo == I_NULL_SYNC_INFO)
		info.clear(M_LOCAL_EXISTS);
	else
		info.set(M_LOCAL_EXISTS);

	if (isFile)
		info.set(M_LOCAL_IS_FILE);
	else
		info.clear(M_LOCAL_IS_FILE);
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
		java.io.File localFile = location.toFile();
		long stat = CoreFileSystemLibrary.getStat(localFile.getAbsolutePath());
		if (CoreFileSystemLibrary.isReadOnly(stat)) {
			String message = Policy.bind("localstore.couldNotWriteReadOnly", target.getFullPath().toString());
			throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, target.getFullPath(), message, null);
		}
		long lastModified = CoreFileSystemLibrary.getLastModified(stat);
		if (!force) {
			if (target.isLocal(IResource.DEPTH_ZERO)) {
				// test if timestamp is the same since last synchronization
				ResourceInfo info = ((Resource) target).getResourceInfo(true, false);
				if (lastModified != info.getLocalSyncInfo()) {
					String message = Policy.bind("localstore.resourceIsOutOfSync", target.getFullPath().toString());
					throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message, null);
				}
			} else
				if (localFile.exists()) {
					String message = Policy.bind("localstore.resourceExists", target.getFullPath().toString());
					throw new ResourceException(IResourceStatus.EXISTS_LOCAL, target.getFullPath(), message, null);
				}
		}
		// add entry to History Store.
		UniversalUniqueIdentifier uuid = null; // uuid to locate the file on history
		if (keepHistory && localFile.exists())
			//never move to the history store, because then the file is missing if write fails
			uuid = historyStore.addState(target.getFullPath(), location, lastModified, false);
		getStore().write(localFile, content, append, monitor);
		// get the new last modified time and stash in the info
		lastModified = CoreFileSystemLibrary.getLastModified(localFile.getAbsolutePath());
		ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
		updateLocalSync(info, lastModified, true);
		if (uuid != null)
			CoreFileSystemLibrary.copyAttributes(historyStore.getFileFor(uuid).getAbsolutePath(), localFile.getAbsolutePath(), false);
	} finally {
		try {
			content.close();
		} catch (IOException e) {
		}
	}
}
/**
 * If force is false, this method fails if there is already a resource in
 * target's location.
 */
public void write(IFolder target, boolean force, IProgressMonitor monitor) throws CoreException {
	java.io.File file = locationFor(target).toFile();
	if (!force)
		if (file.isDirectory()) {
			String message = Policy.bind("localstore.resourceExists", target.getFullPath().toString());
			throw new ResourceException(IResourceStatus.EXISTS_LOCAL, target.getFullPath(), message, null);
		} else {
			if (file.exists()) {
				String message = Policy.bind("localstore.fileExists", target.getFullPath().toString());
				throw new ResourceException(IResourceStatus.OUT_OF_SYNC_LOCAL, target.getFullPath(), message, null);
			}
		}
	getStore().writeFolder(file);
	long lastModified = CoreFileSystemLibrary.getLastModified(file.getAbsolutePath());
	ResourceInfo info = ((Resource) target).getResourceInfo(false, true);
	updateLocalSync(info, lastModified, false);
}

/**
 * Write the .project file without modifying the resource tree.  This is called
 * during save when it is discovered that the .project file is missing.  The tree
 * cannot be modified during save.
 */
public void writeSilently(IProject target) throws CoreException {
	IPath location = locationFor(target);
	getStore().writeFolder(location.toFile());
	//can't do anything if there's no description
	IProjectDescription desc = ((Project)target).internalGetDescription();
	if (desc == null)
		return;
	//write the project location to the meta-data area
	getWorkspace().getMetaArea().writeLocation(target);
	
	//write the file that represents the project description
	java.io.File file = location.append(IProjectDescription.DESCRIPTION_FILE_NAME).toFile();
	FileOutputStream fout = null;
	try {
		fout = new FileOutputStream(file);
		new ModelObjectWriter().write(desc, fout);
	} catch (IOException e) {
		String msg = Policy.bind("resources.writeMeta", target.getFullPath().toString());
		throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, target.getFullPath(), msg, e);
	} finally {
		if (fout != null) {
			try {
				fout.close();
			} catch (IOException e) {
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