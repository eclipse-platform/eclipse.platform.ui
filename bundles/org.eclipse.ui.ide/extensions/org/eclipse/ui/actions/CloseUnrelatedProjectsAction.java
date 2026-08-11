/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
 *     Dina Sayed, dsayed@eg.ibm.com, IBM -  bug 269844
 *     Andrey Loskutov <loskutov@gmx.de> - generified interface, bug 462760
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *     Lucas Bullen (Red Hat Inc.) - Bug 522096 - "Close Projects" on working set
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.misc.ProjectReferenceGraph;

/**
 * This action closes all projects that are unrelated to the selected projects. A
 * project is unrelated if it is not directly or transitively referenced by one
 * of the selected projects, and does not directly or transitively reference
 * one of the selected projects.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @see IDEActionFactory#CLOSE_UNRELATED_PROJECTS
 * @since 3.3
 */
public class CloseUnrelatedProjectsAction extends CloseResourceAction {
	/**
	 * The id of this action.
	 */
	@SuppressWarnings("hiding")
	public static final String ID = PlatformUI.PLUGIN_ID + ".CloseUnrelatedProjectsAction"; //$NON-NLS-1$

	private List<IResource> projectsToClose = new ArrayList<>();

	private boolean selectionDirty = true;

	private List<? extends IResource> oldSelection = Collections.emptyList();

	/** Kept in a field so repeated requests register the same callback. */
	private final Runnable enablementRefresher = this::updateEnablementInUIThread;

	/**
	 * Creates this action.
	 *
	 * @param shell
	 *            The shell to use for parenting any dialogs created by this
	 *            action.
	 *
	 * @deprecated {@link #CloseUnrelatedProjectsAction(IShellProvider)}
	 */
	@Deprecated
	public CloseUnrelatedProjectsAction(Shell shell) {
		super(shell, IDEWorkbenchMessages.CloseUnrelatedProjectsAction_text);
		initAction();
	}

	/**
	 * Creates this action.
	 *
	 * @param provider
	 *            The shell to use for parenting any dialogs created by this
	 *            action.
	 * @since 3.4
	 */
	public CloseUnrelatedProjectsAction(IShellProvider provider){
		super(provider, IDEWorkbenchMessages.CloseUnrelatedProjectsAction_text,
				IDEWorkbenchMessages.CloseUnrelatedProjectsAction_toolTip,
				IDEWorkbenchMessages.CloseUnrelatedProjectsAction_text_plural,
				IDEWorkbenchMessages.CloseUnrelatedProjectsAction_toolTip_plural);
		initAction();
	}

	@Override
	public void run() {
		if (!refreshProjectGraph()) {
			return;
		}
		if (promptForConfirmation()) {
			super.run();
		}
	}

	/**
	 * Rebuilds the project graph before the projects to close are computed from it.
	 * Enablement may answer from a stale graph, but closing projects should not. A
	 * workspace that keeps changing can still leave the graph one rebuild behind;
	 * that graph is used rather than refusing to close anything while a build runs,
	 * and it is far closer to the current state than the one the action ran on
	 * before.
	 *
	 * @return <code>false</code> if the user cancelled or the rebuild failed
	 */
	private boolean refreshProjectGraph() {
		try {
			// waits for the background job rather than computing here, so a slow
			// reference provider cannot freeze the workbench
			PlatformUI.getWorkbench().getProgressService()
					.busyCursorWhile(monitor -> ProjectReferenceGraph.getInstance().refresh(monitor));
		} catch (InterruptedException e) {
			// how the progress service reports that the user cancelled, so the
			// calling thread was never interrupted and must not be marked as such
			return false;
		} catch (InvocationTargetException e) {
			IDEWorkbenchPlugin.log("Failed to compute the project reference graph", e); //$NON-NLS-1$
			return false;
		}
		// drop the projects computed from the previous graph
		clearCache();
		return true;
	}

