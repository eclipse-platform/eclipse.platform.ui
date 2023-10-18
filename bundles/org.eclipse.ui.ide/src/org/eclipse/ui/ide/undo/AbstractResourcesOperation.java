/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.undo.snapshot.IResourceSnapshot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.ui.actions.ReadOnlyStateChecker;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * An AbstractResourcesOperation represents an undoable operation that
 * manipulates resources. It provides implementations for resource rename,
 * delete, creation, and modification. It also assigns the workspace undo
 * context as the undo context for operations of this type.
 *
 * Resource operations record a snapshot of the workspace state when they
 * are created.  It is expected that an operation will be created and
 * executed without there being any intermediate changes to the resources
 * described in the operation between construction of the operation and
 * execution of the operation.
 *
 * Clients may call the public API from a background thread.
 *
 * This class is not intended to be subclassed by clients.
 *
 * @since 3.3
 *
 */
abstract class AbstractResourcesOperation extends AbstractWorkspaceOperation {

	/*
	 * The array of resource descriptions known by this operation to create or
	 * restore overwritten resources.
	 */
	protected IResourceSnapshot<? extends IResource>[] resourceDescriptions;

	/*
	 * Return true if the specified subResource is a descendant of the specified
	 * super resource. Used to remove descendants from the resource array when
	 * an operation is requested on a parent and its descendant.
	 */
	private static boolean isDescendantOf(IResource subResource,
			IResource superResource) {
		return !subResource.equals(superResource)
				&& superResource.getFullPath().isPrefixOf(
						subResource.getFullPath());
	}

	/**
	 * Create an Abstract Resources Operation
	 *
	 * @param resources
	 *            the resources to be modified
	 * @param label
	 *            the label of the operation
	 */
	AbstractResourcesOperation(IResource[] resources, String label) {
		super(label);
		this.addContext(WorkspaceUndoUtil.getWorkspaceUndoContext());

		setTargetResources(resources);
	}

	/**
	 * Create an Abstract Resources Operation
	 *
	 * @param resourceDescriptions
	 *            the resourceDescriptions describing resources to be created
	 * @param label
	 *            the label of the operation
	 */
	AbstractResourcesOperation(IResourceSnapshot<? extends IResource>[] resourceDescriptions,
			String label) {
		super(label);
		addContext(WorkspaceUndoUtil.getWorkspaceUndoContext());
		setResourceDescriptions(resourceDescriptions);
	}

	/**
	 * Delete any resources known by this operation. Store enough information to
	 * undo and redo the operation.
	 *
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @param deleteContent
	 *            <code>true</code> if the content of any known projects
	 *            should be deleted along with the project. <code>false</code>
	 *            if project content should not be deleted.
	 * @throws CoreException
	 *             propagates any CoreExceptions thrown from the resources API
	 */
	protected void delete(IProgressMonitor monitor, IAdaptable uiInfo,
			boolean deleteContent) throws CoreException {
		setResourceDescriptions(WorkspaceUndoUtil.delete(resources, monitor,
				uiInfo, deleteContent));
		setTargetResources(new IResource[0]);
	}

	/**
	 * Recreate any resources known by this operation. Store enough information
	 * to undo and redo the operation.
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
	protected void recreate(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		setTargetResources(WorkspaceUndoUtil.recreate(resourceDescriptions,
				monitor, uiInfo));
		setResourceDescriptions(new IResourceSnapshot<?>[0]);
	}

	/**
	 * Compute the status for creating resources from the descriptions. A status
	 * severity of <code>OK</code> indicates that the create is likely to be
	 * successful. A status severity of <code>ERROR</code> indicates that the
	 * operation is no longer valid. Other status severities are open to
	 * interpretation by the caller.
	 *
	 * Note this method may be called on initial creation of a resource, or when
	 * a create or delete operation is being undone or redone. Therefore, this
	 * method should check conditions that can change over the life of the
	 * operation, such as the existence of the information needed to carry out
	 * the operation. One-time static checks should typically be done by the
	 * caller (such as the action that creates the operation) so that the user
	 * is not continually prompted or warned about conditions that were
	 * acceptable at the time of original execution.
	 *
	 * @param allowOverwrite
	 *            a boolean that specifies whether resource creation should be
	 *            allowed to overwrite an existent resource.
	 */
	protected IStatus computeCreateStatus(boolean allowOverwrite) {
		if (resourceDescriptions == null || resourceDescriptions.length == 0) {
			markInvalid();
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_NotEnoughInfo);
		}
		for (IResourceSnapshot<? extends IResource> resourceDescription : resourceDescriptions) {
			// Check for enough info to restore the resource
			if (resourceDescription == null || !resourceDescription.isValid()) {
				markInvalid();
				return getErrorStatus(UndoMessages.AbstractResourcesOperation_InvalidRestoreInfo);
			} else if (!allowOverwrite && resourceDescription.verifyExistence(false)) {
				// overwrites are not allowed and the resource already exists
				markInvalid();
				return getErrorStatus(UndoMessages.AbstractResourcesOperation_ResourcesAlreadyExist);
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Compute the status for deleting resources. A status severity of
	 * <code>OK</code> indicates that the delete is likely to be successful. A
	 * status severity of <code>ERROR</code> indicates that the operation is
	 * no longer valid. Other status severities are open to interpretation by
	 * the caller.
	 *
	 * Note this method may be called on initial deletion of a resource, or when
	 * a create or delete operation is being undone or redone. Therefore, this
	 * method should check conditions that can change over the life of the
	 * operation, such as the existence of the resources to be deleted. One-time
	 * static checks should typically be done by the caller (such as the action
	 * that creates the operation) so that the user is not continually prompted
	 * or warned about conditions that were acceptable at the time of original
	 * execution.
	 */
	protected IStatus computeDeleteStatus() {
		if (resources == null || resources.length == 0) {
			markInvalid();
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_NotEnoughInfo);
		}
		if (!resourcesExist()) {
			markInvalid();
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_ResourcesDoNotExist);
		}
		return checkReadOnlyResources(resources);
	}

