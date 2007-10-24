/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla <b.muskalla@gmx.net>
 *     - Fix for bug 172574 - [IDE] DeleteProjectDialog inconsequent selection behavior

 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.DeleteResourcesWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Standard action for deleting the currently selected resources.
 * <p>
 * As of 3.4 this action uses the LTK aware undoable operations.  The standard
 * undoable operations are still available.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class DeleteResourceAction extends SelectionListenerAction {


	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID
			+ ".DeleteResourceAction";//$NON-NLS-1$

	/**
	 * The shell in which to show any dialogs.
	 */
	private Shell shell;

	/**
	 * Flag that allows testing mode ... it won't pop up the project delete
	 * dialog, and will return "delete all content".
	 */
	protected boolean fTestingMode = false;

	private String[] modelProviderIds;

	/**
	 * Creates a new delete resource action.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 */
	public DeleteResourceAction(Shell shell) {
		super(IDEWorkbenchMessages.DeleteResourceAction_text);
		setToolTipText(IDEWorkbenchMessages.DeleteResourceAction_toolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.DELETE_RESOURCE_ACTION);
		setId(ID);
		if (shell == null) {
			throw new IllegalArgumentException();
		}
		this.shell = shell;
	}
	/**
	 * Returns whether delete can be performed on the current selection.
	 * 
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the resources can be deleted, and
	 *         <code>false</code> if the selection contains non-resources or
	 *         phantom resources
	 */
	private boolean canDelete(IResource[] resources) {
		// allow only projects or only non-projects to be selected;
		// note that the selection may contain multiple types of resource
		if (!(containsOnlyProjects(resources) || containsOnlyNonProjects(resources))) {
			return false;
		}

		if (resources.length == 0) {
			return false;
		}
		// Return true if everything in the selection exists.
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.isPhantom()) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Returns whether the selection contains only non-projects.
	 * 
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the resources contains only non-projects,
	 *         and <code>false</code> otherwise
	 */
	private boolean containsOnlyNonProjects(IResource[] resources) {
		int types = getSelectedResourceTypes(resources);
		// check for empty selection
		if (types == 0) {
			return false;
		}
		// note that the selection may contain multiple types of resource
		return (types & IResource.PROJECT) == 0;
	}

	/**
	 * Returns whether the selection contains only projects.
	 * 
	 * @param resources
	 *            the selected resources
	 * @return <code>true</code> if the resources contains only projects, and
	 *         <code>false</code> otherwise
	 */
	private boolean containsOnlyProjects(IResource[] resources) {
		int types = getSelectedResourceTypes(resources);
		// note that the selection may contain multiple types of resource
		return types == IResource.PROJECT;
	}



	/**
	 * Return an array of the currently selected resources.
	 * 
	 * @return the selected resources
	 */
	private IResource[] getSelectedResourcesArray() {
		List selection = getSelectedResources();
		IResource[] resources = new IResource[selection.size()];
		selection.toArray(resources);
		return resources;
	}

	/**
	 * Returns a bit-mask containing the types of resources in the selection.
	 * 
	 * @param resources
	 *            the selected resources
	 */
	private int getSelectedResourceTypes(IResource[] resources) {
		int types = 0;
		for (int i = 0; i < resources.length; i++) {
			types |= resources[i].getType();
		}
		return types;
	}

	/*
	 * (non-Javadoc) Method declared on IAction.
	 */
	public void run() {
		final IResource[] resources = getSelectedResourcesArray();

		DeleteResourcesWizard refactoringWizard = new DeleteResourcesWizard(
				resources);
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
				refactoringWizard);
		try {
			op.run(shell,
					IDEWorkbenchMessages.DeleteResourceAction_operationLabel);
		} catch (InterruptedException e) {
			StatusManager
					.getManager()
					.handle(
							new Status(
									IStatus.ERROR,
									IDEWorkbenchPlugin.IDE_WORKBENCH,
									NLS
											.bind(
													IDEWorkbenchMessages.MoveProjectAction_internalError,
													e.getMessage()), e));
		}
	}

	/**
	 * The <code>DeleteResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method disables the action if the
	 * selection contains phantom resources or non-resources
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection)
				&& canDelete(getSelectedResourcesArray());
	}

	/**
	 * Returns the model provider ids that are known to the client that
	 * instantiated this operation.
	 * 
	 * @return the model provider ids that are known to the client that
	 *         instantiated this operation.
	 * @since 3.2
	 */
	public String[] getModelProviderIds() {
		return modelProviderIds;
	}

	/**
	 * Sets the model provider ids that are known to the client that
	 * instantiated this operation. Any potential side effects reported by these
	 * models during validation will be ignored.
	 * 
	 * @param modelProviderIds
	 *            the model providers known to the client who is using this
	 *            operation.
	 * @since 3.2
	 */
	public void setModelProviderIds(String[] modelProviderIds) {
		this.modelProviderIds = modelProviderIds;
	}
}
