/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * Standard action for refreshing the workspace from the local file system for
 * the selected resources and all of their descendents.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class RefreshAction extends WorkspaceAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".RefreshAction";//$NON-NLS-1$

	/**
	 * Creates a new action.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 */
	public RefreshAction(Shell shell) {
		super(shell, IDEWorkbenchMessages.RefreshAction_text);
		setToolTipText(IDEWorkbenchMessages.RefreshAction_toolTip);
		setId(ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.REFRESH_ACTION);
	}

	/**
	 * Checks whether the given project's location has been deleted. If so,
	 * prompts the user with whether to delete the project or not.
	 */
	void checkLocationDeleted(IProject project) throws CoreException {
		if (!project.exists())
			return;
		IFileInfo location = IDEResourceInfoUtils.getFileInfo(project
				.getLocationURI());
		if (!location.exists()) {
			String message = NLS.bind(
					IDEWorkbenchMessages.RefreshAction_locationDeletedMessage,
					project.getName(), location.toString());

			final MessageDialog dialog = new MessageDialog(getShell(),
					IDEWorkbenchMessages.RefreshAction_dialogTitle, // dialog
																	// title
					null, // use default window icon
					message, MessageDialog.QUESTION, new String[] {
							IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL }, 0); // yes is the
																// default

			// Must prompt user in UI thread (we're in the operation thread
			// here).
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					dialog.open();
				}
			});

			// Do the deletion back in the operation thread
			if (dialog.getReturnCode() == 0) { // yes was chosen
				project.delete(true, true, null);
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getOperationMessage() {
		return IDEWorkbenchMessages.RefreshAction_progressMessage;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getProblemsMessage() {
		return IDEWorkbenchMessages.RefreshAction_problemMessage;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getProblemsTitle() {
		return IDEWorkbenchMessages.RefreshAction_problemTitle;
	}

	/**
	 * Returns a list containing the workspace root if the selection would
	 * otherwise be empty.
	 */
	protected List getSelectedResources() {
		List resources = super.getSelectedResources();
		if (resources.isEmpty()) {
			resources = new ArrayList();
			resources.add(ResourcesPlugin.getWorkspace().getRoot());
		}
		return resources;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected void invokeOperation(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		// Check if project's location has been deleted,
		// as per 1G83UCE: ITPUI:WINNT - Refresh from local doesn't detect new
		// or deleted projects
		// and also for bug report #18283
		if (resource.getType() == IResource.PROJECT) {
			checkLocationDeleted((IProject) resource);
		} else if (resource.getType() == IResource.ROOT) {
			IProject[] projects = ((IWorkspaceRoot) resource).getProjects();
			for (int i = 0; i < projects.length; i++) {
				checkLocationDeleted(projects[i]);
			}
		}
		resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	/**
	 * The <code>RefreshAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * enabled if the selection is empty, but is disabled if any of the selected
	 * elements are not resources.
	 */
	protected boolean updateSelection(IStructuredSelection s) {
		return (super.updateSelection(s) || s.isEmpty())
				&& getSelectedNonResources().size() == 0;
	}

	/**
	 * Handle the key release.
	 * 
	 * @param event
	 *            the event
	 */
	public void handleKeyReleased(KeyEvent event) {

		if (event.keyCode == SWT.F5 && event.stateMask == 0) {
			refreshAll();
		}
	}

	/**
	 * Refreshes the entire workspace.
	 */
	public void refreshAll() {
		IStructuredSelection currentSelection = getStructuredSelection();
		selectionChanged(StructuredSelection.EMPTY);
		run();
		selectionChanged(currentSelection);
	}

	/*
	 * (non-Javadoc) Method declared on IAction; overrides method on
	 * WorkspaceAction.
	 */
	public void run() {
		ISchedulingRule rule = null;
		IResourceRuleFactory factory = ResourcesPlugin.getWorkspace()
				.getRuleFactory();
		Iterator resources = getSelectedResources().iterator();
		while (resources.hasNext()) {
			rule = MultiRule.combine(rule, factory
					.refreshRule((IResource) resources.next()));
		}
		runInBackground(rule);
	}
}
