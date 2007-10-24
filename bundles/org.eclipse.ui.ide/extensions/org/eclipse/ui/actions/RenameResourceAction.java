/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.RenameResourceWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Standard action for renaming the selected resources.
 * <p>
 * As of 3.4 this action uses the LTK aware undoable operations.  The standard
 * undoable operations are still available.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class RenameResourceAction extends WorkspaceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID
			+ ".RenameResourceAction";//$NON-NLS-1$

	private String[] modelProviderIds;

	/**
	 * Creates a new action. Using this constructor directly will rename using a
	 * dialog rather than the inline editor of a ResourceNavigator.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 */
	public RenameResourceAction(Shell shell) {
		super(shell, IDEWorkbenchMessages.RenameResourceAction_text);
		setToolTipText(IDEWorkbenchMessages.RenameResourceAction_toolTip);
		setId(ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.RENAME_RESOURCE_ACTION);
	}

	/**
	 * Creates a new action.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 * @param tree
	 *            the tree
	 */
	public RenameResourceAction(Shell shell, Tree tree) {
		this(shell);
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getOperationMessage() {
		return IDEWorkbenchMessages.RenameResourceAction_progress;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getProblemsMessage() {
		return IDEWorkbenchMessages.RenameResourceAction_problemMessage;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getProblemsTitle() {
		return IDEWorkbenchMessages.RenameResourceAction_problemTitle;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction. Since 3.3, this method
	 * is not used, but an implementation is still provided for compatibility.
	 * All work is now done in the operation created in
	 * createOperation(IStatus[]).
	 */
	protected void invokeOperation(IResource resource, IProgressMonitor monitor) {
	}

	/*
	 * (non-Javadoc) Method declared on IAction; overrides method on
	 * WorkspaceAction.
	 */
	public void run() {
		List resourcesList = getSelectedResources();
		if (resourcesList.isEmpty()) {
			return;
		}
		IResource resource = (IResource) resourcesList.get(0);
		RenameResourceWizard refactoringWizard = new RenameResourceWizard(
				resource);
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
				refactoringWizard);
		try {
			op.run(getShell(),
					IDEWorkbenchMessages.RenameResourceAction_inputDialogTitle);
		} catch (InterruptedException e) {
			StatusManager
					.getManager()
					.handle(
							new Status(
									IStatus.ERROR,
									IDEWorkbenchPlugin.IDE_WORKBENCH,
									IDEWorkbenchMessages.RenameResourceAction_problemMessage,
									e));
		}
	}

	/**
	 * Return the currently selected resource. Only return an IResouce if there
	 * is one and only one resource selected.
	 * 
	 * @return IResource or <code>null</code> if there is zero or more than
	 *         one resources selected.
	 */
	private IResource getCurrentResource() {
		List resources = getSelectedResources();
		if (resources.size() == 1) {
			return (IResource) resources.get(0);
		}
		return null;

	}

	/**
	 * The <code>RenameResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * disabled if any of the selections are not resources or resources that are
	 * not local.
	 */
	protected boolean updateSelection(IStructuredSelection selection) {

		if (selection.size() > 1) {
			return false;
		}
		if (!super.updateSelection(selection)) {
			return false;
		}

		IResource currentResource = getCurrentResource();
		if (currentResource == null || !currentResource.exists()) {
			return false;
		}

		return true;
	}

	/**
	 * Set the text action handler.
	 * 
	 * @param actionHandler
	 *            the action handler
	 */
	public void setTextActionHandler(TextActionHandler actionHandler) {
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
