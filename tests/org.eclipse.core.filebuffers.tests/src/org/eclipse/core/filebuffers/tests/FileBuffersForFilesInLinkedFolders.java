/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.After;
import org.osgi.framework.Bundle;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;

import org.eclipse.core.filebuffers.FileBuffers;

import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * FileBuffersForLinkedFiles
 */
public class FileBuffersForFilesInLinkedFolders extends FileBufferFunctions {

	private File fExternalFile;

	@Override
	@After
	public void tearDown() {
		FileTool.delete(getPath());
		File file= fExternalFile;
		FileTool.delete(file); // externalResources/linkedFolderTarget/FileInLinkedFolder
		file= file.getParentFile();
		FileTool.delete(file); // externalResources/linkedFolderTarget
		file= file.getParentFile();
		FileTool.delete(file); // externalResources/
		super.tearDown();
	}

	@Override
	protected IPath createPath(IProject project) throws Exception {
		File sourceFile= FileTool.getFileInPlugin(FileBuffersTestPlugin.getDefault(), IPath.fromOSString("testResources/linkedFolderTarget/FileInLinkedFolder"));
		fExternalFile= FileTool.createTempFileInPlugin(FileBuffersTestPlugin.getDefault(), IPath.fromOSString("externalResources/linkedFolderTarget/FileInLinkedFolder"));
		FileTool.copy(sourceFile, fExternalFile);

		IFolder folder= ResourceHelper.createLinkedFolder(project, IPath.fromOSString("LinkedFolder"), fExternalFile.getParentFile());
		IFile file= folder.getFile(IPath.fromOSString("FileInLinkedFolder"));
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
		ResourceHelper.createFolder("project/folderA");
		IPath path= IPath.fromOSString("/project/folderA/MovedLinkedFile");
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
		try (OutputStream out= fileStore.openOutputStream(EFS.NONE, null)) {
			out.write("Changed content of file in linked folder".getBytes());
			out.flush();
		} catch (IOException x) {
			fail();
		}
		IFileInfo fileInfo= fileStore.fetchInfo();
		fileInfo.setLastModified(1000);
		fileStore.putInfo(fileInfo, EFS.SET_LAST_MODIFIED, null);


		IFile iFile= FileBuffers.getWorkspaceFileAtLocation(getPath());
		iFile.refreshLocal(IResource.DEPTH_INFINITE, null);
		return true;
	}

	@Override
	protected Class<IAnnotationModel> getAnnotationModelClass() throws Exception {
		Bundle bundle= Platform.getBundle("org.eclipse.ui.editors");
		return bundle != null ? IAnnotationModel.class : null;
	}
}
