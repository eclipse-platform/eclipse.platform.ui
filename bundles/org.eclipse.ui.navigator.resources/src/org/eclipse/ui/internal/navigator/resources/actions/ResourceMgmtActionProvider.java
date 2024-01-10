/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
 *     Lucas Bullen (Red Hat Inc.) - Bug 522096 - "Close Projects" on working set
 *******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.CloseUnrelatedProjectsAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * @since 3.2
 */
public class ResourceMgmtActionProvider extends CommonActionProvider {

	private BuildAction buildAction;

	private OpenResourceAction openProjectAction;

	private CloseResourceAction closeProjectAction;

	private CloseUnrelatedProjectsAction closeUnrelatedProjectsAction;

	private RefreshAction refreshAction;

	private Shell shell;

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		shell = aSite.getViewSite().getShell();
		makeActions();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.BUILD_PROJECT.getId(), buildAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.OPEN_PROJECT.getId(), openProjectAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_PROJECT.getId(), closeProjectAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_UNRELATED_PROJECTS.getId(), closeUnrelatedProjectsAction);
		updateActionBars();
	}

	/**
	 * Adds the build, open project, close project and refresh resource actions
	 * to the context menu.
	 * <p>
	 * The following conditions apply: build-only projects selected, auto build
	 * disabled, at least one builder present open project-only projects
	 * selected, at least one closed project close project-only projects
	 * selected, at least one open project refresh-no closed project selected
	 * </p>
	 * <p>
	 * Both the open project and close project action may be on the menu at the
	 * same time.
	 * </p>
	 * <p>
	 * No disabled action should be on the context menu.
	 * </p>
	 *
	 * @param menu
	 *            context menu to add actions to
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		boolean isProjectSelection = true;
		boolean hasOpenProjects = false;
		boolean hasClosedProjects = false;
		boolean hasBuilder = true; // false if any project is closed or does not
									// have builder

		Iterator<IProject> projects = selectionToProjects(selection).iterator();

		while (projects.hasNext() && (!hasOpenProjects || !hasClosedProjects || hasBuilder || isProjectSelection)) {
			IProject project = projects.next();

			if (project == null) {
				isProjectSelection = false;
				continue;
			}
			if (project.isOpen()) {
				hasOpenProjects = true;
				if (hasBuilder && !hasBuilder(project)) {
					hasBuilder = false;
				}
			} else {
				hasClosedProjects = true;
				hasBuilder = false;
			}
		}
		if (!selection.isEmpty() && isProjectSelection && !ResourcesPlugin.getWorkspace().isAutoBuilding()
				&& hasBuilder) {
			// Allow manual incremental build only if auto build is off.
			buildAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, buildAction);
		}
		// To refresh, even if one project is open
		if (hasOpenProjects) {
			refreshAction.selectionChanged(selection);
			menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, refreshAction);
		}
		if (isProjectSelection) {
			if (hasClosedProjects) {
				openProjectAction.selectionChanged(selection);
				menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, openProjectAction);
			}
			if (hasOpenProjects) {
				closeProjectAction.selectionChanged(selection);
				menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, closeProjectAction);
				closeUnrelatedProjectsAction.selectionChanged(selection);
				menu.appendToGroup(ICommonMenuConstants.GROUP_BUILD, closeUnrelatedProjectsAction);
			}
		}
	}

	private static List<IProject> selectionToProjects(IStructuredSelection selection) {
		if (selection == null) {
			return Collections.emptyList();
		}
		List<IProject> resources = new ArrayList<>();
		for (Object currentObject : selection) {
			if (currentObject instanceof IWorkingSet) {
				IWorkingSet workingSet = (IWorkingSet) currentObject;
				for (IAdaptable element : workingSet.getElements()) {
					IProject project = element.getAdapter(IProject.class);
					if (project != null) {
						resources.add(project);
					}
				}
			} else if (currentObject instanceof IAdaptable) {
				IProject resource = ((IAdaptable) currentObject).getAdapter(IProject.class);
				if (resource != null) {
					resources.add(resource);
				}
			}
		}
		return resources;
	}

	/**
	 * Returns whether there are builders configured on the given project.
	 *
	 * @return <code>true</code> if it has builders, <code>false</code> if not,
	 *         or if this could not be determined
	 */
	boolean hasBuilder(IProject project) {
		try {
			ICommand[] commands = project.getDescription().getBuildSpec();
			if (commands.length > 0) {
				return true;
			}
		} catch (CoreException e) {
			// Cannot determine if project has builders. Project is closed
			// or does not exist. Fall through to return false.
		}
		return false;
	}

	protected void makeActions() {
		IShellProvider sp = () -> shell;

		openProjectAction = new OpenResourceAction(sp);

		closeProjectAction = new CloseResourceAction(sp);

		closeUnrelatedProjectsAction = new CloseUnrelatedProjectsAction(sp);

		refreshAction = new RefreshAction(sp) {
			@Override
			public void run() {
				final IStatus[] errorStatus = new IStatus[1];
				errorStatus[0] = Status.OK_STATUS;
				final WorkspaceModifyOperation op = (WorkspaceModifyOperation) createOperation(errorStatus);
				WorkspaceJob job = new WorkspaceJob("refresh") { //$NON-NLS-1$

					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
						try {
							op.run(monitor);
							if (shell != null && !shell.isDisposed()) {
								shell.getDisplay().asyncExec(() -> {
									StructuredViewer viewer = getActionSite().getStructuredViewer();
									if (viewer != null && viewer.getControl() != null
											&& !viewer.getControl().isDisposed()) {
										viewer.refresh();
									}
								});
							}
						} catch (InvocationTargetException e) {
							String msg = NLS.bind(WorkbenchNavigatorMessages.ResourceMgmtActionProvider_logTitle, getClass().getName(), e.getTargetException());
							throw new CoreException(new Status(IStatus.ERROR, NavigatorPlugin.PLUGIN_ID, IStatus.ERROR, msg, e.getTargetException()));
						} catch (InterruptedException e) {
							return Status.CANCEL_STATUS;
						}
						return errorStatus[0];
					}

				};
				ISchedulingRule rule = op.getRule();
				if (rule != null) {
					job.setRule(rule);
				}
				job.setUser(true);
				job.schedule();
			}
		};
		refreshAction.setDisabledImageDescriptor(getImageDescriptor("dlcl16/refresh_nav.png"));//$NON-NLS-1$
		refreshAction.setImageDescriptor(getImageDescriptor("elcl16/refresh_nav.png"));//$NON-NLS-1$
		refreshAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_REFRESH);

		buildAction = new BuildAction(sp, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		buildAction.setActionDefinitionId(IWorkbenchCommandConstants.PROJECT_BUILD_PROJECT);
	}

	/**
	 * Returns the image descriptor with the given relative path.
	 */
	protected ImageDescriptor getImageDescriptor(String relativePath) {
		return IDEWorkbenchPlugin.getIDEImageDescriptor(relativePath);

	}

	@Override
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();
		refreshAction.selectionChanged(selection);
		buildAction.selectionChanged(selection);
		openProjectAction.selectionChanged(selection);
		closeUnrelatedProjectsAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);
	}

}