	/**
	 * Check the specified resources for read only state, and return a status
	 * indicating whether the resources can be deleted.
	 */
	IStatus checkReadOnlyResources(IResource[] resourcesToCheck) {
		// Check read only status if we are permitted
		// to consult the user.
		if (!quietCompute) {
			ReadOnlyStateChecker checker = new ReadOnlyStateChecker(
					getShell(null),
					IDEWorkbenchMessages.DeleteResourceAction_title1,
					IDEWorkbenchMessages.DeleteResourceAction_readOnlyQuestion);
			checker.setIgnoreLinkedResources(true);
			IResource[] approvedResources = checker
					.checkReadOnlyResources(resourcesToCheck);
			if (approvedResources.length == 0) {
				// Consider this a cancelled redo.
				return Status.CANCEL_STATUS;
			}
			// Redefine the redo to only include the approved ones.
			setTargetResources(approvedResources);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Set the array of resource descriptions describing resources to be
	 * restored when undoing or redoing this operation.
	 *
	 * @param descriptions
	 *            the array of resource descriptions
	 */
	protected void setResourceDescriptions(IResourceSnapshot<? extends IResource>[] descriptions) {
		if (descriptions == null) {
			resourceDescriptions = new IResourceSnapshot<?>[0];
		} else {
			resourceDescriptions = descriptions;
		}
	}

	@Override
	protected void appendDescriptiveText(StringBuffer text) {
		super.appendDescriptiveText(text);
		text.append(" resourceDescriptions: "); //$NON-NLS-1$
		text.append(resourceDescriptions);
		text.append('\'');
	}

	/**
	 * Compute a scheduling rule for creating resources.
	 *
	 * @return a scheduling rule appropriate for creating the resources
	 *         specified in the resource descriptions
	 */
	protected ISchedulingRule computeCreateSchedulingRule() {
		ISchedulingRule[] ruleArray = new ISchedulingRule[resourceDescriptions.length * 3];

		for (int i = 0; i < resourceDescriptions.length; i++) {
			if (resourceDescriptions[i] != null) {
				IResource resource = resourceDescriptions[i]
						.createResourceHandle();
				// Need a rule for creating...
				ruleArray[i * 3] = getWorkspaceRuleFactory().createRule(
						resource);
				// ...and modifying
				ruleArray[i * 3 + 1] = getWorkspaceRuleFactory().modifyRule(
						resource);
				// ...and changing the charset
				ruleArray[i * 3 + 2] = getWorkspaceRuleFactory().charsetRule(
						resource);
			}

		}
		return MultiRule.combine(ruleArray);
	}

	/**
	 * Compute a scheduling rule for deleting resources.
	 *
	 * @return a scheduling rule appropriate for deleting the resources
	 *         specified in the receiver.
	 */
	protected ISchedulingRule computeDeleteSchedulingRule() {
		ISchedulingRule[] ruleArray = new ISchedulingRule[resources.length * 2];
		for (int i = 0; i < resources.length; i++) {
			ruleArray[i * 2] = getWorkspaceRuleFactory().deleteRule(
					resources[i]);
			// we include a modify rule because we may have to open a project
			// to record its resources before deleting it.
			ruleArray[i * 2 + 1] = getWorkspaceRuleFactory().modifyRule(
					resources[i]);
		}
		return MultiRule.combine(ruleArray);

	}

	@Override
	protected void setTargetResources(IResource[] targetResources) {
		// Remove any descendants if the parent has also
		// been specified.
		Set<IResource> subResources = new HashSet<>();
		for (IResource subResource : targetResources) {
			for (IResource superResource : targetResources) {
				if (isDescendantOf(subResource, superResource) && !subResources.contains(subResource))
					subResources.add(subResource);
			}
		}
		IResource[] nestedResourcesRemoved = new IResource[targetResources.length - subResources.size()];
		int j = 0;
		for (IResource targetResource : targetResources) {
			if (!subResources.contains(targetResource)) {
				nestedResourcesRemoved[j] = targetResource;
				j++;
			}
		}
		super.setTargetResources(nestedResourcesRemoved);
	}
}
