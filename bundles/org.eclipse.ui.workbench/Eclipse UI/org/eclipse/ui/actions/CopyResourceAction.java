/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionValidator;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;

/**
 * Standard action for copying the currently selected resources elsewhere
 * in the workspace. All resources being copied as a group must be siblings.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CopyResourceAction extends SelectionListenerAction implements ISelectionValidator {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CopyResourceAction"; //$NON-NLS-1$

	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;

	protected CopyFilesAndFoldersOperation operation;

	/**
	 * Returns a new name for a copy of the resource at the given path in the given
	 * workspace. This name could be determined either automatically or by querying
	 * the user. This name will <b>not</b> be verified by the caller, so it must be
	 * valid and unique.
	 * <p>
	 * Note this method is for internal use only.
	 * </p>
	 *
	 * @param originalName the full path of the resource
	 * @param workspace the workspace
	 * @return the new full path for the copy, or <code>null</code> if the resource
	 *   should not be copied
	 */
	public static IPath getNewNameFor(IPath originalName, IWorkspace workspace) {
		return CopyFilesAndFoldersOperation.getAutoNewNameFor(originalName, workspace);
	}
	/**
	 * Creates a new action.
	 *
	 * @param shell the shell for any dialogs
	 */
	public CopyResourceAction(Shell shell) {
		this(shell, WorkbenchMessages.getString("CopyResourceAction.title")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.COPY_RESOURCE_ACTION);
	}
	/**
	 * Creates a new action with the given text.
	 *
	 * @param shell the shell for any dialogs
	 * @param name the string used as the name for the action, 
	 *   or <code>null</code> if there is no name
	 */
	CopyResourceAction(Shell shell, String name) {
		super(name);
		setToolTipText(WorkbenchMessages.getString("CopyResourceAction.toolTip")); //$NON-NLS-1$
		setId(CopyResourceAction.ID);
		Assert.isNotNull(shell);
		this.shell = shell;
	}
	/**
	 * Returns the operation to perform when this action runs.
	 * 
	 * @return the operation to perform when this action runs.
	 */
	protected CopyFilesAndFoldersOperation createOperation() {
		return new CopyFilesAndFoldersOperation(getShell());
	}
	/**
	 * Returns the path of the container to initially select in the container
	 * selection dialog, or <code>null</code> if there is no initial selection
	 */
	IContainer getInitialContainer() {
		List resources = getSelectedResources();
		if (resources.size() > 0) {
			IResource resource = (IResource) resources.get(0);
			return resource.getParent();
		}
		return null;
	}
	/**
	 * Returns an array of resources to use for the operation from 
	 * the provided list.
	 * 
	 * @return an array of resources to use for the operation
	 */
	protected IResource[] getResources(List resourceList) {
		return (IResource[]) resourceList.toArray(new IResource[resourceList.size()]);
	}
	/**
	 * Returns the shell in which to show any dialogs
	 */
	Shell getShell() {
		return shell;
	}
	/**
	 * The <code>CopyResourceAction</code> implementation of this 
	 * <code>ISelectionValidator</code> method checks whether the given path
	 * is a good place to copy the selected resources.
	 */
	public String isValid(Object destination) {
		IWorkspaceRoot root = WorkbenchPlugin.getPluginWorkspace().getRoot();
		IContainer container = (IContainer) root.findMember((IPath) destination);
		
		if (container != null) {
			// create a new operation here. 
			// isValid is API and may be called in any context.
			CopyFilesAndFoldersOperation operation = createOperation();
			List sources = getSelectedResources();
			IResource[] resources = (IResource[]) sources.toArray(new IResource[sources.size()]);
			return operation.validateDestination(container, resources);
		}
		return null;
	}
	/**
	 * Asks the user for the destination of this action.
	 *
	 * @return the path on an existing or new resource container, or 
	 *  <code>null</code> if the operation should be abandoned
	 */
	IPath queryDestinationResource() {
		// start traversal at root resource, should probably start at a
		// better location in the tree
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(shell, getInitialContainer(), true, WorkbenchMessages.getString("CopyResourceAction.selectDestination")); //$NON-NLS-1$
		dialog.setValidator(this);
		dialog.showClosedProjects(false);
		dialog.open();
		Object[] result = dialog.getResult();
		if (result != null && result.length == 1) {
			return (IPath) result[0];
		}
		return null;
	}
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		operation = createOperation();
		IPath destination = queryDestinationResource();
		if (destination == null)
			return;

		IWorkspaceRoot root = WorkbenchPlugin.getPluginWorkspace().getRoot();
		IContainer container = (IContainer) root.findMember(destination);
		if (container == null) {
			return;
		}

		List sources = getSelectedResources();
		runOperation(getResources(sources), container);
		operation = null;
	}
	/**
	 * Runs the operation created in <code>createOperaiton</code>
	 * 
	 * @param resources source resources to pass to the operation
	 * @param destination destination container to pass to the operation
	 */
	protected void runOperation(IResource[] resources, IContainer destination) {
		operation.copyResources(resources, destination);
	}
	/**
	 * The <code>CopyResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method enables this action only if 
	 * all of the one or more selections are sibling resources which are 
	 * local (depth infinity).
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}
		if (getSelectedNonResources().size() > 0) {
			return false;
		}

		// to enable this command all selected resources must be siblings
		List selectedResources = getSelectedResources();
		if (selectedResources.size() == 0)
			return false;
		IContainer firstParent = ((IResource) selectedResources.get(0)).getParent();
		if (firstParent == null) {
			return false;
		}
		Iterator resourcesEnum = selectedResources.iterator();
		while (resourcesEnum.hasNext()) {
			IResource currentResource = (IResource) resourcesEnum.next();
			if (currentResource.getType() == IResource.PROJECT) {
				return false;
			}
			if (!currentResource.getParent().equals(firstParent)) {
				return false;
			}
		}
		return true;
	}
}
