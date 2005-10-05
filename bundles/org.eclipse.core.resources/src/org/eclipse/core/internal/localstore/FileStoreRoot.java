/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.localstore;

import java.net.URI;
import org.eclipse.core.filesystem.FileSystemCore;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Represents the root of a file system that is connected to the workspace.
 * A file system can be rooted on any resource.
 */
public class FileStoreRoot {
	private int chop;
	/**
	 * When a root is changed, the old root object is marked invalid
	 * so that other resources with a cache of the root will know they need to update.
	 */
	private boolean isValid = true;
	private URI root;

	private final IPathVariableManager variableManager;

	/**
	 * Defines the root of a file system within the workspace tree.
	 * @param rootURI The virtual file representing the root of the file
	 * system that has been mounted
	 * @param workspacePath The workspace path at which this file
	 * system has been mounted
	 */
	FileStoreRoot(URI rootURI, IPath workspacePath) {
		this.variableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		this.root = rootURI;
		this.chop = workspacePath.segmentCount();
	}

	IFileStore createStore(IPath workspacePath) {
		IPath childPath = workspacePath.removeFirstSegments(chop);
		IFileStore rootStore;
		try {
			rootStore = FileSystemCore.getStore(variableManager.resolveURI(root));
		} catch (CoreException e) {
			//handles case where resource location cannot be resolved
			//such as unresolved path variable or invalid file system scheme
			return FileSystemCore.getNullFileSystem().getStore(workspacePath);
		}
		if (childPath.segmentCount() == 0)
			return rootStore;
		return rootStore.getChild(childPath);
	}

	boolean isValid() {
		return isValid;
	}

	void setValid(boolean value) {
		this.isValid = value;
	}
}