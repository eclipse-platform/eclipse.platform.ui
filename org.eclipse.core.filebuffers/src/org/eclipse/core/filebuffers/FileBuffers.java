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
package org.eclipse.core.filebuffers;


import java.io.File;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;


/**
 * Facade for the file buffers plug-in. Provides access to the text file buffer
 * manager and helper methods for location handling. This facade is available
 * independent from the activation status of the file buffers plug-in.
 *
 * @since 3.0
 */
public final class FileBuffers {

	/**
	 * Cannot be instantiated.
	 */
	private FileBuffers()  {
	}

	/**
	 * Returns the text file buffer manager. May return <code>null</code> if
	 * the file buffers plug-in may no be activated. This is, for example, the
	 * case when the method is called on plug-in shutdown.
	 *
	 * @return the text file buffer manager or <code>null</code>
	 */
	public static ITextFileBufferManager getTextFileBufferManager()  {
		FileBuffersPlugin plugin= FileBuffersPlugin.getDefault();
		return plugin != null ? plugin.getFileBufferManager() : null;
	}

	/**
	 * Returns the workspace file at the given location or <code>null</code> if
	 * the location is not a valid location in the workspace.
	 *
	 * @param location the location
	 * @return the workspace file at the location or <code>null</code>
	 */
	public static IFile getWorkspaceFileAtLocation(IPath location) {
		IPath normalized= normalizeLocation(location);
		if (normalized.segmentCount() >= 2) {
			// @see IContainer#getFile for the required number of segments
			IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
			IFile file= workspaceRoot.getFile(normalized);
			if  (file != null && file.exists())
				return file;
		}
		return null;
	}

	/**
	 * Returns the normalized form of the given path or location.
	 * <p>
	 * The normalized form is defined as follows:
	 * </p>
	 * <ul>
	 * <li><b>Existing Workspace Files:</b> For a path or location for
	 * which there
	 * {@link org.eclipse.core.resources.IContainer#exists(org.eclipse.core.runtime.IPath) exists}
	 * a workspace file, the normalized form is that file's workspace
	 * relative, absolute path as returned by
	 * {@link IFile#getFullPath()}.</li>
	 * <li><b>Non-existing Workspace Files:</b> For a path to a
	 * non-existing workspace file, the normalized form is the
	 * {@link IPath#makeAbsolute() absolute} form of the path.</li>
	 * <li><b>External Files:</b> For a location for which there
	 * exists no workspace file, the normalized form is the
	 * {@link IPath#makeAbsolute() absolute} form of the location.</li>
	 * </ul>
	 * 
	 * @param pathOrLocation the path or location to be normalized
	 * @return the normalized form of <code>pathOrLocation</code>
	 */
	public static IPath normalizeLocation(IPath pathOrLocation) {
		IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();

		// existing workspace resources - this is the 93% case
		if (workspaceRoot.exists(pathOrLocation))
			return pathOrLocation.makeAbsolute();

		IFile file= workspaceRoot.getFileForLocation(pathOrLocation);
		// existing workspace resources referenced by their file system path
		// files that do not exist (including non-accessible files) do not pass
		if (file != null && file.exists())
			return file.getFullPath();

		// non-existing resources and external files
		return pathOrLocation.makeAbsolute();
	}

	/**
	 * Returns the file in the local file system for the given location.
	 * <p>
	 * The location is either a full path of a workspace resource or an
	 * absolute path in the local file system.
	 * </p>
	 *
	 * @param location the location
	 * @return the {@link File} in the local file system for the given location
	 */
	public static File getSystemFileAtLocation(IPath location) {
		if (location == null)
			return null;

		IFile file= getWorkspaceFileAtLocation(location);
		if (file != null) {
			IPath path= file.getLocation();
			return path.toFile();
		}

		return location.toFile();
	}
}
