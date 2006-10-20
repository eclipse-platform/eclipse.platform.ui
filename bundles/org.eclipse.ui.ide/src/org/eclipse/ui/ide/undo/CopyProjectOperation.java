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

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.ide.undo.ResourceDescription;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * A CopyProjectOperation represents an undoable operation for copying a
 * project, also specifying the location of its contents. Clients may call the
 * public API from a background thread.
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
public class CopyProjectOperation extends CopyResourcesOperation {

	private URI projectLocation;

	/**
	 * Create a CopyProjectOperation that copies the specified project and sets
	 * its location to the specified location.
	 * 
	 * @param project
	 *            the project to be copied
	 * @param name
	 *            the name of the copy
	 * @param location
	 *            the location for the project's content, or <code>null</code>
	 *            if the default project location should be used.
	 * @param label
	 *            the label of the operation
	 */
	public CopyProjectOperation(IProject project, String name, URI location,
			String label) {
		super(new IResource[] { project }, new Path(name), label);
		Assert.isLegal(project != null);
		if (location != null
				&& URIUtil.toPath(location).equals(Platform.getLocation())) {
			projectLocation = null;
		} else {
			projectLocation = location;
		}
	}

	/*
	 * Get the project that this operation is moving.
	 */
	private IProject getProject() {
		return (IProject) resources[0];
	}

	/*
	 * Make a project handle for the proposed target project, or null if one
	 * cannot be made.
	 */
	private IProject getProposedProjectHandle() {
		if (destination.segmentCount() == 1) {
			return getWorkspace().getRoot().getProject(
					destination.lastSegment());
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Checks that the specified project location is valid in addition to
	 * superclass checks.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractCopyOrMoveResourcesOperation#computeMoveOrCopyStatus()
	 */
	protected IStatus computeMoveOrCopyStatus() {
		IStatus status = Status.OK_STATUS;
		IProject project = getProposedProjectHandle();
		if (project == null) {
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_NotEnoughInfo);
		}
		if (projectLocation != null) {
			status = getWorkspace().validateProjectLocationURI(project,
					projectLocation);
		}
		if (status.isOK()) {
			return super.computeMoveOrCopyStatus();
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Map execute to moving the project
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doExecute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doExecute(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		IProject newProject = copyProject(getProject(), destination,
				projectLocation, monitor);
		setTargetResources(new IResource[] { newProject });
		setResourceDescriptions(new ResourceDescription[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractCopyOrMoveResourcesOperation#isDestinationPathValid(org.eclipse.core.resources.IResource,
	 *      int)
	 */
	protected boolean isDestinationPathValid(IResource resource, int index) {
		// path has already been validated in #computeMoveOrCopyStatus()
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractCopyOrMoveResourcesOperation#getProposedName(org.eclipse.core.resources.IResource,
	 *      int)
	 */
	protected String getProposedName(IResource resource, int index) {
		return destination.lastSegment();
	}
}
