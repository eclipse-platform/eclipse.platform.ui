/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * A MoveResourcesOperation represents an undoable operation for moving one or
 * more resources in the workspace. Clients may call the public API from a
 * background thread.
 * <p>
 * This operation can track any overwritten resources and restore them when the
 * move is undone. It is up to clients to determine whether overwrites are
 * allowed. If a resource should not be overwritten, it should not be included
 * in this operation. In addition to checking for overwrites, the target
 * location for the move is assumed to have already been validated by the
 * client. It will not be revalidated on undo and redo.
 * </p>
 * <p>
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.3
 *
 */
public class MoveResourcesOperation extends
		AbstractCopyOrMoveResourcesOperation {

	IResource[] originalResources;

	IPath originalDestination;

	IPath[] originalDestinationPaths;

	/**
	 * Create a MoveResourcesOperation that moves all of the specified resources
	 * to the same target location, using their existing names.
	 *
	 * @param resources
	 *            the resources to be moved
	 * @param destinationPath
	 *            the destination path for the resources, not including the name
	 *            of the moved resource.
	 * @param label
	 *            the label of the operation
	 */
	public MoveResourcesOperation(IResource[] resources, IPath destinationPath,
			String label) {
		super(resources, destinationPath, label);
		originalResources = this.resources;
		originalDestination = this.destination;
		originalDestinationPaths = this.destinationPaths;
	}

	/**
	 * Create a MoveResourcesOperation that moves a single resource to a new
	 * location. The new location includes the name of the resource, so this may
	 * be used for a move/rename operation or a simple move.
	 *
	 * @param resource
	 *            the resource to be moved
	 * @param newPath
	 *            the new path for the resource, including its desired name.
	 * @param label
	 *            the label of the operation
	 */
	public MoveResourcesOperation(IResource resource, IPath newPath,
			String label) {
		super(new IResource[] { resource }, new IPath[] { newPath }, label);
		originalResources = this.resources;
		originalDestination = this.destination;
		originalDestinationPaths = this.destinationPaths;
	}

	/*
	 * Map execute to moving the resources
	 */
	@Override
	protected void doExecute(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		move(monitor, uiInfo);
	}

	/**
	 * Move any known resources according to the destination parameters known by
	 * this operation. Store enough information to undo and redo the operation.
	 *
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @throws CoreException
	 *             propagates any CoreExceptions thrown from the resources API
	 */
	protected void move(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				resources.length + (resourceDescriptions != null ? resourceDescriptions.length : 0));
		subMonitor.setTaskName(UndoMessages.AbstractResourcesOperation_MovingResources);
		List<IResource> resourcesAtDestination = new ArrayList<>();
		List<IPath> undoDestinationPaths = new ArrayList<>();
		List<ResourceDescription> overwrittenResources = new ArrayList<>();

		for (int i = 0; i < resources.length; i++) {
			// Move the resources and record the overwrites that would
			// be restored if this operation were reversed
			ResourceDescription[] overwrites;
			overwrites = WorkspaceUndoUtil.move(new IResource[] { resources[i] }, getDestinationPath(resources[i], i),
					resourcesAtDestination, undoDestinationPaths, subMonitor.split(1), uiInfo, true);

			// Accumulate the overwrites into the full list
			overwrittenResources.addAll(Arrays.asList(overwrites));
		}

		// Are there any previously overwritten resources to restore now?
		if (resourceDescriptions != null) {
			for (ResourceDescription resourceDescription : resourceDescriptions) {
				if (resourceDescription != null) {
					resourceDescription.createResource(subMonitor.split(1));
				}
			}
		}

		// Reset resource descriptions to the just overwritten resources
		setResourceDescriptions(overwrittenResources
				.toArray(new ResourceDescription[overwrittenResources.size()]));

		// Reset the target resources to refer to the resources in their new
		// location.
		setTargetResources(resourcesAtDestination
				.toArray(new IResource[resourcesAtDestination.size()]));
		// Reset the destination paths that correspond to these resources
		destinationPaths = undoDestinationPaths
				.toArray(new IPath[undoDestinationPaths.size()]);
		destination = null;
	}

	/*
	 * Map undo to moving the resources.
	 */
	@Override
	protected void doUndo(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		// We've recorded the original moves atomically, so perform the move
		move(monitor, uiInfo);
		// Now reset everything back to the way it was originally.
		// If we don't do this, the move will be "precisely reversed."
		// For example, if we merged a folder by moving certain files,
		// we want redo to redo the folder merge, rather than remembering
		// only the files that were originally merged. This makes us more
		// adaptable to changes in the target.
		setTargetResources(originalResources);
		this.resourceDescriptions = new ResourceDescription[0];
		this.destination = originalDestination;
		this.destinationPaths = originalDestinationPaths;
	}

	@Override
	protected boolean updateResourceChangeDescriptionFactory(
			IResourceChangeDescriptionFactory factory, int operation) {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			factory.move(resource, getDestinationPath(resource, i));
		}
		return true;
	}

	/*
	 * Map undo to move status.
	 */
	@Override
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeUndoableStatus(monitor);
		if (status.isOK()) {
			status = computeMoveOrCopyStatus();
		}
		return status;
	}
}
