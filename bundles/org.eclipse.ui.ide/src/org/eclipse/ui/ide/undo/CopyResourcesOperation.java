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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * A CopyResourcesOperation represents an undoable operation for copying one or
 * more resources in the workspace. Clients may call the public API from a
 * background thread.
 * 
 * <p>
 * This operation can track any overwritten resources and restore them when the
 * copy is undone. It is up to clients to determine whether overwrites are
 * allowed. If a resource should not be overwritten, it should not be included
 * in this operation. In addition to checking for overwrites, the target
 * location for the copy is assumed to have already been validated by the
 * client. It will not be revalidated on undo and redo.
 * </p>
 * 
 * <p>
 * This class is intended to be instantiated and used by clients. It is not
 * intended to be subclassed by clients.
 * </p>
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.3
 */
public class CopyResourcesOperation extends
		AbstractCopyOrMoveResourcesOperation {

	IResource[] originalResources;

	ResourceDescription[] snapshotResourceDescriptions;

	/**
	 * Create a CopyResourcesOperation that copies a single resource to a new
	 * location. The new location includes the name of the copy.
	 * 
	 * @param resource
	 *            the resource to be copied
	 * @param newPath
	 *            the new workspace-relative path for the copy, including its
	 *            desired name.
	 * @param label
	 *            the label of the operation
	 */
	public CopyResourcesOperation(IResource resource, IPath newPath,
			String label) {
		super(new IResource[] { resource }, new IPath[] { newPath }, label);
		setOriginalResources(new IResource[] { resource });
	}

	/**
	 * Create a CopyResourcesOperation that copies all of the specified
	 * resources to a single target location. The original resource name will be
	 * used when copied to the new location.
	 * 
	 * @param resources
	 *            the resources to be copied
	 * @param destinationPath
	 *            the workspace-relative destination path for the copied
	 *            resource.
	 * @param label
	 *            the label of the operation
	 */
	public CopyResourcesOperation(IResource[] resources, IPath destinationPath,
			String label) {
		super(resources, destinationPath, label);
		setOriginalResources(this.resources);
	}

	/**
	 * Create a CopyResourcesOperation that copies each of the specified
	 * resources to its corresponding destination path in the destination path
	 * array. The resource name for the target is included in the corresponding
	 * destination path.
	 * 
	 * @param resources
	 *            the resources to be copied. Must not contain null resources.
	 * @param destinationPaths
	 *            a workspace-relative destination path for each copied
	 *            resource, which includes the name of the resource at the new
	 *            destination. Must be the same length as the resources array,
	 *            and may not contain null paths.
	 * @param label
	 *            the label of the operation
	 */
	public CopyResourcesOperation(IResource[] resources,
			IPath[] destinationPaths, String label) {
		super(resources, destinationPaths, label);
		setOriginalResources(this.resources);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation copies the resources.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doExecute(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doExecute(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		copy(monitor, uiInfo);
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
	 * @throws CoreException
	 *             propagates any CoreExceptions thrown from the resources API
	 */
	protected void copy(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {

		monitor.beginTask("", 2000); //$NON-NLS-1$
		monitor
				.setTaskName(UndoMessages.AbstractResourcesOperation_CopyingResourcesProgress);
		List resourcesAtDestination = new ArrayList();
		List overwrittenResources = new ArrayList();

		for (int i = 0; i < resources.length; i++) {
			// Copy the resources and record the overwrites that would
			// be restored if this operation were reversed
			ResourceDescription[] overwrites;
			overwrites = WorkspaceUndoUtil.copy(
					new IResource[] { resources[i] }, getDestinationPath(
							resources[i], i), resourcesAtDestination,
					new SubProgressMonitor(monitor, 1000 / resources.length),
					uiInfo, true, fCreateGroups, fCreateLinks,
					fRelativeToVariable);
			// Accumulate the overwrites into the full list
			for (int j = 0; j < overwrites.length; j++) {
				overwrittenResources.add(overwrites[j]);
			}
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
		// location.
		setTargetResources((IResource[]) resourcesAtDestination
				.toArray(new IResource[resourcesAtDestination.size()]));
		monitor.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation deletes the previously made copies and restores any
	 * resources that were overwritten by the copy.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#doUndo(org.eclipse.core.runtime.IProgressMonitor,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected void doUndo(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		monitor.beginTask("", 2); //$NON-NLS-1$
		monitor
				.setTaskName(UndoMessages.AbstractResourcesOperation_CopyingResourcesProgress);
		// undoing a copy is first deleting the copied resources...
		WorkspaceUndoUtil.delete(resources, new SubProgressMonitor(monitor, 1),
				uiInfo, true);
		// then restoring any overwritten by the previous copy...
		WorkspaceUndoUtil.recreate(resourceDescriptions,
				new SubProgressMonitor(monitor, 1), uiInfo);
		setResourceDescriptions(new ResourceDescription[0]);
		// then setting the target resources back to the original ones.
		// Note that the destination paths never changed since they
		// are not used during undo.
		setTargetResources(originalResources);
		monitor.done();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#updateResourceChangeDescriptionFactory(org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory,
	 *      int)
	 */
	protected boolean updateResourceChangeDescriptionFactory(
			IResourceChangeDescriptionFactory factory, int operation) {
		boolean update = false;
		if (operation == UNDO) {
			for (int i = 0; i < resources.length; i++) {
				update = true;
				IResource resource = resources[i];
				factory.delete(resource);
			}
			for (int i = 0; i < resourceDescriptions.length; i++) {
				if (resourceDescriptions[i] != null) {
					update = true;
					IResource resource = resourceDescriptions[i]
							.createResourceHandle();
					factory.create(resource);
				}
			}
		} else {
			for (int i = 0; i < resources.length; i++) {
				update = true;
				IResource resource = resources[i];
				factory.copy(resource, getDestinationPath(resource, i));
			}
		}
		return update;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * This implementation computes the ability to delete the original copy and
	 * restore any overwritten resources.
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#computeUndoableStatus(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus computeUndoableStatus(IProgressMonitor monitor) {
		IStatus status = super.computeUndoableStatus(monitor);
		if (!status.isOK()) {
			return status;
		}
		// If the originals no longer exist, we do not want to attempt to
		// undo the copy which involves deleting the copies. They may be all we
		// have left.
		if (originalResources == null) {
			markInvalid();
			return getErrorStatus(UndoMessages.CopyResourcesOperation_NotAllowedDueToDataLoss);
		}
		for (int i = 0; i < snapshotResourceDescriptions.length; i++) {
			if (!snapshotResourceDescriptions[i].verifyExistence(true)) {
				markInvalid();
				return getErrorStatus(UndoMessages.CopyResourcesOperation_NotAllowedDueToDataLoss);
			}
		}
		// undoing a copy means deleting the copy that was made
		if (status.isOK()) {
			status = computeDeleteStatus();
		}
		// and if there were resources overwritten by the copy, can we still
		// recreate them?
		if (status.isOK() && resourceDescriptions != null
				&& resourceDescriptions.length > 0) {
			status = computeCreateStatus(true);
		}

		return status;
	}

	/*
	 * Record the original resources, including a resource description to
	 * describe it. This is so we can make sure the original resources and their
	 * subtrees are intact before allowing a copy to be undone.
	 */
	private void setOriginalResources(IResource[] originals) {
		originalResources = originals;
		snapshotResourceDescriptions = new ResourceDescription[originals.length];
		for (int i = 0; i < originals.length; i++) {
			snapshotResourceDescriptions[i] = ResourceDescription
					.fromResource(originals[i]);
		}
	}
}
