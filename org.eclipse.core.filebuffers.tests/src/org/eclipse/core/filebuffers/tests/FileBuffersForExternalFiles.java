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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileStoreConstants;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IProject;

import org.eclipse.core.filebuffers.FileBuffers;

/**
 * FileBuffersForExternalFiles
 */
public class FileBuffersForExternalFiles extends FileBufferFunctions {
	
	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#tearDown()
	 */
	protected void tearDown() throws Exception {
		FileTool.delete(getPath());
		FileTool.delete(FileBuffers.getSystemFileAtLocation(getPath()).getParentFile());
		super.tearDown();
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#createPath(org.eclipse.core.resources.IProject)
	 */
	protected IPath createPath(IProject project) throws Exception {
		File sourceFile= FileTool.getFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("testResources/ExternalFile"));
		File externalFile= FileTool.createTempFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("externalResources/ExternalFile"));
		FileTool.copy(sourceFile, externalFile);
		return new Path(externalFile.getAbsolutePath());
	}
	
	protected void setReadOnly(boolean state) throws Exception {
		IFileStore fileStore= FileBuffers.getFileStoreAtLocation(getPath());
		assertNotNull(fileStore);
		fileStore.fetchInfo().setAttribute(IFileStoreConstants.ATTRIBUTE_READ_ONLY, state);
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#isStateValidationSupported()
	 */
	protected boolean isStateValidationSupported() {
		return false;
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#deleteUnderlyingFile()
	 */
	protected boolean deleteUnderlyingFile() throws Exception {
		return false;
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#moveUnderlyingFile()
	 */
	protected IPath moveUnderlyingFile() throws Exception {
		return null;
	}

	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#modifyUnderlyingFile()
	 */
	protected boolean modifyUnderlyingFile() throws Exception {
		return false;
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#getAnnotationModelClass()
	 */
	protected Class getAnnotationModelClass() throws Exception {
		return null;
	}
}
