/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import java.io.FileNotFoundException;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

/**
 * FileBuffersForWorkspaceFiles
 */
public class FileBuffersForNonAccessibleWorkspaceFiles extends FileBufferFunctions {
	
	protected void setUp() throws Exception {
		super.setUp();
		getProject().close(null);
	}
	
	protected IPath createPath(IProject project) throws Exception {
		IFolder folder= ResourceHelper.createFolder("project/folderA/folderB/");
		IFile file= ResourceHelper.createFile(folder, "WorkspaceFile", "content");
		return file.getFullPath();
	}

	
	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#tearDown()
	 */
	protected void tearDown() throws Exception {
		File file= FileBuffers.getSystemFileAtLocation(getPath());
		FileTool.delete(file);
		super.tearDown();
	}
	
	/*
	 * @see org.eclipse.core.filebuffers.tests.FileBufferFunctions#markReadOnly()
	 */
	protected void markReadOnly() throws Exception {
		File file= FileBuffers.getSystemFileAtLocation(getPath());
		file.setReadOnly();
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
	
	public void test7() throws Exception {
		try {
			super.test7();
			fail();
		} catch (CoreException e) {
			ensureFileNotFoundError(e);
		}
	}
	
	public void test11_1() throws Exception {
		try {
			super.test11_1();
			fail();
		} catch (CoreException e) {
			ensureFileNotFoundError(e);
		}
	}

	public void test17_3() throws Exception {
		try {
			super.test17_3();
			fail();
		} catch (CoreException e) {
			ensureFileNotFoundError(e);
		}
	}

	private void ensureFileNotFoundError(CoreException e) {
		IStatus status= e.getStatus();
		assertEquals(FileBuffersPlugin.PLUGIN_ID, status.getPlugin());
		assertEquals(IStatus.ERROR, status.getSeverity());
		assertEquals(IStatus.OK, status.getCode());
		Throwable cause= status.getException();
		assertNotNull(cause);
		assertTrue(cause instanceof FileNotFoundException);
	}
}