	/**
	 * Returns whether to close unrelated projects.
	 * Consults the preference and prompts the user if necessary.
	 *
	 * @return <code>true</code> if unrelated projects should be closed, and
	 *         <code>false</code> otherwise.
	 */
	private boolean promptForConfirmation() {
		IPreferenceStore store = IDEWorkbenchPlugin.getDefault().getPreferenceStore();
		if (store.getBoolean(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS)) {
			return true;
		}

		// get first project name
		List<? extends IResource> selection = super.getSelectedResources();
		int selectionSize = selection.size();
		if (selectionSize == 0) {
			return true;
		}

		String message = null;
		if (selectionSize == 1) { // if one project is selected then print its name
			IResource firstSelected = selection.get(0);
			String projectName = null;
			if (firstSelected instanceof IProject) {
				projectName = firstSelected.getName();
			}
			message = NLS.bind(IDEWorkbenchMessages.CloseUnrelatedProjectsAction_confirmMsg1, projectName);
		} else { // if more then one project is selected then print there number
			message = NLS.bind(IDEWorkbenchMessages.CloseUnrelatedProjectsAction_confirmMsgN, selectionSize);
		}

			MessageDialogWithToggle dialog = MessageDialogWithToggle.openOkCancelConfirm(
						getShell(), IDEWorkbenchMessages.CloseUnrelatedProjectsAction_toolTip,
						message, IDEWorkbenchMessages.CloseUnrelatedProjectsAction_AlwaysClose,
						false, null, null);
		if (dialog.getReturnCode() != IDialogConstants.OK_ID) {
			return false;
		}
		store.setValue(IDEInternalPreferences.CLOSE_UNRELATED_PROJECTS, dialog.getToggleState());
		return true;
	}

	/**
	 * Initializes for the constructor.
	 */
	private void initAction(){
		setId(ID);
		setToolTipText(IDEWorkbenchMessages.CloseUnrelatedProjectsAction_toolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IIDEHelpContextIds.CLOSE_UNRELATED_PROJECTS_ACTION);
	}

	@Override
	protected void clearCache() {
		super.clearCache();
		oldSelection = Collections.emptyList();
		selectionDirty = true;
	}

	/**
	 * Computes the related projects of the selection.
	 */
	private List<IResource> computeRelated(List<? extends IResource> selection) {
		if (selection.contains(ResourcesPlugin.getWorkspace().getRoot())) {
			return new ArrayList<>();
		}
		Set<IProject> selectedProjects = new HashSet<>();
		for (IResource resource : selection) {
			IProject project = resource.getProject();
			if (project != null) {
				selectedProjects.add(project);
			}
		}
		// a component without a selected project is unrelated, so all of it can be
		// closed; the components come from the cache and resolve no references here
		List<IResource> projects = new ArrayList<>();
		for (List<IProject> component : ProjectReferenceGraph.getInstance().getComponents(enablementRefresher)) {
			if (Collections.disjoint(component, selectedProjects)) {
				projects.addAll(component);
			}
		}
		return projects;
	}

	private void updateEnablementInUIThread() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(() -> selectionChanged(getStructuredSelection()));
	}

	@Override
	protected List<? extends IResource> getSelectedResources() {
		if (selectionDirty) {
			List<? extends IResource> newSelection = super.getSelectedResources();
			if (!oldSelection.equals(newSelection)) {
				oldSelection = newSelection;
				projectsToClose = computeRelated(newSelection);
			}
			selectionDirty = false;
		}
		return projectsToClose;
	}

	/**
	 * Handles a resource changed event by updating the enablement
	 * when projects change.
	 * <p>
	 * This method overrides the super-type implementation to update
	 * the selection when the open state or description of any project changes.
	 */
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// don't bother looking at delta if selection not applicable
		if (selectionIsOfType(IResource.PROJECT)) {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				IResourceDelta[] projDeltas = delta.getAffectedChildren(IResourceDelta.CHANGED);
				for (IResourceDelta projDelta : projDeltas) {
					//changing either the description or the open state can affect enablement
					if ((projDelta.getFlags() & (IResourceDelta.OPEN | IResourceDelta.DESCRIPTION)) != 0) {
						selectionChanged(getStructuredSelection());
						return;
					}
				}
			}
		}
	}
}
