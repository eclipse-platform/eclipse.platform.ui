package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
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
	getPropertyManager().deleteProperties(this);
	workspace.deleteResource(this);
	IFile result = workspace.getRoot().getFile(path);
	workspace.createResource(result, false);
	return result;
}
/**
 * @see IFolder
 */
public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("creating", new String[] { getFullPath().toString()}), Policy.totalWork);
		checkValidPath(path, FOLDER);
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			int flags = getFlags(info);
			checkDoesNotExist(flags, false);

			workspace.beginOperation(true);
			refreshLocal(DEPTH_ZERO, null);
			Container parent = (Container) getParent();
			info = parent.getResourceInfo(false, false);
			flags = getFlags(info);
			parent.checkAccessible(flags);

			info = getResourceInfo(false, false);
			flags = getFlags(info);
			if (force) {
				if (exists(flags, false))
					return;
			} else {
				checkDoesNotExist(flags, false);
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
		String message = Policy.bind("folderOverFile", new String[] { getFullPath().toString()});
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
		monitor.beginTask("Creating file.", Policy.totalWork);
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
