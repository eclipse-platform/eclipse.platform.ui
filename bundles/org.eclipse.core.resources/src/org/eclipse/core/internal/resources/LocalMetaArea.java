package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.utils.*;
import java.util.*;
import java.io.*;

public class LocalMetaArea implements ICoreConstants {
	protected IPath location;

	/* package */ static final String F_BACKUP_FILE_EXTENSION = ".bak";
	/* package */ static final String F_DESCRIPTION = ".workspace";
	/* package */ static final String F_HISTORY_STORE = ".history";
	/* package */ static final String F_MARKERS = ".markers";
	/* package */ static final String F_PROJECT = ".prj";
	/* package */ static final String F_PROJECTS = ".projects";
	/* package */ static final String F_PROPERTIES = ".properties";
	/* package */ static final String F_ROOT = ".root";
	/* package */ static final String F_SAFE_TABLE = ".safetable";
	/* package */ static final String F_SNAP = ".snap";
	/* package */ static final String F_SNAP_EXTENSION = "snap";
	/* package */ static final String F_SYNCINFO = ".syncinfo";
	/* package */ static final String F_TREE = ".tree";
public LocalMetaArea() {
}
private Properties buildPathProperties(Hashtable paths) {
	Properties result = new Properties();
	for (Enumeration keys = paths.keys(); keys.hasMoreElements();) {
		String key = (String) keys.nextElement();
		StringBuffer entry = new StringBuffer(100);
		IPath[] list = (IPath[]) paths.get(key);
		for (int i = 0; i < list.length; i++) {
			entry.append(list[i].toOSString());
			entry.append(";");
		}
		result.put(key, entry.toString());
	}
	return result;
}
/**
 * 
 */
public void delete(IProject target) throws CoreException {
	IPath path = getLocationFor(target);
	if (!Workspace.clear(path.toFile()) && path.toFile().exists()) {
		String message = Policy.bind("resources.deleteMeta", target.getFullPath().toString());
		throw new ResourceException(IResourceStatus.FAILED_DELETE_METADATA, target.getFullPath(), message, null);
	}
}
public IPath getBackupLocationFor(IPath file) {
	return file.removeLastSegments(1).append(file.lastSegment() + F_BACKUP_FILE_EXTENSION);
}
public IPath getDescriptionLocationFor(IProject target) {
	return getLocationFor(target).append(F_PROJECT);
}
public IPath getHistoryStoreLocation() {
	return getLocation().append(F_HISTORY_STORE);
}
/**
 * Returns the local filesystem location which contains the META data for
 * the resources plugin (i.e., the entire workspace).
 */
public IPath getLocation() {
	if (location == null)
		location = ResourcesPlugin.getPlugin().getStateLocation();
	return location;
}
/**
 * Returns the local filesystem location in which the meta data for the given
 * resource is stored.
 */
public IPath getLocationFor(IResource resource) {
	if (resource.getType() == IResource.ROOT)
		return getLocation().append(F_ROOT);
	else
		return getLocation().append(F_PROJECTS).append(resource.getProject().getName());
}
/**
 * Returns the path of the file in which to save markers for the given resource.
 * Should only be called for the workspace root and projects.
 */
public IPath getMarkersLocationFor(IResource resource) {
	Assert.isNotNull(resource);
	Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
	return getLocationFor(resource).append(F_MARKERS);
}
/**
 * Returns the path of the file in which to snapshot markers for the given resource.
 * Should only be called for the workspace root and projects.
 */
public IPath getMarkersSnapshotLocationFor(IResource resource) {
	return getMarkersLocationFor(resource).addFileExtension(F_SNAP_EXTENSION);
}

public IPath getPropertyStoreLocation(IResource resource) {
	int type = resource.getType();
	Assert.isTrue(type != IResource.FILE && type != IResource.FOLDER);
	return getLocationFor(resource).append(F_PROPERTIES);
}
public IPath getSafeTableLocationFor(String pluginId) {
	IPath prefix = getLocation().append(F_SAFE_TABLE);
	// if the plugin is the resources plugin, we return the master table location
	if (pluginId.equals(ResourcesPlugin.getPlugin().getDescriptor().getUniqueIdentifier()))
		return prefix.append(pluginId); // master table
	int saveNumber = getWorkspace().getSaveManager().getSaveNumber(pluginId);
	return prefix.append(pluginId + "." + saveNumber);
}
public IPath getSnapshotLocationFor(IResource resource) {
	return getLocation().append(F_SNAP);
}
/**
 * Returns the path of the file in which to save the sync information for the given resource.
 * Should only be called for the workspace root and projects.
 */
public IPath getSyncInfoLocationFor(IResource resource) {
	Assert.isNotNull(resource);
	Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
	return getLocationFor(resource).append(F_SYNCINFO);
}
/**
 * Returns the path of the file in which to snapshot the sync information for the given resource.
 * Should only be called for the workspace root and projects.
 */
public IPath getSyncInfoSnapshotLocationFor(IResource resource) {
	return getSyncInfoLocationFor(resource).addFileExtension(F_SNAP_EXTENSION);
}
/**
 * Returns the local file system location of the tree file for the given resource. This
 * file does not follow the same save number as its plug-in. So, the number here is called
 * "sequence number" and not "save number" to avoid confusion.
 */
public IPath getTreeLocationFor(IResource target, boolean updateSequenceNumber) {
	IPath key = target.getFullPath().append(F_TREE);
	String sequenceNumber = getWorkspace().getSaveManager().getMasterTable().getProperty(key.toString());
	if (sequenceNumber == null)
		sequenceNumber = "0";
	if (updateSequenceNumber) {
		int n = new Integer(sequenceNumber).intValue() + 1;
		n = n < 0 ? 1 : n;
		sequenceNumber = new Integer(n).toString();
		getWorkspace().getSaveManager().getMasterTable().setProperty(key.toString(), new Integer(sequenceNumber).toString());
	}
	return getLocationFor(target).append(sequenceNumber + F_TREE);
}
public IPath getWorkingLocation(IResource resource, IPluginDescriptor plugin) {
	return getLocationFor(resource).append(plugin.getUniqueIdentifier());
}
protected Workspace getWorkspace() {
	return (Workspace) ResourcesPlugin.getWorkspace();
}
public IPath getWorkspaceDescriptionLocation() {
	return getLocation().append(F_DESCRIPTION);
}
public boolean hasSavedWorkspace() throws CoreException {
	return getWorkspaceDescriptionLocation().toFile().exists() || getBackupLocationFor(getWorkspaceDescriptionLocation()).toFile().exists();
}
public ProjectDescription read(IProject project) throws CoreException {
	IPath path = getDescriptionLocationFor(project);
	IPath tempPath = getBackupLocationFor(path);
	try {
		return (ProjectDescription) new ModelObjectReader().read(path, tempPath);
	} catch (IOException e) {
		return null;
	}
}
public WorkspaceDescription readWorkspace() throws CoreException {
	IPath path = getWorkspaceDescriptionLocation();
	IPath tempPath = getBackupLocationFor(path);
	try {
		return (WorkspaceDescription) new ModelObjectReader().read(path, tempPath);
	} catch (IOException e) {
		return null;
	}
}
public void write(IProject target) throws CoreException {
	IPath path = getDescriptionLocationFor(target);
	path.toFile().mkdirs();
	IPath tempPath = getBackupLocationFor(path);
	try {
		IProjectDescription description = ((Project) target).internalGetDescription();
		new ModelObjectWriter().write(description, path, tempPath);
	} catch (IOException e) {
		String message = Policy.bind("resourceswriteMeta", target.getFullPath().toString());
		throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, target.getFullPath(), message, null);
	}
}
public void writeWorkspace(WorkspaceDescription description) throws CoreException {
	IPath path = getWorkspaceDescriptionLocation();
	path.toFile().getParentFile().mkdirs();
	IPath tempPath = getBackupLocationFor(path);
	try {
		new ModelObjectWriter().write(description, path, tempPath);
	} catch (IOException e) {
		String message = Policy.bind("resources.writeWorkspaceMeta", path.toString());
		throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, null, message, e);
	}
	description.clean();
}
}
