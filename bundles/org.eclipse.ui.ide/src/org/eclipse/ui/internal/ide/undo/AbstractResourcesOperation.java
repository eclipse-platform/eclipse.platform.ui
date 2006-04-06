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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;

/**
 * An AbstractResourcesOperation knows how to perform a variety of
 * operations on the set of targeted resources, including move, copy,
 * rename, create, or delete.
 * 
 * @since 3.2
 * 
 */
public abstract class AbstractResourcesOperation extends
		AbstractWorkspaceOperation {

	/**
	 * Create a RenameResourceOperation
	 * 
	 * @param resources
	 *            the resources to be modified
	 * @param label
	 *            the label of the operation
	 */
	public AbstractResourcesOperation(IResource[] resources, String label) {
		super(label);
		setTargetResources(resources);
	}

	/*
	 * Return the first resource in the list of resources. Used by subclasses
	 * that only operate on a single resource.
	 */
	protected IResource getResource() {
		if (resources.length == 0) {
			return null;
		}
		return resources[0];
	}

	protected void rename(IResource resource, IPath newPath,
			IProgressMonitor monitor, IAdaptable uiInfo) throws CoreException {
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
	protected boolean validateEdit(IFile source, IFile destination, Shell shell) {
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
