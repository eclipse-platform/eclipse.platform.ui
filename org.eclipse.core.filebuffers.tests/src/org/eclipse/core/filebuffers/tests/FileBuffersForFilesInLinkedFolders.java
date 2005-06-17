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
package org.eclipse.core.filebuffers.tests;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.filebuffers.FileBuffers;

import org.eclipse.jface.text.source.IAnnotationModel;

import org.osgi.framework.Bundle;

/**
 * FileBuffersForLinkedFiles
 */
public class FileBuffersForFilesInLinkedFolders extends FileBufferFunctions {
	
	private File fExternalFile;
	
	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#tearDown()
	 */
	protected void tearDown() throws Exception {
		File file= FileBuffers.getSystemFileAtLocation(getPath());
		FileTool.delete(file);
		file= fExternalFile;
		FileTool.delete(file); // externalResources/linkedFolderTarget/FileInLinkedFolder
		file= file.getParentFile();
		FileTool.delete(file); // externalResources/linkedFolderTarget
		file= file.getParentFile();
		FileTool.delete(file); // externalResources/
		super.tearDown();
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#createPath(org.eclipse.core.resources.IProject)
	 */
	protected IPath createPath(IProject project) throws Exception {
		File sourceFile= FileTool.getFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("testResources/linkedFolderTarget/FileInLinkedFolder"));
		fExternalFile= FileTool.createTempFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("externalResources/linkedFolderTarget/FileInLinkedFolder"));
		FileTool.copy(sourceFile, fExternalFile);
		
		IFolder folder= ResourceHelper.createLinkedFolder(project, new Path("LinkedFolder"), fExternalFile.getParentFile());
		IFile file= folder.getFile(new Path("FileInLinkedFolder"));
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
		ResourceHelper.createFolder("project/folderA");
		IPath path= new Path("/project/folderA/MovedLinkedFile");
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
		FileTool.write(file.getAbsolutePath(), new StringBuffer("Changed content of file in linked folder"));
		file.setLastModified(1000);
		IFile iFile= FileBuffers.getWorkspaceFileAtLocation(getPath());
		iFile.refreshLocal(IResource.DEPTH_INFINITE, null);
		return true;
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#getAnnotationModelClass()
	 */
	protected Class getAnnotationModelClass() throws Exception {
		Bundle bundle= Platform.getBundle("org.eclipse.ui.editors");
		return bundle != null ? IAnnotationModel.class : null;
	}
}
