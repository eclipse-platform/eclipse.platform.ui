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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

/**
 * @since 3.2
 * 
 */
public abstract class RenameResourceOperation extends
		AbstractResourcesOperation {

	String previousName;

	/**
	 * Create a RenameResourceOperation
	 * 
	 * @param resource
	 *            the resource to be named
	 * @param newName
	 *            the new name of the resource.
	 * @param label
	 *            the label of the operation
	 */
	public RenameResourceOperation(IResource resource, String newName,
			String label) {
		super(new IResource[] { resource }, label);
		previousName = newName;
	}

	protected void doExecute(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		swapName(monitor, uiInfo);
	}

	protected void doUndo(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		swapName(monitor, uiInfo);
	}

	private IPath getProposedPath() {
		return getResource().getFullPath().removeLastSegments(1).append(
				previousName);
	}

	private void swapName(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		IResource resource = getResource();
		String currentName = resource.getName();
		rename(resource, getProposedPath(), monitor, uiInfo);
		previousName = currentName;
	}

	protected boolean updateResourceChangeDescriptionFactory(
			IResourceChangeDescriptionFactory factory, boolean undo) {
		IResource resource = getResource();
		IPath newPath = getProposedPath();
		factory.move(resource, newPath);
		return true;
	}

	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = computeRenameStatus();
		if (status.isOK()) {
			return super.computeUndoableStatus(monitor);
		}
		return status;
	}

	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = computeRenameStatus();
		if (status.isOK()) {
			return super.computeRedoableStatus(monitor);
		}
		return status;
	}

	private IStatus computeRenameStatus() {
		IResource resource = getResource();
		// Does the resource still exist?
		if (!resource.exists()) {
			return getErrorStatus(UndoMessages.RenameResourceOperation_ResourceDoesNotExist);
		}
		// Are we really trying to rename it a different name?
		IPath proposedPath = getProposedPath();
		if (resource.getFullPath().equals(proposedPath)) {
			return getErrorStatus(UndoMessages.RenameResourceOperation_SameName);
		}
		// Is the proposed name valid?
		IStatus status = getWorkspace().validateName(previousName,
				resource.getType());
		if (!status.isOK()) {
			return status;
		}
		// Is the resource read only?
		if (resource.getResourceAttributes().isReadOnly()) {
			return getWarningStatus(NLS.bind(
					UndoMessages.RenameResourceOperation_ReadOnly, resource
							.getName()), 0);
		}

		// Does the newly named resource already exist. If so, we could
		// overwrite
		IResource newResource = getWorkspace().getRoot().findMember(
				proposedPath);
		if (newResource == null) {
			return getWarningStatus(NLS.bind(
					UndoMessages.RenameResourceOperation_ResourceAlreadyExists,
					proposedPath.toString()), 0);
		}
		return Status.OK_STATUS;
	}


}
