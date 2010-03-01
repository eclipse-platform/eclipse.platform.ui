/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.ide.undo;



import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * An AbstractCopyOrMoveResourcesOperation represents an undoable operation for
 * moving or copying one or more resources in the workspace. Clients may call
 * the public API from a background thread.
 * 
 * This class is not intended to be subclassed by clients.
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

	protected boolean fCreateGroups = false;

	protected boolean fCreateLinks = false;

	protected String fRelativeToVariable = null;

	/**
	 * Create an AbstractCopyOrMoveResourcesOperation that moves or copies all
	 * of the specified resources to the specified paths. The destination paths
	 * must include the names of the resources at their new location.
	 * 
	 * @param resources
	 *            the resources to be moved or copied.  May not contain null
	 *            resources, or resources that are descendants of already 
	 *            included resources.
	 * @param destinationPaths
	 *            the destination paths for the resources, including the name to
	 *            be assigned to the resource at its new location.  May not contain
	 *            null paths, and must be the same length as the resources array.
	 * @param label
	 *            the label of the operation
	 *            
	 */
	AbstractCopyOrMoveResourcesOperation(IResource[] resources,
			IPath[] destinationPaths, String label) {
		super(resources, label);
		// Check for null arguments
		if (this.resources == null || destinationPaths == null)
			throw new IllegalArgumentException("The resource and destination paths may not be null"); //$NON-NLS-1$
		// Special case to flag descendants.  Note this would fail on the next check
		// anyway, so we are first giving a more specific explanation.
		// See bug #176764
		if (this.resources.length != resources.length)
			throw new IllegalArgumentException("The resource list contained descendants that cannot be moved to separate destination paths"); //$NON-NLS-1$
		// Check for destination paths corresponding for each resource
		if (this.resources.length != destinationPaths.length) {
			throw new IllegalArgumentException("The resource and destination paths must be the same length"); //$NON-NLS-1$
		}
		for (int i=0; i<this.resources.length; i++) {
			if (this.resources[i] == null) {
				throw new IllegalArgumentException("The resources array may not contain null resources"); //$NON-NLS-1$
			}
			if (destinationPaths[i] == null) {
				throw new IllegalArgumentException("The destination paths array may not contain null paths"); //$NON-NLS-1$
			}
		}
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

	public void setCreateVirtualFolders(boolean value) {
		fCreateGroups = value;
	}

	public void setCreateLinks(boolean value) {
		fCreateLinks = value;
	}

	public void setRelativeVariable(String value) {
		fRelativeToVariable = value;
	}
}
