package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

public class File extends Resource implements IFile {
protected File(IPath path, Workspace container) {
	super(path, container);
}
/**
 * @see IFile
 */
public void appendContents(InputStream content, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.settingContents", getFullPath().toString());
		monitor.beginTask(message, Policy.totalWork);
		Assert.isNotNull(content, "Content cannot be null.");
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			checkAccessible(getFlags(info));

			workspace.beginOperation(true);
			internalSetContents(content, getLocalManager().locationFor(this), force, keepHistory, true, Policy.subMonitorFor(monitor, Policy.opWork));
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
 * Changes this file to be a folder in the resource tree and returns
 * the newly created folder.  All related
 * properties are deleted.  It is assumed that on disk the resource is
 * already a folder/directory so no action is taken to delete the disk
 * contents.
 * <p>
 * <b>This method is for the exclusive use of the local resource manager</b>
 *
 * @see FileSystemResourceManager#reportChanges
 */
public IFolder changeToFolder() throws CoreException {
	getPropertyManager().deleteProperties(this);
	workspace.deleteResource(this);
	IFolder result = workspace.getRoot().getFolder(path);
	workspace.createResource(result, false);
	return result;
}
/**
 * @see IFile
 */
public void create(InputStream content, boolean force, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.creating", getFullPath().toString());
		monitor.beginTask(message, Policy.totalWork);
		checkValidPath(path, FILE);
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
						if (localFile.getName().equals(name)) {
							delete(true, null);
						} else {
							// The file system is not case sensitive and there is already a file
							// under this location.
							String msg = Policy.bind("resources.existsDifferentCase", location.removeLastSegments(1).append(name).toOSString());
							throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, getFullPath(), msg, null);
						}
					}
				}
			} else {
				if (localFile.exists()) {
					String msg = Policy.bind("resources.fileExists", localFile.getAbsolutePath());
					throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, getFullPath(), msg, null);
				}
			}
			monitor.worked(Policy.opWork * 40 / 100);

			workspace.createResource(this, false);
			boolean local = content != null;
			if (local) {
				try {
					internalSetContents(content, location, force, false, false, Policy.subMonitorFor(monitor, Policy.opWork * 40 / 100));
				} catch (CoreException e) {
					// a problem happened creating the file on disk, so delete from the workspace
					workspace.deleteResource(this);
					throw e; // rethrow
				}
			}
			setLocal(local, DEPTH_ZERO, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));
			if (!local)
				getResourceInfo(true, true).setModificationStamp(IResource.NULL_STAMP);
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
 * @see IFile#getContents
 */
public InputStream getContents() throws CoreException {
	return getContents(false);
}
/**
 * @see IFile
 */
public InputStream getContents(boolean force) throws CoreException {
	ResourceInfo info = getResourceInfo(false, false);
	int flags = getFlags(info);
	checkAccessible(flags);
	checkLocal(flags, DEPTH_ZERO);
	return getLocalManager().read(this, force, null);
}
/**
 * @see IFile#getHistory
 */
public IFileState[] getHistory(IProgressMonitor monitor) throws CoreException {
	// FIXME: monitor is not used
	return getLocalManager().getHistoryStore().getStates(getFullPath());
}
public int getType() {
	return FILE;
}
protected void internalSetContents(InputStream content, IPath location, boolean force, boolean keepHistory, boolean append, IProgressMonitor monitor) throws CoreException {
	if (content == null)
		content = new ByteArrayInputStream(new byte[0]);
	getLocalManager().write(this, location, content, force, keepHistory, append, monitor);
	ResourceInfo info = getResourceInfo(false, true);
	info.incrementContentId();
	workspace.updateModificationStamp(info);
}
/**
 * @see IFile
 */
public void setContents(InputStream content, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.settingContents", getFullPath().toString());
		monitor.beginTask(message, Policy.totalWork);
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			checkAccessible(getFlags(info));

			workspace.beginOperation(true);
			internalSetContents(content, getLocalManager().locationFor(this), force, keepHistory, false, Policy.subMonitorFor(monitor, Policy.opWork));
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
 * @see IFile#setContents
 */
public void setContents(IFileState source, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException {
	setContents(source.getContents(), force, keepHistory, monitor);
}
}
