/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
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
 *     Mohamed Tarief , IBM - Bug 139211
 *     Lucas Bullen (Red Hat Inc.) - Bug 522096 - "Close Projects" on working set
 *     Lars Vogel <Lars.Vogel@vogella.com> - ask before opening nested projects
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
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
	@Deprecated
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
		for (IProject project : projects) {
			if (!project.isOpen()) {
				count++;
			}
		}
		return count;
	}

	@Override
	protected String getOperationMessage() {
		if (getActionResources().size() > 1) {
			return IDEWorkbenchMessages.OpenResourceAction_operationMessage_plural;
		}
		return IDEWorkbenchMessages.OpenResourceAction_operationMessage;
	}

	@Override
	protected String getProblemsMessage() {
		return IDEWorkbenchMessages.OpenResourceAction_problemMessage;
	}

	@Override
	protected String getProblemsTitle() {
		return IDEWorkbenchMessages.OpenResourceAction_dialogTitle;
	}

	/**
	 * Returns whether there are closed projects in the workspace that are not
	 * among the given ones.
	 */
	private boolean hasOtherClosedProjects(List<? extends IResource> projects) {
		int closedInSelection = 0;
		for (IResource project : projects) {
			if (!((IProject) project).isOpen()) {
				closedInSelection++;
			}
		}
		return closedInSelection < countClosedProjects();
	}

	@Override
	protected void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
		((IProject) resource).open(IResource.BACKGROUND_REFRESH, monitor);
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
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// Warning: code duplicated in CloseResourceAction
		List<? extends IResource> sel = getSelectedResources();
		// don't bother looking at delta if selection not applicable
		if (selectionIsOfType(IResource.PROJECT)) {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				IResourceDelta[] projDeltas = delta.getAffectedChildren(IResourceDelta.CHANGED);
				for (IResourceDelta projDelta : projDeltas) {
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

	@Override
	public void run() {
		List<? extends IResource> projects = promptForProjectsToOpen(getActionResources());
		if (projects == null) {
			// the user cancelled the operation
			return;
		}
		try {
			runOpenWithReferences(projects);
		} catch (OperationCanceledException e) {
			//just return when canceled
		}
	}

	/**
	 * Logs a project that could not be opened, e.g. because its .project file is
	 * missing, so that the remaining projects can still be opened.
	 */
	private static void logOpenFailure(IProject project, CoreException e) {
		ILog.of(OpenResourceAction.class).warn("Failed to open project " + project.getName(), e); //$NON-NLS-1$
	}

	/**
	 * Offers to open closed projects nested below the selected ones, unless the
	 * preference already decides.
	 *
	 * @return the projects to open, or <code>null</code> if the user cancelled
	 */
	private List<? extends IResource> promptForProjectsToOpen(List<? extends IResource> projects) {
		List<IProject> nestedProjects = NestedProjects.below(projects, false);
		if (nestedProjects.isEmpty()) {
			return projects;
		}
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		String key = IDEInternalPreferences.OPEN_NESTED_PROJECTS;
		String value = store.getString(key);
		if (IDEInternalPreferences.PSPM_NEVER.equals(value)) {
			return projects;
		}
		if (!IDEInternalPreferences.PSPM_ALWAYS.equals(value)) {
			// the map fixes the button ids, so the toggle stores ALWAYS or NEVER
			LinkedHashMap<String, Integer> buttons = new LinkedHashMap<>();
			buttons.put(IDEWorkbenchMessages.OpenResourceAction_openSelectedOnly,
					Integer.valueOf(IDialogConstants.NO_ID));
			buttons.put(IDEWorkbenchMessages.OpenResourceAction_openIncludingNested,
					Integer.valueOf(IDialogConstants.YES_ID));
			buttons.put(IDialogConstants.CANCEL_LABEL, Integer.valueOf(IDialogConstants.CANCEL_ID));
			MessageDialogWithToggle dialog = MessageDialogWithToggle.open(MessageDialog.QUESTION, getShell(),
					IDEWorkbenchMessages.OpenResourceAction_nestedTitle, nestedMessage(projects, nestedProjects),
					null, false, store, key, SWT.SHEET, buttons);
			switch (dialog.getReturnCode()) {
			case IDialogConstants.YES_ID:
				break;
			case IDialogConstants.NO_ID:
				return projects;
			default:
				return null;
			}
		}
		List<IResource> allProjects = new ArrayList<>(projects);
		allProjects.addAll(nestedProjects);
		return allProjects;
	}

	/**
	 * @return the question asked when the selection nests further closed projects
	 */
	private static String nestedMessage(List<? extends IResource> projects, List<IProject> nestedProjects) {
		boolean oneProject = projects.size() == 1;
		if (nestedProjects.size() == 1) {
			return oneProject
					? NLS.bind(IDEWorkbenchMessages.OpenResourceAction_openOneNestedBelowProject,
							projects.get(0).getName())
					: IDEWorkbenchMessages.OpenResourceAction_openOneNestedBelowSelection;
		}
		Integer count = Integer.valueOf(nestedProjects.size());
		return oneProject
				? NLS.bind(IDEWorkbenchMessages.OpenResourceAction_openNestedBelowProject, count,
						projects.get(0).getName())
				: NLS.bind(IDEWorkbenchMessages.OpenResourceAction_openNestedBelowSelection, count);
	}

	/**
	 * Opens the given projects, and all related projects, in the background.
	 */
	private void runOpenWithReferences(List<? extends IResource> projects) {
		final List<IResource> resources = new ArrayList<>(projects);
		Job job = new WorkspaceJob(removeMnemonics(getText())) {
			private boolean openProjectReferences = true;
			private boolean hasPrompted = false;
			private boolean canceled = false;
			/**
			 * Opens a project along with all projects it references
			 */
			private void doOpenWithReferences(IProject project, IProgressMonitor mon) throws CoreException {
				if (!project.exists() || project.isOpen()) {
					return;
				}
				SubMonitor subMonitor = SubMonitor.convert(mon, openProjectReferences ? 2 : 1);
				try {
					project.open(IResource.BACKGROUND_REFRESH, subMonitor.split(1));
				} catch (CoreException e) {
					logOpenFailure(project, e);
					return;
				}
				final IProject[] references = project.getReferencedProjects();
				if (!hasPrompted) {
					openProjectReferences = false;
					for (IProject reference : references) {
						if (reference.exists() && !reference.isOpen()) {
							openProjectReferences = true;
							break;
						}
					}
					if (openProjectReferences && hasOtherClosedProjects(resources)) {
						Display.getDefault().syncExec(() -> {
							try {
							openProjectReferences = promptToOpenWithReferences();
							} catch (OperationCanceledException e) {
								canceled = true;
							}
							//remember that we have prompted to avoid repeating the analysis
							hasPrompted = true;
						});
						if (canceled) {
							throw new OperationCanceledException();
						}
					}
				}
				if (openProjectReferences) {
					SubMonitor loopMonitor = subMonitor.split(1).setWorkRemaining(references.length);
					for (IProject reference : references) {
						doOpenWithReferences(reference, loopMonitor.split(1));
					}
				}
			}

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, countClosedProjects());
				// at most we can only open all projects currently closed
				subMonitor.setTaskName(getOperationMessage());
				for (IResource resource : resources) {
					if (!(resource instanceof IProject project)) {
						continue;
					}

					if (!project.exists() || project.isOpen()) {
						continue;
					}
					try {
						doOpenWithReferences(project, subMonitor.split(1));
					} catch (CoreException e) {
						logOpenFailure(project, e);
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.setUser(true);
		job.schedule();
	}

	@Override
	protected boolean shouldPerformResourcePruning() {
		return false;
	}

	/**
	 * The <code>OpenResourceAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method ensures that this action is
	 * enabled only if one of the selections is a closed project.
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection s) {
		// don't call super since we want to enable if closed project is
		// selected.
		setText(IDEWorkbenchMessages.OpenResourceAction_text);
		setToolTipText(IDEWorkbenchMessages.OpenResourceAction_toolTip);
		if (!selectionIsOfType(IResource.PROJECT)) {
			return false;
		}

		boolean hasClosedProjects = false;
		for (IResource currentResource : getSelectedResources()) {
			if (!((IProject) currentResource).isOpen()) {
				if (hasClosedProjects) {
					setText(IDEWorkbenchMessages.OpenResourceAction_text_plural);
					setToolTipText(IDEWorkbenchMessages.OpenResourceAction_toolTip_plural);
					break;
				}
				hasClosedProjects = true;
			}
		}
		return hasClosedProjects;
	}
}
