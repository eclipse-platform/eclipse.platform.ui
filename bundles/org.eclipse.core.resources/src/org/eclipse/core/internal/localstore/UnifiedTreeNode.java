package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.internal.resources.*;
import java.util.Enumeration;

public class UnifiedTreeNode implements ILocalStoreConstants {
	protected IResource resource;
	protected UnifiedTreeNode child;
	protected UnifiedTree tree;
	protected long stat;
	protected boolean existsWorkspace;
	//the name of the resource in the local file system, if any
	protected String localName;
public UnifiedTreeNode(UnifiedTree tree, IResource resource, long stat, String localName, boolean existsWorkspace) {
	this.tree = tree;
	setResource(resource);
	this.stat = stat;
	this.existsWorkspace = existsWorkspace;
	this.localName = localName;
}
public boolean existsInFileSystem() {
	return isFile() || isFolder();
}
public boolean existsInWorkspace() {
	return existsWorkspace;
}
/**
 * Returns an Enumeration of UnifiedResourceNode.
 */
public Enumeration getChildren() throws CoreException {
	return tree.getChildren(this);
}
protected UnifiedTreeNode getFirstChild() {
	return child;
}
public long getLastModified() {
	return CoreFileSystemLibrary.getLastModified(stat);
}
public int getLevel() {
	return tree.getLevel();
}
public IPath getLocalLocation() {
	return tree.getLocalLocation(resource);
}
/**
 * Gets the name of this node in the local filesystem.
 * @return Returns a String
 */
public String getLocalName() {
	return localName;
}
public IResource getResource() {
	return resource;
}
public boolean isFile() {
	return CoreFileSystemLibrary.isFile(stat);
}
public boolean isFolder() {
	return CoreFileSystemLibrary.isFolder(stat);
}
public boolean isReadOnly() {
	return CoreFileSystemLibrary.isReadOnly(stat);
}
public void setExistsWorkspace(boolean exists) {
	this.existsWorkspace = exists;
}
protected void setFirstChild(UnifiedTreeNode child) {
	this.child = child;
}
public void setResource(IResource resource) {
	this.resource = resource;
}
public String toString() {
	String s = resource == null ? "null" : resource.getFullPath().toString();
	return "Node: " + s;
}
public void removeChildrenFromTree() throws CoreException {
	tree.removeNodeChildrenFromQueue(this);
}
}
