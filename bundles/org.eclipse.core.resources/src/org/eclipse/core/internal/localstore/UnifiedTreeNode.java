/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.util.Iterator;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IResource;

public class UnifiedTreeNode implements ILocalStoreConstants {
	protected IResource resource;
	protected UnifiedTreeNode child;
	protected UnifiedTree tree;
	protected IFileStore store;
	protected IFileInfo fileInfo;
	protected boolean existsWorkspace;

	public UnifiedTreeNode(UnifiedTree tree, IResource resource, IFileStore store, IFileInfo fileInfo, boolean existsWorkspace) {
		this.tree = tree;
		this.resource = resource;
		this.store = store;
		this.fileInfo = fileInfo;
		this.existsWorkspace = existsWorkspace;
	}

	public boolean existsInFileSystem() {
		return fileInfo != null && fileInfo.exists();
	}

	public boolean existsInWorkspace() {
		return existsWorkspace;
	}

	/**
	 * Returns an Enumeration of UnifiedResourceNode.
	 */
	public Iterator getChildren() {
		return tree.getChildren(this);
	}

	protected UnifiedTreeNode getFirstChild() {
		return child;
	}

	public long getLastModified() {
		if (fileInfo != null)
			return fileInfo.getLastModified();
		if (getStore() == null)
			return 0;
		fileInfo = store.fetchInfo();
		return fileInfo == null ? 0 : fileInfo.getLastModified();
	}

	public int getLevel() {
		return tree.getLevel();
	}

	/**
	 * Returns the local store of this resource.  May be null.
	 */
	public IFileStore getStore() {
		//initialize store lazily, because it is not always needed
		if (store == null)
			store = ((Resource)resource).getStore();
		return store;
	}

	/**
	 * Gets the name of this node in the local file system.
	 * @return Returns a String
	 */
	public String getLocalName() {
		return fileInfo == null ? null : fileInfo.getName();
	}

	public IResource getResource() {
		return resource;
	}

	public boolean isFolder() {
		if (fileInfo != null)
			return fileInfo.isDirectory();
		if (getStore() == null)
			return false;
		fileInfo = store.fetchInfo();
		return fileInfo == null ? false : fileInfo.isDirectory();
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

	public void removeChildrenFromTree() {
		tree.removeNodeChildrenFromQueue(this);
	}

	/**
	 * Reuses this object by assigning all new values for the fields.
	 */
	public void reuse(UnifiedTree aTree, IResource aResource, IFileStore aStore, IFileInfo info, boolean existsInWorkspace) {
		this.tree = aTree;
		this.child = null;
		this.resource = aResource;
		this.store = aStore;
		this.fileInfo = info;
		this.existsWorkspace = existsInWorkspace;
	}
}
