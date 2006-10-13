/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.undo;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * FileDescription is a lightweight description that describes a file to be
 * created.
 * 
 * This class is not intended to be instantiated or used by clients.
 * 
 * @since 3.3
 * 
 */
public class FileDescription extends ResourceDescription {

	String name;

	IPath location;

	private IFileState fileState;

	/**
	 * Create a FileDescription that can be used to later restore the given
	 * file. The file typically already exists, but this constructor will not
	 * fail if the file does not exist.
	 * 
	 * @param file
	 *            the file to be restored.
	 */
	public FileDescription(IFile file) {
		super(file);
		this.name = file.getName();
		if (file.isLinked()) {
			location = file.getLocation();
		}

	}

	/**
	 * Create a file description from the specified file handle. The handle does
	 * not exist, so no information should be derived from it. If a location
	 * path is specified, this file should represent a link to another location.
	 * The IFileState describes any state that should be used when the file
	 * resource is created.
	 * 
	 * @param file
	 *            the file to be described
	 * @param linkLocation
	 *            the location of the file's link, or <code>null</code> if the
	 *            file is not linked
	 * @param fileState
	 *            the file state that can be used to get information about the
	 *            file, such as its initial content
	 */
	public FileDescription(IFile file, IPath linkLocation, IFileState fileState) {
		super(file);
		this.name = file.getName();
		this.location = linkLocation;
		this.fileState = fileState;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.undo.ResourceDescription#recordLastHistory(org.eclipse.core.resources.IResource, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void recordLastHistory(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		Assert.isLegal(resource.getType() == IResource.FILE);

		if (location != null) {
			// file is linked, no need to record any history
			return;
		}
		IFileState[] states = ((IFile) resource).getHistory(monitor);
		if (states.length > 0) {
			this.fileState = states[0];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.ide.undo.ResourceDescription#createResourceHandle()
	 */
	public IResource createResourceHandle() {
		IWorkspaceRoot workspaceRoot = parent.getWorkspace().getRoot();
		IPath filePath = parent.getFullPath().append(name);
		return workspaceRoot.getFile(filePath);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.undo.ResourceDescription#createExistentResourceFromHandle(org.eclipse.core.resources.IResource, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void createExistentResourceFromHandle(IResource resource,
			IProgressMonitor monitor) throws CoreException {

		Assert.isLegal(resource instanceof IFile);
		if (resource.exists()) {
			return;
		}
		IFile fileHandle = (IFile) resource;
		monitor.beginTask(UndoMessages.FileDescription_NewFileProgress, 200);
		try {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (location != null) {
				fileHandle.createLink(location, IResource.ALLOW_MISSING_LOCAL,
						new SubProgressMonitor(monitor, 200));
			} else {
				InputStream contents = new ByteArrayInputStream(
						UndoMessages.FileDescription_ContentsCouldNotBeRestored.getBytes());
				String charset = null;
				// Retrieve the contents and charset from the file state.
				// Other file state attributes, such as timestamps, have
				// already been retrieved from the original IResource object
				// and are restored in the superclass.
				if (fileState != null && fileState.exists()) {
					contents = fileState.getContents();
					charset = fileState.getCharset();
				}
				fileHandle.create(contents, false, new SubProgressMonitor(
						monitor, 100));
				fileHandle.setCharset(charset, new SubProgressMonitor(monitor,
						100));
			}
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		} finally {
			monitor.done();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.undo.ResourceDescription#isValid()
	 */
	public boolean isValid() {
		return super.isValid() && fileState != null && fileState.exists();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.undo.ResourceDescription#getName()
	 */
	public String getName() {
		return name;
	}
}