/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.filebuffers.FileBuffers;

/**
 * FileBuffersForWorkspaceFiles
 */
public class FileBuffersForWorkspaceFiles extends FileBufferFunctions {
	
	
	private static class ResourceListener implements IResourceChangeListener {
		public boolean fFired= false;
		public IPath fPath;
		
		public ResourceListener(IPath path) {
			fPath= path;
		}
		
		public void resourceChanged(IResourceChangeEvent event) {
			if (!fFired) {
				IResourceDelta delta= event.getDelta();
				if (delta != null) {
					delta= delta.findMember(fPath);
					if (delta != null)
						fFired= IResourceDelta.CHANGED == delta.getKind() && (IResourceDelta.CONTENT & delta.getFlags()) != 0;
				}
			}
		}
	}
	
	protected IPath createPath(IProject project) throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "WorkspaceFile", "content");
		return file.getFullPath();
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#markReadOnly()
	 */
	protected void markReadOnly() throws Exception {
		IFile file= FileBuffers.getWorkspaceFileAtLocation(getPath());
		file.setReadOnly(true);
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#isStateValidationSupported()
	 */
	protected boolean isStateValidationSupported() {
		return true;
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#deleteUnderlyingFile()
	 */
	protected boolean deleteUnderlyingFile() throws Exception {
		IFile file= FileBuffers.getWorkspaceFileAtLocation(getPath());
		file.delete(true, false, null);
		return file.exists();
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#moveUnderlyingFile()
	 */
	protected IPath moveUnderlyingFile() throws Exception {
		IFile file= FileBuffers.getWorkspaceFileAtLocation(getPath());
		ResourceHelper.createFolder("project/folderA/folderB/folderC");
		IPath path= new Path("/project/folderA/folderB/folderC/MovedWorkspaceFile");
		file.move(path, true, false, null);
		
		file= FileBuffers.getWorkspaceFileAtLocation(path);
		if (file != null && file.exists())
			return path;
		
		return null;
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#modifyUnderlyingFile()
	 */
	protected boolean modifyUnderlyingFile() throws Exception {
		File file= FileBuffers.getSystemFileAtLocation(getPath());
		FileTool.write(file.getAbsolutePath(), new StringBuffer("Changed content of workspace file"));
		IFile iFile= FileBuffers.getWorkspaceFileAtLocation(getPath());
		
		ResourceListener listener= new ResourceListener(iFile.getFullPath());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
		try {
			iFile.refreshLocal(IResource.DEPTH_INFINITE, null);
			return receivedNotification(listener);
		} finally {
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
			listener= null;
		}
	}
	
	private boolean receivedNotification(ResourceListener listener) {
		for (int i= 0; i < 5; i++) {
			if (listener.fFired)
				return true;
			try {
				Thread.sleep(1000); // sleep a second
			} catch (InterruptedException e) {
			}
		}
		return false;
	}
}
