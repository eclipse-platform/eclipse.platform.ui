/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A CreateResourcesOperation represents an undoable operation for creating
 * resources in the workspace. Clients may call the public API from a background
 * thread.
 *
 * This class is not intended to be subclassed by clients.
 *
 * @since 3.3
 *
 */
abstract class AbstractCreateResourcesOperation extends
		AbstractResourcesOperation {

	/**
	 * Create an AbstractCreateResourcesOperation.
	 *
	 * @param resourceDescriptions
	 *            the resourceDescriptions describing resources to be created
	 * @param label
	 *            the label of the operation
	 */
	AbstractCreateResourcesOperation(
			ResourceDescription[] resourceDescriptions, String label) {
		super(resourceDescriptions, label);
	}

	/*
	 * This implementation creates resources from the known resource
	 * descriptions.
	 */
	@Override
	protected void doExecute(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		recreate(monitor, uiInfo);
	}

	/*
	 * This implementation deletes resources.
	 */
	@Override
	protected void doUndo(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		delete(monitor, uiInfo, false); // never delete content
	}

	/*
	 * This implementation documents the impending create or delete.
	 */
	@Override
	protected boolean updateResourceChangeDescriptionFactory(
			IResourceChangeDescriptionFactory factory, int operation) {
		boolean modified = false;
		if (operation == UNDO) {
			for (IResource resource : resources) {
				factory.delete(resource);
				modified = true;
			}
		} else {
			for (ResourceDescription resourceDescription : resourceDescriptions) {
				if (resourceDescription != null) {
					IResource resource = resourceDescription.createResourceHandle();
					factory.create(resource);
					modified = true;
				}
			}
		}
		return modified;
	}

	@Override
	protected ISchedulingRule getExecuteSchedulingRule() {
		return super.computeCreateSchedulingRule();
	}

	@Override
	protected ISchedulingRule getUndoSchedulingRule() {
		return super.computeDeleteSchedulingRule();
	}

	/*
	 * This implementation computes the status for creating resources.
	 */
	@Override
	public IStatus computeExecutionStatus(IProgressMonitor monitor) {
		IStatus status = super.computeExecutionStatus(monitor);
		if (status.isOK()) {
			status = computeCreateStatus(true);
		}
		return status;
	}

	/*
	 * This implementation computes the status for deleting resources.
	 */
	@Override
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeUndoableStatus(monitor);
		if (status.isOK()) {
			status = computeDeleteStatus();
		}
		return status;
	}

	/*
	 * This implementation computes the status for creating resources.
	 */
	@Override
	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeRedoableStatus(monitor);
		if (status.isOK()) {
			status = computeCreateStatus(true);
		}
		return status;
	}
}
