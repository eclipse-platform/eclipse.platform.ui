package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
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
		monitor.beginTask(Policy.bind("settingContents", new String[] { getFullPath().toString()}), Policy.totalWork);
		Assert.isNotNull(content, "Content cannot be null.");
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			checkAccessible(getFlags(info));

			workspace.beginOperation(true);
			internalSetContents(content, force, keepHistory, true, Policy.subMonitorFor(monitor, Policy.opWork));
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
		monitor.beginTask(Policy.bind("creating", new String[] { getFullPath().toString()}), Policy.totalWork);
		checkValidPath(path, FILE);
		try {
			workspace.prepareOperation();
			boolean local = content != null;
			ResourceInfo info = getResourceInfo(false, false);
			int flags = getFlags(info);
			checkDoesNotExist(flags, false);

			workspace.beginOperation(true);
			Container parent = (Container) getParent();
			parent.refreshLocal(DEPTH_ZERO, null);
			refreshLocal(DEPTH_ZERO, null);

			info = parent.getResourceInfo(false, false);
			flags = getFlags(info);
			parent.checkAccessible(flags);

			info = getResourceInfo(false, false);
			flags = getFlags(info);
			if (force) {
				// if the resource exists (i.e. was just discovered in the local refresh)
				// then we delete what we just discovered and create the new resource
				if (exists(flags, false))
					delete(true, Policy.subMonitorFor(monitor, Policy.opWork / 2));
			} else {
				monitor.worked(Policy.opWork / 2);
				checkDoesNotExist(flags, false);
			}
			workspace.createResource(this, false);
			if (local) {
				try {
					internalSetContents(content, force, false, false, Policy.subMonitorFor(monitor, Policy.opWork / 2));
				} catch (CoreException e) {
					// a problem happened creating the file on disk, so delete from the workspace
					workspace.deleteResource(this);
					throw e; // rethrow
				}
			}
			setLocal(local, DEPTH_ZERO);
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
protected void internalSetContents(InputStream content, boolean force, boolean keepHistory, boolean append, IProgressMonitor monitor) throws CoreException {
	if (content == null)
		content = new ByteArrayInputStream(new byte[0]);
	getLocalManager().write(this, content, force, keepHistory, append, monitor);
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
		monitor.beginTask(Policy.bind("settingContents", new String[] { getFullPath().toString()}), Policy.totalWork);
		try {
			workspace.prepareOperation();
			ResourceInfo info = getResourceInfo(false, false);
			checkAccessible(getFlags(info));

			workspace.beginOperation(true);
			internalSetContents(content, force, keepHistory, false, Policy.subMonitorFor(monitor, Policy.opWork));
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
	if (exists())
		setContents(source.getContents(), force, keepHistory, monitor);
	else
		create(source.getContents(), force, monitor);
}
}
