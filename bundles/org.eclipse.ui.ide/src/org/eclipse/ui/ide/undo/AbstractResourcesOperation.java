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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.undo.ResourceDescription;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * An AbstractResourcesOperation represents an undoable operation that
 * manipulates resources. It provides implementations for resource rename,
 * delete, creation, and modification. It also assigns the workspace undo
 * context as the undo context for operations of this type. Clients may call the
 * public API from a background thread.
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
abstract class AbstractResourcesOperation extends AbstractWorkspaceOperation {

	/*
	 * The array of resource descriptions known by this operation to create or
	 * restore overwritten resources.
	 */
	protected ResourceDescription[] resourceDescriptions;

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
	AbstractResourcesOperation(ResourceDescription[] resourceDescriptions,
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
	 */
	protected void recreate(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		setTargetResources(WorkspaceUndoUtil.recreate(resourceDescriptions,
				monitor, uiInfo));
		setResourceDescriptions(new ResourceDescription[0]);
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
	 */
	protected IStatus computeCreateStatus() {
		if (resourceDescriptions == null || resourceDescriptions.length == 0) {
			markInvalid();
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_NotEnoughInfo);
		}
		for (int i = 0; i < resourceDescriptions.length; i++) {
			if (!resourceDescriptions[i].isValid()) {
				markInvalid();
				return getErrorStatus(UndoMessages.AbstractResourcesOperation_InvalidRestoreInfo);
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
		return Status.OK_STATUS;
	}

	/**
	 * Ask the user whether the given resource should be overwritten.
	 * 
	 * @param resource
	 *            the resource to be overwritten
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user. When this parameter is not <code>null</code>, it
	 *            contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @return One of the IDialogConstants constants indicating which of the
	 *         Yes, Yes to All, No, Cancel options has been selected by the
	 *         user.
	 */
	protected int queryOverwrite(IResource resource, IAdaptable uiInfo) {
		Shell shell = getShell(uiInfo);
		final MessageDialog dialog = new MessageDialog(
				shell,
				UndoMessages.AbstractResourcesOperation_overwriteTitle,
				null,
				NLS
						.bind(
								UndoMessages.AbstractResourcesOperation_overwriteQuestion,
								resource.getName()), MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.YES_TO_ALL_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		int result = dialog.getReturnCode();
		if (result == 0) {
			return IDialogConstants.YES_ID;
		}
		if (result == 1) {
			return IDialogConstants.YES_TO_ALL_ID;
		}
		if (result == 2) {
			return IDialogConstants.NO_ID;
		}
		return IDialogConstants.CANCEL_ID;
	}

	/**
	 * Set the array of resource descriptions describing resources to be
	 * restored when undoing or redoing this operation.
	 * 
	 * @param descriptions
	 *            the array of resource descriptions
	 */
	protected void setResourceDescriptions(ResourceDescription[] descriptions) {
		if (descriptions == null) {
			resourceDescriptions = new ResourceDescription[0];
		} else {
			resourceDescriptions = descriptions;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ide.undo.AbstractWorkspaceOperation#appendDescriptiveText(java.lang.StringBuffer)
	 */
	protected void appendDescriptiveText(StringBuffer text) {
		super.appendDescriptiveText(text);
		text.append(" resourceDescriptions: "); //$NON-NLS-1$
		text.append(resourceDescriptions);
		text.append('\'');
	}
}
