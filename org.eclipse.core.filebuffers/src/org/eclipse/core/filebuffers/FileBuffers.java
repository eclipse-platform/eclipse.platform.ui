/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.core.filebuffers;

import java.io.File;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Facade for the file buffers plug-in. Provides access to the
 * text file buffer manager.
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
	 * the file buffers plug-in may no be activated.
	 * 
	 * @return the text file buffer manager
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
	 * Returns a copy of the given location in a normalized form.
	 * 
	 * @param location the location to be normalized
	 * @return normalized copy of location
	 */
	public static IPath normalizeLocation(IPath location) {
		IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects= workspaceRoot.getProjects();
		
		for (int i= 0, length= projects.length; i < length; i++) {
			IPath path= projects[i].getLocation();
			if (path != null && path.isPrefixOf(location)) {
				IPath filePath= location.removeFirstSegments(path.segmentCount());
				filePath= projects[i].getFullPath().append(filePath);
				return filePath.makeAbsolute();
			}
			
		}
		return location.makeAbsolute();
	}
	
	/**
	 * Returns the file in the local file system for the given location.
	 * <p>
	 * The location is either a full path of a workspace resource or an
	 * absolute path in the local file system.
	 * </p>
	 * 
	 * @param location
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
