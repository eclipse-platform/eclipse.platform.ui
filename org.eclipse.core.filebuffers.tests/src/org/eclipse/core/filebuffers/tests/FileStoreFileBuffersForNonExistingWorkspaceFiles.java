/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * FileBuffersForNonExistingWorkspaceFiles
 */
public class FileStoreFileBuffersForNonExistingWorkspaceFiles extends FileStoreFileBufferFunctions {

	@Override
	@After
	public void tearDown() {
		FileTool.delete(getPath());
		super.tearDown();
	}

	@Override
	protected IPath createPath(IProject project) throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IPath filePath= folder.getLocation().append("NonExistingWorkspaceFile");
		return filePath.makeAbsolute();
	}

	@Test
	public void testBug118199() throws Exception {
		IFile file= getProject().getWorkspace().getRoot().getFile(getPath());
		assertFalse(file.exists());
		fManager.connect(getPath(), LocationKind.NORMALIZE, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(getPath(), LocationKind.NORMALIZE);
			buffer.getDocument().set("test");
			buffer.commit(null, false);
		} finally {
			fManager.disconnect(getPath(), LocationKind.NORMALIZE, null);
		}
		assertFalse(file.exists());
	}

	@Test
	public void testBug118199_fixed() throws Exception {
		IFile file= getProject().getWorkspace().getRoot().getFileForLocation(getPath());
		IPath path= file.getFullPath();
		assertFalse(file.exists());
		fManager.connect(path, LocationKind.IFILE, null);
		try {
			ITextFileBuffer buffer= fManager.getTextFileBuffer(path, LocationKind.IFILE);
			buffer.getDocument().set("test");
			buffer.commit(null, false);
		} finally {
			fManager.disconnect(path, LocationKind.IFILE, null);
		}
		assertTrue(file.exists());
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#markReadOnly()
	 */
	@Override
	protected void setReadOnly(boolean state) throws Exception {
		IFileStore fileStore= FileBuffers.getFileStoreAtLocation(getPath());
		assertNotNull(fileStore);
		fileStore.fetchInfo().setAttribute(EFS.ATTRIBUTE_READ_ONLY, state);
	}

	@Override
	protected boolean isStateValidationSupported() {
		return false;
	}

	@Override
	protected boolean deleteUnderlyingFile() throws Exception {
		return false;
	}

	@Override
	protected IPath moveUnderlyingFile() throws Exception {
		return null;
	}

	@Override
	protected boolean modifyUnderlyingFile() throws Exception {
		return false;
	}

	@Override
	protected Class<IAnnotationModel> getAnnotationModelClass() throws Exception {
		return null;
	}
}
