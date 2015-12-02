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

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A DeleteResourcesOperation represents an undoable operation for deleting one
 * or more resources in the workspace. Clients may call the public API from a
 * background thread.
 * <p>
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.3
 */
public class DeleteResourcesOperation extends AbstractResourcesOperation {

	// Whether to delete project content
	private boolean deleteContent = false;

	/**
	 * Create a DeleteResourcesOperation
	 *
	 * @param resources
	 *            the resources to be deleted
	 * @param label
	 *            the label of the operation
	 * @param deleteContent
	 *            whether or not we are deleting content for projects
	 */
	public DeleteResourcesOperation(IResource[] resources, String label,
			boolean deleteContent) {
		super(resources, label);
		this.deleteContent = deleteContent;
	}

	/*
	 * Map execution to resource deletion.
	 */
	@Override
	protected void doExecute(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		delete(monitor, uiInfo, deleteContent);
	}

	/*
	 * Map undo to resource recreation.
	 */
	@Override
	protected void doUndo(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		recreate(monitor, uiInfo);
	}

	@Override
	protected boolean updateResourceChangeDescriptionFactory(
			IResourceChangeDescriptionFactory factory, int operation) {
		boolean modified = false;
		if (operation == UNDO) {
			for (int i = 0; i < resourceDescriptions.length; i++) {
				if (resourceDescriptions[i] != null) {
					IResource resource = resourceDescriptions[i]
							.createResourceHandle();
					factory.create(resource);
					modified = true;
				}
			}
		} else {
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				factory.delete(resource);
				modified = true;
			}
		}
		return modified;
	}

	@Override
	protected ISchedulingRule getExecuteSchedulingRule() {
		return super.computeDeleteSchedulingRule();
	}

	@Override
	protected ISchedulingRule getUndoSchedulingRule() {
		return super.computeCreateSchedulingRule();
	}

	/*
	 * Map execution status to deletion status. Provide an extra warning if
	 * project content is to be deleted.
	 */
	@Override
	public IStatus computeExecutionStatus(IProgressMonitor monitor) {
		IStatus status = super.computeExecutionStatus(monitor);
		if (status.isOK()) {
			status = computeDeleteStatus();
		}
		return status;
	}

	/*
	 * Map undo status to resource creation status.
	 */
	@Override
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeUndoableStatus(monitor);
		if (status.isOK()) {
			// Recreating should not allow overwriting anything that is there,
			// because we have no way to restore it.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=162655
			status = computeCreateStatus(false);
		}
		return status;
	}

	/*
	 * Map redo status to resource deletion status.
	 */
	@Override
	public IStatus computeRedoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeRedoableStatus(monitor);
		if (status.isOK()) {
			status = computeDeleteStatus();
		}
		return status;
	}

	@Override
	protected void appendDescriptiveText(StringBuffer text) {
		super.appendDescriptiveText(text);
		text.append(" deleteContent: "); //$NON-NLS-1$
		text.append(deleteContent);
		text.append('\'');
	}

	/*
	 * Overridden so that projects whose contents are not to be deleted will not
	 * be checked. A better solution would be to add API to ReadOnlyStateChecker
	 * to specify whether project children should be checked, but it is too late
	 * to do that now. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=180758
	 */
	@Override
	IStatus checkReadOnlyResources(IResource[] resourcesToCheck) {
		// If we aren't deleting content of projects, don't bother
		// checking the read only status of projects or their children.
		// Clients currently do not mix and match projects and non-projects
		// in a DeleteResourcesOperation. However, this is not specified
		// in the API, so assume that there could be mixes.
		if (!deleteContent) {
			ArrayList nonProjectResourcesToCheck = new ArrayList();
			for (int i = 0; i < resourcesToCheck.length; i++) {
				if (resourcesToCheck[i].getType() != IResource.PROJECT) {
					nonProjectResourcesToCheck.add(resourcesToCheck[i]);
				}
			}
			if (nonProjectResourcesToCheck.isEmpty()) {
				return Status.OK_STATUS;
			}
			return super
					.checkReadOnlyResources((IResource[]) nonProjectResourcesToCheck
							.toArray(new IResource[nonProjectResourcesToCheck
									.size()]));
		}
		// We are deleting project content, so do it the normal way
		return super.checkReadOnlyResources(resourcesToCheck);
	}
}
