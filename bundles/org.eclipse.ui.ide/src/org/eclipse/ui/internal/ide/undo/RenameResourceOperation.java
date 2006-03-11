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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 * 
 */
public abstract class RenameResourceOperation extends
		AbstractWorkspaceOperation {

	String previousName;

	/**
	 * Create a RenameResourceOperation
	 * 
	 * @param resource
	 *            the resource to be named
	 * @param newName
	 *            the new name of the resource.
	 * @param modelProviderIds
	 * @param label
	 *            the label of the operation
	 */
	public RenameResourceOperation(IResource resource, String newName,
			String label) {
		super(label);
		previousName = newName;
		setTargetResources(new IResource[] { resource });
	}

	private IResource getResource() {
		return resources[0];
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
		IPath newPath = getProposedPath();
		monitor.beginTask(getLabel(), 100);
		IWorkspaceRoot workspaceRoot = resource.getWorkspace().getRoot();

		IResource newResource = workspaceRoot.findMember(newPath);
		// If it already exists, we must overwrite it.
		if (newResource != null) {
			if (resource.getType() == IResource.FILE
					&& newResource.getType() == IResource.FILE) {
				IFile file = (IFile) resource;
				IFile newFile = (IFile) newResource;
				if (validateEdit(file, newFile, getShell(uiInfo))) {
					IProgressMonitor subMonitor = new SubProgressMonitor(
							monitor, 50);
					newFile.setContents(file.getContents(),
							IResource.KEEP_HISTORY, subMonitor);
					file.delete(IResource.KEEP_HISTORY, subMonitor);
				}
				monitor.worked(100);
				return;
			}
			newResource.delete(IResource.KEEP_HISTORY, new SubProgressMonitor(
					monitor, 50));
		}
		if (resource.getType() == IResource.PROJECT) {
			IProject project = (IProject) resource;
			IProjectDescription description = project.getDescription();
			description.setName(newPath.segment(0));
			project.move(description, IResource.FORCE | IResource.SHALLOW,
					monitor);
		} else {
			resource.move(newPath, IResource.KEEP_HISTORY | IResource.SHALLOW,
					new SubProgressMonitor(monitor, 50));
		}
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

	/**
	 * Validates the destination file if it is read-only and additionally the
	 * source file if both are read-only. Returns true if both files could be
	 * made writeable.
	 * 
	 * @param source
	 *            source file
	 * @param destination
	 *            destination file
	 * @param shell
	 *            ui context for the validation
	 * @return boolean <code>true</code> both files could be made writeable.
	 *         <code>false</code> either one or both files were not made
	 *         writeable
	 */
	boolean validateEdit(IFile source, IFile destination, Shell shell) {
		if (destination.isReadOnly()) {
			IWorkspace workspace = getWorkspace();
			IStatus status;
			if (source.isReadOnly()) {
				status = workspace.validateEdit(new IFile[] { source,
						destination }, shell);
			} else {
				status = workspace.validateEdit(new IFile[] { destination },
						shell);
			}
			return status.isOK();
		}
		return true;
	}
}
