/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.localstore;

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
	
	//the location of the resource in the local file system, if any
	protected String localLocation;
	protected String localName;
	
public UnifiedTreeNode(UnifiedTree tree, IResource resource, long stat, String localLocation, String localName, boolean existsWorkspace) {
	this.tree = tree;
	this.resource = resource;
	this.stat = stat;
	this.existsWorkspace = existsWorkspace;
	this.localLocation = localLocation;
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
/**
 * Returns the local location of this resource.
 */
public String getLocalLocation() {
	return localLocation != null ? localLocation : tree.getLocalLocation(resource);
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
	String s = resource == null ? "null" : resource.getFullPath().toString(); //$NON-NLS-1$
	return "Node: " + s; //$NON-NLS-1$
}
public void removeChildrenFromTree() throws CoreException {
	tree.removeNodeChildrenFromQueue(this);
}
/**
 * Reuses this object by assigning all new values for the fields.
 */
public void reuse(UnifiedTree tree, IResource resource, long stat, String localLocation, String localName, boolean existsWorkspace) {
	this.tree = tree;
	this.child = null;
	this.resource = resource;
	this.stat = stat;
	this.existsWorkspace = existsWorkspace;
	this.localLocation = localLocation;
	this.localName = localName;
}
}