package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.internal.utils.Policy;

public class Folder extends Container implements IFolder {
protected Folder(IPath path, Workspace container) {
	super(path, container);
}
/**
 * Changes this folder to be a file in the resource tree and returns
 * the newly created file.  All related
 * properties are deleted.  It is assumed that on disk the resource is
 * already a file so no action is taken to delete the disk contents.
 * <p>
 * <b>This method is for the exclusive use of the local resource manager</b>
 *
 * @see FileSystemResourceManager#reportChanges
 */
public IFile changeToFile() throws CoreException {
	getPropertyManager().deleteProperties(this, IResource.DEPTH_INFINITE);
	workspace.deleteResource(this);
	IFile result = workspace.getRoot().getFile(path);
	workspace.createResource(result, false);
	return result;
}

/*
 * @see IFolder
 */
public void create(int updateFlags, boolean local, IProgressMonitor monitor) throws CoreException {
	final boolean force = (updateFlags & IResource.FORCE) != 0;
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.creating", getFullPath().toString());
		monitor.beginTask(message, Policy.totalWork);
		checkValidPath(path, FOLDER);
		try {
			workspace.prepareOperation();
			checkDoesNotExist();
			Container parent = (Container) getParent();
			ResourceInfo info = parent.getResourceInfo(false, false);
			parent.checkAccessible(getFlags(info));

			workspace.beginOperation(true);
			IPath location = getLocalManager().locationFor(this);
			java.io.File localFile = location.toFile();
			if (force) {
				if (!CoreFileSystemLibrary.isCaseSensitive()) {
					if (localFile.exists()) {
						String name = getLocalManager().getLocalName(localFile);
						if (name == null || localFile.getName().equals(name)) {
							delete(true, null);
						} else {
							// The file system is not case sensitive and there is already a file
							// under this location.
							String msg = Policy.bind("resources.existsLocalDifferentCase", location.removeLastSegments(1).append(name).toOSString());
							throw new ResourceException(IResourceStatus.CASE_VARIANT_EXISTS, getFullPath(), msg, null);
						}
					}
				}
			} else {
				if (localFile.exists()) {
					//return an appropriate error message for case variant collisions
					if (!CoreFileSystemLibrary.isCaseSensitive()) {
						String name = getLocalManager().getLocalName(localFile);
						if (!localFile.getName().equals(name)) {
							String msg =  Policy.bind("resources.existsLocalDifferentCase", location.removeLastSegments(1).append(name).toOSString());
							throw new ResourceException(IResourceStatus.CASE_VARIANT_EXISTS, getFullPath(), msg, null);
						}
					}
					String msg = Policy.bind("resources.fileExists", localFile.getAbsolutePath());
					throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, getFullPath(), msg, null);
				}
			}

			internalCreate(force, local, Policy.subMonitorFor(monitor, Policy.opWork));
		} catch (OperationCanceledException e) {
			workspace.getWorkManager().operationCanceled();
			throw e;
		} finally {
			workspace.endOperation(true, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}

/**
 * @see IFolder
 */
public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
	// funnel all operations to central method
	create((force ? IResource.FORCE : IResource.NONE), local, monitor);
}

/** 
 * Ensures that this folder exists in the workspace. This is similar in
 * concept to mkdirs but it does not work on projects.
 * If this folder is created, it will be marked as being local.
 */
public void ensureExists(IProgressMonitor monitor) throws CoreException {
	ResourceInfo info = getResourceInfo(false, false);
	int flags = getFlags(info);
	if (exists(flags, true))
		return;
	if (exists(flags, false)) {
		String message = Policy.bind("resources.folderOverFile", getFullPath().toString());
		throw new ResourceException(IResourceStatus.RESOURCE_WRONG_TYPE, getFullPath(), message, null);
	}
	Container parent = (Container) getParent();
	if (parent.getType() == PROJECT) {
		info = parent.getResourceInfo(false, false);
		parent.checkExists(getFlags(info), true);
	} else
		 ((Folder) parent).ensureExists(monitor);
	internalCreate(true, true, monitor);
}
/**
 * @see IResource#getType
 */
public int getType() {
	return FOLDER;
}
public void internalCreate(boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.creating", getFullPath().toString());
		monitor.beginTask(message, Policy.totalWork);
		workspace.createResource(this, false);
		if (local) {
			try {
				getLocalManager().write(this, force, Policy.subMonitorFor(monitor, Policy.totalWork * 75 / 100));
			} catch (CoreException e) {
				// a problem happened creating the folder on disk, so delete from the workspace
				workspace.deleteResource(this);
				throw e; // rethrow
			}
		}
		setLocal(local, DEPTH_ZERO, Policy.subMonitorFor(monitor, Policy.totalWork * 25 / 100));
		if (!local)
			getResourceInfo(true, true).setModificationStamp(IResource.NULL_STAMP);
	} finally {
		monitor.done();
	}
}

}
