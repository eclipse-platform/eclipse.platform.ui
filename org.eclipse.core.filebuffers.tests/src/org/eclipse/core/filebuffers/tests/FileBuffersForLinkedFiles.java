/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.junit.After;
import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;

import org.eclipse.core.filebuffers.FileBuffers;

import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * FileBuffersForLinkedFiles
 */
public class FileBuffersForLinkedFiles extends FileBufferFunctions {

	private File fExternalFile;

	@Override
	@After
	public void tearDown() {
		FileTool.delete(fExternalFile);
		FileTool.delete(fExternalFile.getParentFile());
		super.tearDown();
	}

	@Override
	protected IPath createPath(IProject project) throws Exception {
		File sourceFile= FileTool.getFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("testResources/LinkedFileTarget"));
		fExternalFile= FileTool.createTempFileInPlugin(FileBuffersTestPlugin.getDefault(), new Path("externalResources/LinkedFileTarget"));
		FileTool.copy(sourceFile, fExternalFile);
		IFile file= ResourceHelper.createLinkedFile(project, new Path("LinkedFile"), fExternalFile);
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
		IPath path= new Path("/project/folderA/MovedLinkedFile");
		file.move(path, true, false, null);

		file= FileBuffers.getWorkspaceFileAtLocation(path);
		if (file != null && file.exists())
			return path;

		return null;
	}

	@Override
	protected boolean modifyUnderlyingFile() throws Exception {
		FileTool.write(fExternalFile.getAbsolutePath(), new StringBuffer("Changed content of linked file"));
		fExternalFile.setLastModified(1000);
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
