/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.ui.ide.undo;

import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.undo.snapshot.IContainerSnapshot;
import org.eclipse.core.resources.undo.snapshot.IResourceSnapshot;
import org.eclipse.core.resources.undo.snapshot.ResourceSnapshotFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A CreateFileOperation represents an undoable operation for creating a file in
 * the workspace. If a link location is specified, the file is considered to
 * be linked to the file at the specified location. If a link location is not
 * specified, the file will be created in the location specified by the handle,
 * and the entire containment path of the file will be created if it does not
 * exist.  The file should not already exist, and the existence of the
 * containment path should not be changed between the time this operation
 * is created and the time it is executed.
 * <p>
 * Clients may call the public API from a background thread.
 * </p>
 * <p>
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.3
 */
public class CreateFileOperation extends AbstractCreateResourcesOperation {

	/**
	 * Create a CreateFileOperation
	 *
	 * @param fileHandle
	 *            the file to be created
	 * @param linkLocation
	 *            the location of the file if it is to be linked
	 * @param contents
	 *            the initial contents of the file, or null if there is to be no
	 *            contents
	 * @param label
	 *            the label of the operation
	 */
	public CreateFileOperation(IFile fileHandle, URI linkLocation,
			InputStream contents, String label) {
		super(null, label);
		IResourceSnapshot<? extends IResource> resourceDescription;
		if (fileHandle.getParent().exists()) {
			resourceDescription = ResourceSnapshotFactory.fromFileDetails(fileHandle, linkLocation, contents);
		} else {
			// must first ensure descriptions for the parent folders are
			// created
			IContainerSnapshot<? extends IContainer> containerDescription = ResourceSnapshotFactory
					.fromContainer(fileHandle.getParent());
			IResourceSnapshot<IFile> fileDescription = ResourceSnapshotFactory.fromFileDetails(fileHandle, linkLocation,
					contents);
			WorkspaceUndoUtil.getFirstLeafFolder(containerDescription).addMember(fileDescription);
			resourceDescription = containerDescription;
		}
		setResourceDescriptions(new IResourceSnapshot<?>[] { resourceDescription });
	}


	@Override
	public IStatus computeExecutionStatus(IProgressMonitor monitor) {
		IStatus status = super.computeExecutionStatus(monitor);
		if (status.isOK()) {
			// Overwrite is not allowed when we are creating a new file
			status = computeCreateStatus(false);
		}
		return status;
	}

}
