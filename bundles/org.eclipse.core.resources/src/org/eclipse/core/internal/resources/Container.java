package org.eclipse.core.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.*;
import java.util.Enumeration;

public abstract class Container extends Resource implements IContainer {
protected Container(IPath path, Workspace container) {
	super(path, container);
}
/**
 * Converts this resource and all its children into phantoms by modifying
 * their resource infos in-place.
 */
public void convertToPhantom() throws CoreException {
	if (isPhantom())
		return;
	super.convertToPhantom();
	IResource[] members = members(true);
	for (int i = 0; i < members.length; i++)
		 ((Resource) members[i]).convertToPhantom();
}
/** 
 * @see IContainer
 */
public boolean exists(IPath path) {
	return workspace.getResourceInfo(getFullPath().append(path), false, false) != null;
}
/**
 * @see IContainer#findMember(String)
 */
public IResource findMember(String name) {
	return findMember(name, false);
}
/**
 * @see IContainer#findMember(String, boolean)
 */
public IResource findMember(String name, boolean phantom) {
	IPath childPath = getFullPath().append(name);
	ResourceInfo info = workspace.getResourceInfo(childPath, phantom, false);
	return info == null ? null : workspace.newResource(childPath, info.getType());
}
/**
 * @see IContainer#findMember(IPath)
 */
public IResource findMember(IPath path) {
	return findMember(path, false);
}
/**
 * @see IContainer#findMember(IPath)
 */
public IResource findMember(IPath path, boolean phantom) {
	path = getFullPath().append(path);
	ResourceInfo info = workspace.getResourceInfo(path, phantom, false);
	return (info == null) ? null : workspace.newResource(path, info.getType());
}
/**
 */
protected void fixupAfterMoveSource() throws CoreException {
	if (!synchronizing(getResourceInfo(false, false)) || getType() == PROJECT) {
		workspace.deleteResource(this);
		return;
	}
	super.fixupAfterMoveSource();
	IResource[] members = members(true);
	for (int i = 0; i < members.length; i++)
		 ((Resource) members[i]).fixupAfterMoveSource();
}
protected IResource[] getChildren(Container parent, boolean phantom) {
	return getChildren(parent.getFullPath(), phantom);
}
protected IResource[] getChildren(IPath parentPath, boolean phantom) {
	IPath[] children = workspace.tree.getChildren(parentPath);
	if (children.length == 0)
		return new IResource[0];
	Resource[] result = new Resource[children.length];
	int j = 0;
	for (int i = 0; i < children.length; i++) {
		ResourceInfo info = workspace.getResourceInfo(children[i], phantom, false);
		if (info != null)
			result[j++] = workspace.newResource(children[i], info.getType());
	}
	if (j == result.length) {
		return result;
	} else {
		Resource[] trimmedResult = new Resource[j];
		System.arraycopy(result, 0, trimmedResult, 0, j);
		return trimmedResult;
	}
}
/** 
 * @see IContainer#getFile
 */
public IFile getFile(String name) {
	return (IFile)workspace.newResource(getFullPath().append(name), FILE);
}
/** 
 * @see IContainer#getFile
 */
public IFile getFile(IPath path) {
	return (IFile)workspace.newResource(getFullPath().append(path), FILE);
}
/** 
 * @see IContainer#getFolder
 */
public IFolder getFolder(String name) {
	return (IFolder)workspace.newResource(getFullPath().append(name), FOLDER);
}
/** 
 * @see IContainer#getFolder
 */
public IFolder getFolder(IPath path) {
	return (IFolder)workspace.newResource(getFullPath().append(path), FOLDER);
}
public boolean isLocal(int flags, int depth) {
	if (!super.isLocal(flags, depth))
		return false;
	if (depth == DEPTH_ZERO)
		return true;
	if (depth == DEPTH_ONE)
		depth = DEPTH_ZERO;
	// get the children via the workspace since we know that this
	// resource exists (it is local).
	IResource[] children = getChildren(this, false);
	for (int i = 0; i < children.length; i++)
		if (!children[i].isLocal(depth))
			return false;
	return true;
}
/**
 * @see IContainer#members
 */
public IResource[] members() throws CoreException {
	return members(false);
}
/**
 * @see IContainer#members(boolean)
 */
public IResource[] members(boolean phantom) throws CoreException {
	ResourceInfo info = getResourceInfo(phantom, false);
	checkExists(getFlags(info), true);
	return getChildren(this, phantom);
}
}
