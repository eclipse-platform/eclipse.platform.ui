/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mohamed Tarief , IBM - Bug 139211
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Standard action for opening the currently selected project(s).
 * <p>
 * Note that there is a different action for opening an editor on file
 * resources: <code>OpenFileAction</code>.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class OpenResourceAction extends WorkspaceAction implements IResourceChangeListener {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".OpenResourceAction"; //$NON-NLS-1$

	/**
	 * Creates a new action.
	 * 
	 * @param shell
	 *            the shell for any dialogs
	 *    
	 * @deprecated {@link #OpenResourceAction(IShellProvider)}
	 */
	public OpenResourceAction(Shell shell) {
		super(shell, IDEWorkbenchMessages.OpenResourceAction_text);
		initAction();
	}

	/**
	 * Creates a new action.
	 * 
	 * @param provider
	 * 				the shell for any dialogs
	 * @since 3.4
	 */
	public OpenResourceAction(IShellProvider provider) {
		super(provider, IDEWorkbenchMessages.OpenResourceAction_text);
		initAction();
	}

	/**
	 * Initializes the workbench
	 */
	private void initAction() {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.OPEN_RESOURCE_ACTION);
		setToolTipText(IDEWorkbenchMessages.OpenResourceAction_toolTip);
		setId(ID);
	}

	/**
	 * Returns the total number of closed projects in the workspace.
	 */
	private int countClosedProjects() {
		int count = 0;
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (!projects[i].isOpen()) {
				count++;
			}
		}
		return count;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getOperationMessage() {
		return IDEWorkbenchMessages.OpenResourceAction_operationMessage;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getProblemsMessage() {
		return IDEWorkbenchMessages.OpenResourceAction_problemMessage;
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected String getProblemsTitle() {
		return IDEWorkbenchMessages.OpenResourceAction_dialogTitle;
	}

	/**
	 * Returns whether there are closed projects in the workspace that are
	 * not part of the current selection.
	 */
	private boolean hasOtherClosedProjects() {
		//count the closed projects in the selection
		int closedInSelection = 0;
		Iterator resources = getSelectedResources().iterator();
		while (resources.hasNext()) {
			IProject project = (IProject) resources.next();
			if (!project.isOpen())
				closedInSelection++;
		}
		//there are other closed projects if the selection does
		//not contain all closed projects in the workspace
		return closedInSelection < countClosedProjects();
	}

	protected void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
		((IProject) resource).open(monitor);
	}

	/**
	 * Returns the preference for whether to open required projects when opening
	 * a project. Consults the preference and prompts the user if necessary.
	 * 
	 * @return <code>true</code> if referenced projects should be opened, and
	 *         <code>false</code> otherwise.
	 */
	private boolean promptToOpenWithReferences() {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		String key = IDEInternalPreferences.OPEN_REQUIRED_PROJECTS;
		String value = store.getString(key);
		if (MessageDialogWithToggle.ALWAYS.equals(value)) {
			return true;
		}
		if (MessageDialogWithToggle.NEVER.equals(value)) {
			return false;
		}
		String message = IDEWorkbenchMessages.OpenResourceAction_openRequiredProjects;
		MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(getShell(), IDEWorkbenchMessages.Question, message, null, false, store, key);
		int result = dialog.getReturnCode();
		// the result is equal to SWT.DEFAULT if the user uses the 'esc' key to close the dialog
		if (result == Window.CANCEL || result == SWT.DEFAULT) {
			throw new OperationCanceledException();
		}
		return dialog.getReturnCode() == IDialogConstants.YES_ID;
	}

	/**
	 * Handles a resource changed event by updating the enablement if one of the
	 * selected projects is opened or closed.
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		// Warning: code duplicated in CloseResourceAction
		List sel = getSelectedResources();
		// don't bother looking at delta if selection not applicable
		if (selectionIsOfType(IResource.PROJECT)) {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				IResourceDelta[] projDeltas = delta.getAffectedChildren(IResourceDelta.CHANGED);
				for (int i = 0; i < projDeltas.length; ++i) {
					IResourceDelta projDelta = projDeltas[i];
					if ((projDelta.getFlags() & IResourceDelta.OPEN) != 0) {
						if (sel.contains(projDelta.getResource())) {
							selectionChanged(getStructuredSelection());
							return;
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc) Method declared on IAction; overrides method on
	 * WorkspaceAction.
	 */
	public void run() {
		try {
			runOpenWithReferences();
		} catch (OperationCanceledException e) {
			//just return when canceled
		}
	}

	/**
	 * Opens the selected projects, and all related projects, in the background.
	 */
	private void runOpenWithReferences() {
		final List resources = new ArrayList(getActionResources());
		Job job = new WorkspaceJob(removeMnemonics(getText())) {
			private boolean openProjectReferences = true;
			private boolean hasPrompted = false;
			private boolean canceled = false;
			/**
			 * Opens a project along with all projects it references
			 */
			private void doOpenWithReferences(IProject project, IProgressMonitor monitor) throws CoreException {
				if (!project.exists() || project.isOpen()) {
					return;
				}
				project.open(new SubProgressMonitor(monitor, 1000));
				final IProject[] references = project.getReferencedProjects();
				if (!hasPrompted) {
					openProjectReferences = false;
					for (int i = 0; i < references.length; i++) {
						if (references[i].exists() && !references[i].isOpen()) {
							openProjectReferences = true;
							break;
						}
					}
					if (openProjectReferences && hasOtherClosedProjects()) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								try {
								openProjectReferences = promptToOpenWithReferences();
								} catch (OperationCanceledException e) {
									canceled = true;
								}
								//remember that we have prompted to avoid repeating the analysis
								hasPrompted = true;
							}
						});
						if (canceled)
							throw new OperationCanceledException();
					}
				}
				if (openProjectReferences) {
					for (int i = 0; i < references.length; i++) {
						doOpenWithReferences(references[i], monitor);
					}
				}
			}

			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					// at most we can only open all projects currently closed
					monitor.beginTask("", countClosedProjects() * 1000); //$NON-NLS-1$
					monitor.setTaskName(getOperationMessage());
					for (Iterator it = resources.iterator(); it.hasNext();) {
						doOpenWithReferences((IProject) it.next(), monitor);
					}
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.setUser(true);
		job.schedule();
	}

	/*
	 * (non-Javadoc) Method declared on WorkspaceAction.
	 */
	protected boolean shouldPerformResourcePruning() {
		return false;
	}

	/**
	 * The <code>OpenResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * enabled only if one of the selections is a closed project.
	 */
	protected boolean updateSelection(IStructuredSelection s) {
		// don't call super since we want to enable if closed project is
		// selected.

		if (!selectionIsOfType(IResource.PROJECT)) {
			return false;
		}

		Iterator resources = getSelectedResources().iterator();
		while (resources.hasNext()) {
			IProject currentResource = (IProject) resources.next();
			if (!currentResource.isOpen()) {
				return true;
			}
		}
		return false;
	}
}
