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

package org.eclipse.ui.ide.undo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.internal.ide.undo.ResourceDescription;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * An AbstractCopyOrMoveResourcesOperation represents an undoable operation for
 * moving or copying one or more resources in the workspace. Clients may call
 * the public API from a background thread.
 * 
 * This class is not intended to be subclassed by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
abstract class AbstractCopyOrMoveResourcesOperation extends
		AbstractResourcesOperation {

	// Used when there are different destination names for each resource
	protected IPath[] destinationPaths = null;

	// Used when all resources are going to the same container (no name changes)
	protected IPath destination = null;

	/**
	 * Create an AbstractCopyOrMoveResourcesOperation that moves or copies all
	 * of the specified resources to the specified paths. The destination paths
	 * must include the names of the resources at their new location.
	 * 
	 * @param resources
	 *            the resources to be moved or copied
	 * @param destinationPaths
	 *            the destination paths for the resources, including the name to
	 *            be assigned to the resource at its new location.
	 * @param label
	 *            the label of the operation
	 */
	AbstractCopyOrMoveResourcesOperation(IResource[] resources,
			IPath[] destinationPaths, String label) {
		super(resources, label);
		this.destinationPaths = destinationPaths;
	}

	/**
	 * Create an AbstractCopyOrMoveResourcesOperation that moves or copies all
	 * of the specified resources to the same target location, using their
	 * existing names.
	 * 
	 * @param resources
	 *            the resources to be moved or copied
	 * @param destinationPath
	 *            the destination path for the resources, not including the name
	 *            of the new resource.
	 * @param label
	 *            the label of the operation
	 */
	AbstractCopyOrMoveResourcesOperation(IResource[] resources,
			IPath destinationPath, String label) {
		super(resources, label);
		destination = destinationPath;
	}

	/**
	 * Create an AbstractCopyOrMoveResourcesOperation whose destination is not
	 * yet specified.
	 * 
	 * @param resources
	 *            the resources to be modified
	 * @param label
	 *            the label of the operation
	 */
	AbstractCopyOrMoveResourcesOperation(IResource[] resources, String label) {
		super(resources, label);
	}

	/**
	 * Move or copy any known resources according to the destination parameters
	 * known by this operation. Store enough information to undo and redo the
	 * operation.
	 * 
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @param move
	 *            <code>true</code> if the operation is a move, and
	 *            <code>false</code> if it is a copy
	 * @throws CoreException
	 *             propagates any CoreExceptions thrown from the resources API
	 */
	protected void moveOrCopy(IProgressMonitor monitor, IAdaptable uiInfo,
			boolean move) throws CoreException {

		String progressMessage;
		if (move) {
			progressMessage = UndoMessages.AbstractResourcesOperation_MovingResources;
		} else {
			progressMessage = UndoMessages.AbstractResourcesOperation_CopyingResourcesProgress;
		}
		monitor.beginTask("", 2000); //$NON-NLS-1$
		monitor.setTaskName(progressMessage);
		IResource[] resourcesAtDestination = new IResource[resources.length];
		List overwrittenResources = new ArrayList();
		IPath[] newDestinationPaths = new IPath[resources.length];

		for (int i = 0; i < resources.length; i++) {
			// Record the original path so this can be undone
			newDestinationPaths[i] = resources[i].getFullPath();

			// Move or copy the resources and record the overwrites that would
			// be restored if this operation were reversed
			ResourceDescription[] overwrites;
			if (move) {
				overwrites = WorkspaceUndoUtil
						.move(resources[i],
								getDestinationPath(resources[i], i),
								new SubProgressMonitor(monitor,
										1000 / resources.length), uiInfo);
			} else {
				overwrites = WorkspaceUndoUtil
						.copy(new IResource[] { resources[i] },
								getDestinationPath(resources[i], i),
								new SubProgressMonitor(monitor,
										1000 / resources.length), uiInfo, true);
			}
			// Accumulate the overwrites into the full list
			for (int j = 0; j < overwrites.length; j++) {
				overwrittenResources.add(overwrites[i]);
			}
			// Record the resource in its new destination path
			resourcesAtDestination[i] = getWorkspace().getRoot().findMember(
					getDestinationPath(resources[i], i));
		}

		// Are there any previously overwritten resources to restore now?
		if (resourceDescriptions != null) {
			for (int i = 0; i < resourceDescriptions.length; i++) {
				if (resourceDescriptions[i] != null) {
					resourceDescriptions[i]
							.createResource(new SubProgressMonitor(monitor,
									1000 / resourceDescriptions.length));
				}
			}
		}

		// Reset resource descriptions to the just overwritten resources
		setResourceDescriptions((ResourceDescription[]) overwrittenResources
				.toArray(new ResourceDescription[overwrittenResources.size()]));

		// Reset the target resources to refer to the resources in their new
		// location. Note that the destination paths were reset to the original
		// location as we did the move.
		setTargetResources(resourcesAtDestination);

		// Reset the destination path to the new paths
		destinationPaths = newDestinationPaths;
		destination = null;

		monitor.done();
	}

	/**
	 * Compute the status for moving or copying the resources. A status severity
	 * of <code>OK</code> indicates that the copy or move is likely to be
	 * successful. A status severity of <code>ERROR</code> indicates that the
	 * operation is no longer valid. Other status severities are open to
	 * interpretation by the caller.
	 * 
	 * Note this method may be called on initial moving or copying of a
	 * resource, or when a move or copy is undone or redone. Therefore, this
	 * method should check conditions that can change over the life of the
	 * operation, such as whether the file to moved or copied exists, and
	 * whether the target location is still valid. One-time static checks should
	 * typically be done by the caller so that the user is not continually
	 * prompted or warned about conditions that were acceptable at the time of
	 * original execution and do not change over time.
	 * 
	 * @return the status indicating the projected outcome of moving or copying
	 *         the resources.
	 */
	protected IStatus computeMoveOrCopyStatus() {
		// Check for error conditions first so that we do not prompt the user
		// on warnings that eventually will not matter anyway.
		if (resources == null) {
			markInvalid();
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_NotEnoughInfo);
		}
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			// Does the resource still exist?
			if (!resource.exists()) {
				markInvalid();
				return getErrorStatus(UndoMessages.AbstractCopyOrMoveResourcesOperation_ResourceDoesNotExist);
			}

			// Are we really trying to move it to a different name?
			if (!isDestinationPathValid(resource, i)) {
				markInvalid();
				return getErrorStatus(UndoMessages.AbstractCopyOrMoveResourcesOperation_SameNameOrLocation);
			}
			// Is the proposed name valid?
			IStatus status = getWorkspace().validateName(
					getProposedName(resource, i), resource.getType());
			if (status.getSeverity() == IStatus.ERROR) {
				markInvalid();
			}
			if (!status.isOK()) {
				return status;
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Return the destination path that should be used to move or copy the
	 * specified resource. This path is relative to the workspace.
	 * 
	 * @param resource
	 *            the resource being moved or copied
	 * @param index
	 *            the integer index of the resource in the resource array
	 * @return the path specifying the destination for the resource
	 */
	protected IPath getDestinationPath(IResource resource, int index) {
		if (destinationPaths != null) {
			return destinationPaths[index];
		}
		return destination.append(resource.getName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#appendDescriptiveText(java.lang.StringBuffer)
	 */
	protected void appendDescriptiveText(StringBuffer text) {
		super.appendDescriptiveText(text);
		text.append(" destination: "); //$NON-NLS-1$
		text.append(destination);
		text.append(", destinationPaths: "); //$NON-NLS-1$
		text.append(destinationPaths);
		text.append('\'');
	}

	/**
	 * Return any resource that will be overwritten by moving or copying the
	 * specified resource to the destination recorded at the specified index.
	 * 
	 * @param resource
	 *            the resource to be moved or copied
	 * @param index
	 *            the index within the destination array, if applicable
	 * @return the resource that will be overwritten, or <code>null</code> if
	 *         no resource will be overwritten.
	 */
	protected IResource getOverwrittenResource(IResource resource, int index) {
		IPath proposedPath = getDestinationPath(resource, index);
		return getWorkspace().getRoot().findMember(proposedPath);
	}

	/*
	 * Move the project to its new location, returning its previous location.
	 */
	URI moveProject(IProject project, URI locationURI,
			IProgressMonitor monitor) throws CoreException {
		monitor
				.setTaskName(UndoMessages.AbstractCopyOrMoveResourcesOperation_moveProjectProgress);

		IProjectDescription description = project.getDescription();
		// Record the original path so this can be undone
		URI newDestinationURI = description.getLocationURI();
		// Set the new location into the project's description
		description.setLocationURI(locationURI);

		project.move(description, IResource.FORCE | IResource.SHALLOW, monitor);

		// Now adjust the projectLocation so this can be undone/redone.
		return newDestinationURI;
	}

	/*
	 * Copy the specified project, returning the handle of the copy.
	 */
	IProject copyProject(IProject project, IPath destinationPath,
			URI locationURI, IProgressMonitor monitor)
			throws CoreException {
		monitor
				.setTaskName(UndoMessages.AbstractCopyOrMoveResourcesOperation_copyProjectProgress);

		IProjectDescription description = project.getDescription();

		// Set the new name and location into the project's description
		description.setName(destinationPath.lastSegment());
		description.setLocationURI(locationURI);

		project.copy(description, IResource.FORCE | IResource.SHALLOW, monitor);

		// Now return the handle of the new project
		return (IProject) getWorkspace().getRoot().findMember(destinationPath);
	}

	/**
	 * Return a boolean indicating whether the proposed destination path for a
	 * resource is valid.
	 * 
	 * @param resource
	 *            the resource whose path is to be checked
	 * @param index
	 *            the integer index of the resource in the resource array
	 * @return a boolean indicating whether the destination path is valid
	 */
	protected boolean isDestinationPathValid(IResource resource, int index) {
		return !resource.getFullPath().equals(
				getDestinationPath(resource, index));
	}

	/**
	 * Return a string indicating the proposed name for the resource
	 * 
	 * @param resource
	 *            the resource whose path is to be checked
	 * @param index
	 *            the integer index of the resource in the resource array
	 * @return the string name of the resource
	 */
	protected String getProposedName(IResource resource, int index) {
		return getDestinationPath(resource, index).lastSegment();
	}
}
