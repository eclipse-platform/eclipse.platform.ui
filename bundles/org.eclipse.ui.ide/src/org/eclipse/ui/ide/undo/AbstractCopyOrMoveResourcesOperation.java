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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ui.internal.ide.undo.ResourceDescription;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * An AbstractCopyOrMoveResourcesOperation represents an undoable operation for
 * moving or copying one or more resources in the workspace.
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
public abstract class AbstractCopyOrMoveResourcesOperation extends
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
		IResource[] resourcesInNewLocations = new IResource[resources.length];
		List overwrittenResources = new ArrayList();
		IPath[] newDestinationPaths = new IPath[resources.length];

		for (int i = 0; i < resources.length; i++) {
			// Record the original path so this can be undone
			if (resources[i].getType() == IResource.PROJECT) {
				URI locationURI = ((IProject) resources[i]).getDescription()
						.getLocationURI();
				if (locationURI == null) {
					newDestinationPaths[i] = Platform.getLocation().append(
							resources[i].getName());
				} else {
					newDestinationPaths[i] = new Path(locationURI.getPath())
							.append(resources[i].getName());
				}
			} else {
				newDestinationPaths[i] = resources[i].getFullPath();
			}
			// Move or copy the resources and record the overwrites that would
			// be restored if this operation were reversed
			ResourceDescription[] overwrites;
			if (move) {
				overwrites = move(resources[i], getDestinationPath(
						resources[i], i, true), new SubProgressMonitor(monitor,
						1000 / resources.length), uiInfo);
			} else {
				overwrites = copy(
						new IResource[] { resources[i] },
						getDestinationPath(resources[i], i, true),
						new SubProgressMonitor(monitor, 1000 / resources.length),
						uiInfo, true);
			}
			// Accumulate the overwrites into the full list
			for (int j = 0; j < overwrites.length; j++) {
				overwrittenResources.add(overwrites[i]);
			}
			// Record the resource in its new location
			resourcesInNewLocations[i] = getWorkspace().getRoot().findMember(
					getDestinationPath(resources[i], i, false));
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
		setTargetResources(resourcesInNewLocations);

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
		if (resources == null || !hasDestinationPath()) {
			markInvalid();
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_NotEnoughInfo);
		}
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			// Does the resource still exist?
			if (!resource.exists()) {
				markInvalid();
				return getErrorStatus(UndoMessages.MoveOrCopyResourceOperation_ResourceDoesNotExist);
			}
			// Are we really trying to move it to a different name?
			IPath proposedPath = getDestinationPath(resource, i, false);
			if (resource.getFullPath().equals(proposedPath)) {
				markInvalid();
				return getErrorStatus(UndoMessages.MoveOrCopyResourceOperation_SameNameOrLocation);
			}
			// Is the proposed name valid?
			IStatus status = getWorkspace().validateName(
					proposedPath.lastSegment(), resource.getType());
			if (status.getSeverity() == IStatus.ERROR) {
				markInvalid();
			}
			if (!status.isOK()) {
				return status;
			}
		}

		for (int i = 0; i < resources.length; i++) {
			// No error conditions. Check and warn for any overwrites that may
			// occur.
			IResource newResource = null;
			if (resources[i].getType() == IResource.PROJECT) {
				// Projects may be moved to a new location or copied to a new
				// name. To cover both cases, we consider matching location
				// paths, including name, to represent an overwrite.
				IPath proposedPath = getDestinationPath(resources[i], i, true);
				IPath existingPath = resources[i].getLocation().append(
						resources[i].getName());
				if (proposedPath.equals(existingPath)) {
					newResource = resources[i];
				}
			} else {
				IPath proposedPath = getDestinationPath(resources[i], i, false);
				newResource = getWorkspace().getRoot().findMember(proposedPath);
			}
			if (newResource != null) {
				int result = queryOverwrite(newResource, null);
				if (result == IDialogConstants.YES_TO_ALL_ID) {
					return Status.OK_STATUS;
				} else if (result == IDialogConstants.CANCEL_ID) {
					return Status.CANCEL_STATUS;
				} else if (result == IDialogConstants.NO_ID) {
					markInvalid();
					return getErrorStatus(UndoMessages.AbstractResourcesOperation_overwriteError);
				}
				// Otherwise (YES_ID) we continue checking each one
				// individually.
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Return the destination path that should be used to move or copy the
	 * specified resource.
	 * 
	 * @param resource
	 *            the resource being moved or copied
	 * @param index
	 *            the integer index of the resource in the resource array
	 * @param includeProjectLocation
	 *            if this resource is a project, a value of <code>true</code>
	 *            indicates that the returned path should include the project's
	 *            location, and <code>false</code> indicates that the path
	 *            should not include the location
	 * @return the path specifying the destination for the resource
	 */
	protected IPath getDestinationPath(IResource resource, int index,
			boolean includeProjectLocation) {
		if (resource.getType() == IResource.PROJECT) {
			return getProjectDestinationPath((IProject) resource, index,
					includeProjectLocation);
		}
		if (destinationPaths != null) {
			return destinationPaths[index];
		}
		return destination.append(resource.getName());

	}

	/*
	 * Return a path appropriate for moving or copying the specified project.
	 * The boolean parameter determines whether the project's location should be
	 * included in the path.
	 */
	private IPath getProjectDestinationPath(IProject project, int i,
			boolean includeProjectLocation) {
		String projectName;
		IPath projectLocation = null;
		if (destinationPaths != null) {
			if (includeProjectLocation) {
				IPath targetPath = destinationPaths[i];
				if (targetPath.segmentCount() == 1) {
					// we only have the name, so we must assume the same
					// location as the source project
					URI locationURI = null;
					try {
						locationURI = ((IProject) resources[i])
								.getDescription().getLocationURI();
					} catch (CoreException e) {
						// assume null location
					}
					if (locationURI == null) {
						targetPath = Platform.getLocation().append(
								targetPath.lastSegment());
					} else {
						targetPath = new Path(locationURI.getPath())
								.append(targetPath.lastSegment());
					}
				}
				return targetPath;
			}
			// new name specified in destination path
			projectName = destinationPaths[i].lastSegment();
		} else {
			// if not use the old name and common destination path
			projectName = project.getName();
			projectLocation = destination;
		}
		if (includeProjectLocation) {
			return projectLocation.append(projectName);
		}
		return new Path(projectName);
	}

	/**
	 * Return whether a destination path has been specified correctly for this
	 * operation.
	 * 
	 * @return <code>true</code> if there is a destination path specified and
	 *         <code>false</code> if one has not yet been specified.
	 */
	protected boolean hasDestinationPath() {
		return (destinationPaths != null && resources.length == destinationPaths.length)
				|| (destination != null);
	}
}
