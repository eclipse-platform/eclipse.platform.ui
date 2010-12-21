/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.File;
import java.net.URI;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

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
	/**
	 * If this root represents a resource in the local file system, this path
	 * represents the root location.  This value is null if the root represents
	 * a non-local file system
	 */
	private IPath localRoot = null;

	private URI root;

	/**
	 * Defines the root of a file system within the workspace tree.
	 * @param rootURI The virtual file representing the root of the file
	 * system that has been mounted
	 * @param workspacePath The workspace path at which this file
	 * system has been mounted
	 */
	FileStoreRoot(URI rootURI, IPath workspacePath) {
		Assert.isNotNull(rootURI);
		Assert.isNotNull(workspacePath);
		this.root = rootURI;
		this.chop = workspacePath.segmentCount();
		this.localRoot = toLocalPath(root);
	}

	private IPathVariableManager getManager(IPath workspacePath) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = workspaceRoot.findMember(workspacePath);
		if (resource != null)
			return resource.getPathVariableManager();
		return workspaceRoot.getFile(workspacePath).getPathVariableManager();
	}

	/**
	 * Returns the resolved, absolute file system location of the resource
	 * corresponding to the given workspace path, or null if none could
	 * be computed.
	 */
	public URI computeURI(IPath workspacePath) {
		IPath childPath = workspacePath.removeFirstSegments(chop);
		final URI rootURI = getManager(workspacePath).resolveURI(root);
		if (childPath.segmentCount() == 0)
			return rootURI;
		try {
			return EFS.getStore(rootURI).getChild(childPath).toURI();
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * Creates an IFileStore for a given workspace path.
	 * @exception CoreException If the file system for that resource is undefined
	 */
	IFileStore createStore(IPath workspacePath, IResource resource) throws CoreException {
		IPath childPath = workspacePath.removeFirstSegments(chop);
		IFileStore rootStore;
		final URI uri = resource.getPathVariableManager().resolveURI(root);
		if (!uri.isAbsolute()) {
			//handles case where resource location cannot be resolved
			//such as unresolved path variable or invalid file system scheme
			return EFS.getNullFileSystem().getStore(workspacePath);
		}
		rootStore = EFS.getStore(uri);
		if (childPath.segmentCount() == 0)
			return rootStore;
		return rootStore.getChild(childPath);
	}

	boolean isValid() {
		return isValid;
	}

	IPath localLocation(IPath workspacePath, IResource resource) {
		if (localRoot == null)
			return null;
		IPath location;
		if (workspacePath.segmentCount() <= chop)
			location = localRoot;
		else
			location = localRoot.append(workspacePath.removeFirstSegments(chop));
		location = resource.getPathVariableManager().resolvePath(location);
		
		// if path is still relative then path variable could not be resolved
		// if path is null, it means path variable refers to a non-local filesystem
		if (location == null || !location.isAbsolute())
			return null;
		return location;
	}

	void setValid(boolean value) {
		this.isValid = value;
	}

	/**
	 * Returns the local path for the given URI, or null if not possible.
	 */
	private IPath toLocalPath(URI uri) {
		try {
			final File localFile = EFS.getStore(uri).toLocalFile(EFS.NONE, null);
			return localFile == null ? null : new Path(localFile.getAbsolutePath());
		} catch (CoreException e) {
			return FileUtil.toPath(uri);
		}
	}
}
