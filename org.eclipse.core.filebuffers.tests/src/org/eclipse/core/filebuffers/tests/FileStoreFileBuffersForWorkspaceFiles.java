/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;

import org.osgi.framework.Bundle;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;

import org.eclipse.core.filebuffers.FileBuffers;

import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * FileBuffersForWorkspaceFiles
 */
public class FileStoreFileBuffersForWorkspaceFiles extends FileBufferFunctions {

	@Override
	protected IPath createPath(IProject project) throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "WorkspaceFile", "content");
		return file.getFullPath();
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#markReadOnly()
	 */
	@Override
	protected void setReadOnly(boolean state) throws Exception {
		IFile file= FileBuffers.getWorkspaceFileAtLocation(getPath());
		ResourceAttributes attributes= new ResourceAttributes();
		attributes.setReadOnly(state);
		file.setResourceAttributes(attributes);
	}

	@Override
	protected boolean isStateValidationSupported() {
		return true;
	}

	@Override
	protected boolean deleteUnderlyingFile() throws Exception {
		IFile file= FileBuffers.getWorkspaceFileAtLocation(getPath());
		file.delete(true, false, null);
		return file.exists();
	}

	@Override
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

	@Override
	protected boolean modifyUnderlyingFile() throws Exception {
		IFileStore fileStore= FileBuffers.getFileStoreAtLocation(getPath());
		assertTrue(fileStore.fetchInfo().exists());
		OutputStream out= fileStore.openOutputStream(EFS.NONE, null);
		try {
			out.write("Changed content of workspace file".getBytes());
			out.flush();
		} catch (IOException x) {
			fail();
		} finally {
			out.close();
		}
		IFileInfo fileInfo= fileStore.fetchInfo();
		fileInfo.setLastModified(1000);
		fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);
		IFile iFile= FileBuffers.getWorkspaceFileAtLocation(getPath());
		assertTrue(iFile.exists() && iFile.getFullPath().equals(getPath()));
		iFile.refreshLocal(IResource.DEPTH_INFINITE, null);
		return true;
	}

	@Override
	protected Class<IAnnotationModel> getAnnotationModelClass() throws Exception {
		Bundle bundle= Platform.getBundle("org.eclipse.ui.editors");
		return bundle != null ? IAnnotationModel.class : null;
	}
}
