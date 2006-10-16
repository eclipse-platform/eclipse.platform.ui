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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * A MoveResourcesOperation represents an undoable operation for moving one or
 * more resources in the workspace. Clients may call the public API from a
 * background thread.
 * 
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public class MoveResourcesOperation extends
		AbstractCopyOrMoveResourcesOperation {

	/**
	 * Create a MoveResourcesOperation that moves all of the specified resources
	 * to the same target location, using their existing names.
	 * 
	 * @param resources
	 *            the resources to be moved
	 * @param destinationPath
	 *            the destination path for the resources, not including the name
	 *            of the new resource.
	 * @param label
	 *            the label of the operation
	 */
	public MoveResourcesOperation(IResource[] resources, IPath destinationPath,
			String label) {
		super(resources, destinationPath, label);
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
	}

	/**
	 * Create a MoveResourcesOperation whose destination is not
	 * yet specified.
	 * 
	 * @param resources
	 *            the resources to be modified
	 * @param label
	 *            the label of the operation
	 */
	MoveResourcesOperation(IResource[] resources, String label) {
		super(resources, label);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * Map execute to moving the resources
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doExecute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doExecute(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		moveOrCopy(monitor, uiInfo, true); // true = move
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Map undo to moving the resources.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doUndo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doUndo(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		moveOrCopy(monitor, uiInfo, true); // true = move
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#updateResourceChangeDescriptionFactory(org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory,
	 *      int)
	 */
	protected boolean updateResourceChangeDescriptionFactory(
			IResourceChangeDescriptionFactory factory, int operation) {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			factory.move(resource, getDestinationPath(resource, i));
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Map execution to move status.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeExecutionStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeExecutionStatus(IProgressMonitor monitor) {
		IStatus status = super.computeExecutionStatus(monitor);
		if (status.isOK()) {
			status = computeMoveOrCopyStatus();
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Map undo to move status.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeUndoableStatus(monitor);
		if (status.isOK()) {
			status = computeMoveOrCopyStatus();
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Map redo to move status.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeRedoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeRedoableStatus(monitor);
		if (status.isOK()) {
			status = computeMoveOrCopyStatus();
		}
		return status;
	}
}
