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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
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

	@Override
	protected void invokeOperation(IResource resource, IProgressMonitor monitor) throws CoreException {
		((IProject) resource).open(IResource.BACKGROUND_REFRESH, monitor);
	}

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
		List<? extends IResource> projects = getActionResources();
		List<IProject> nestedProjects = NestedProjects.below(projects, false);
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		String nestedValue = store.getString(IDEInternalPreferences.OPEN_NESTED_PROJECTS);
		String referencedValue = store.getString(IDEInternalPreferences.OPEN_REQUIRED_PROJECTS);
		boolean includeNested = IDEInternalPreferences.PSPM_ALWAYS.equals(nestedValue);
		boolean includeReferenced = IDEInternalPreferences.PSPM_ALWAYS.equals(referencedValue);
		boolean askNested = !nestedProjects.isEmpty() && prompts(nestedValue);
		boolean askReferenced = prompts(referencedValue) && hasClosedReferences(projects, nestedProjects);
		if (askNested || askReferenced) {
			RelatedProjectsDialog.Answer answer = RelatedProjectsDialog.open(getShell(),
					IDEWorkbenchMessages.OpenResourceAction_promptTitle,
					askNested ? nestedMessage(projects, nestedProjects)
							: IDEWorkbenchMessages.OpenResourceAction_referencedProjectsClosed,
					IDEWorkbenchMessages.OpenResourceAction_open, store,
					askNested ? IDEInternalPreferences.OPEN_NESTED_PROJECTS : null,
					askReferenced ? IDEInternalPreferences.OPEN_REQUIRED_PROJECTS : null);
			if (answer == null) {
				return;
			}
			includeNested = askNested ? answer.includeNested() : includeNested;
			includeReferenced = askReferenced ? answer.includeReferenced() : includeReferenced;
		}
		List<IResource> allProjects = new ArrayList<>(projects);
		if (includeNested) {
			allProjects.addAll(nestedProjects);
		}
		runOpenWithReferences(allProjects, includeReferenced);
	}

	private static boolean prompts(String preferenceValue) {
		return !IDEInternalPreferences.PSPM_ALWAYS.equals(preferenceValue)
				&& !IDEInternalPreferences.PSPM_NEVER.equals(preferenceValue);
	}

	/**
	 * Returns whether a project among the given ones references a closed
	 * project outside them. Closed projects cannot be asked, so the references
	 * come from their .project files.
	 */
	private static boolean hasClosedReferences(List<? extends IResource> projects, List<IProject> nestedProjects) {
		List<IResource> candidates = new ArrayList<>(projects);
		candidates.addAll(nestedProjects);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		for (IResource candidate : candidates) {
			IPath location = candidate.getLocation();
			if (!(candidate instanceof IProject project) || project.isOpen() || location == null) {
				continue;
			}
			IProjectDescription description;
			try {
				description = workspace.loadProjectDescription(location.append(IProjectDescription.DESCRIPTION_FILE_NAME));
			} catch (CoreException e) {
				continue;
			}
			for (IProject reference : description.getReferencedProjects()) {
				if (reference.exists() && !reference.isOpen() && !candidates.contains(reference)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Logs a project that could not be opened, e.g. because its .project file is
	 * missing, so that the remaining projects can still be opened.
	 */
	private static void logOpenFailure(IProject project, CoreException e) {
		ILog.of(OpenResourceAction.class).warn("Failed to open project " + project.getName(), e); //$NON-NLS-1$
	}

	/**
	 * @return the statement shown when the selection nests further closed projects
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
	 * Opens the given projects in the background, with the projects they
	 * reference if wanted.
	 */
	private void runOpenWithReferences(List<? extends IResource> projects, boolean openProjectReferences) {
		final List<IResource> resources = new ArrayList<>(projects);
		Job job = new WorkspaceJob(removeMnemonics(getText())) {
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
				if (openProjectReferences) {
					IProject[] references = project.getReferencedProjects();
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
