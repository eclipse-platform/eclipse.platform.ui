/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Images for menu items (27481)
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import java.util.Iterator;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.actions.CloseResourceAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.internal.ide.actions.CloseUnrelatedProjectsAction;

/**
 * This is the action group for workspace actions such as Build, Refresh Local,
 * and Open/Close Project.
 */
public class WorkspaceActionGroup extends ResourceNavigatorActionGroup {

    private BuildAction buildAction;

    private OpenResourceAction openProjectAction;

    private CloseResourceAction closeProjectAction;
    
    private CloseUnrelatedProjectsAction closeUnrelatedProjectsAction;

    private RefreshAction refreshAction;

    public WorkspaceActionGroup(IResourceNavigator navigator) {
        super(navigator);
    }

    public void fillActionBars(IActionBars actionBars) {
        actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(),
                refreshAction);
        actionBars.setGlobalActionHandler(IDEActionFactory.BUILD_PROJECT
                .getId(), buildAction);
        actionBars.setGlobalActionHandler(
                IDEActionFactory.OPEN_PROJECT.getId(), openProjectAction);
        actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_PROJECT
                .getId(), closeProjectAction);
        actionBars.setGlobalActionHandler(IDEActionFactory.CLOSE_UNRELATED_PROJECTS
                .getId(), closeUnrelatedProjectsAction);
    }

    /**
     * Adds the build, open project, close project and refresh resource
     * actions to the context menu.
     * <p>
     * The following conditions apply: 
     * 	build-only projects selected, auto build disabled, at least one 
     * 		builder present
     * 	open project-only projects selected, at least one closed project
     * 	close project-only projects selected, at least one open project
     * 	refresh-no closed project selected
     * </p>
     * <p>
     * Both the open project and close project action may be on the menu
     * at the same time.
     * </p>
     * <p>
     * No disabled action should be on the context menu.
     * </p>
     * 
     * @param menu context menu to add actions to
     */
    public void fillContextMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) getContext()
                .getSelection();
        boolean isProjectSelection = true;
        boolean hasOpenProjects = false;
        boolean hasClosedProjects = false;
        boolean hasBuilder = true; // false if any project is closed or does not have builder 
        Iterator resources = selection.iterator();

        while (resources.hasNext()
                && (!hasOpenProjects || !hasClosedProjects || hasBuilder || isProjectSelection)) {
            Object next = resources.next();
            IProject project = null;

            if (next instanceof IProject)
                project = (IProject) next;
            else if (next instanceof IAdaptable)
                project = (IProject) ((IAdaptable) next)
                        .getAdapter(IProject.class);

            if (project == null) {
                isProjectSelection = false;
                continue;
            }
            if (project.isOpen()) {
                hasOpenProjects = true;
                if (hasBuilder && !hasBuilder(project))
                    hasBuilder = false;
            } else {
                hasClosedProjects = true;
                hasBuilder = false;
            }
        }
        if (!selection.isEmpty() && isProjectSelection
                && !ResourcesPlugin.getWorkspace().isAutoBuilding()
                && hasBuilder) {
            // Allow manual incremental build only if auto build is off.
            buildAction.selectionChanged(selection);
            menu.add(buildAction);
        }
        if (!hasClosedProjects) {
            refreshAction.selectionChanged(selection);
            menu.add(refreshAction);
        }
        if (isProjectSelection) {
            if (hasClosedProjects) {
                openProjectAction.selectionChanged(selection);
                menu.add(openProjectAction);
            }
            if (hasOpenProjects) {
                closeProjectAction.selectionChanged(selection);
                menu.add(closeProjectAction);
                closeUnrelatedProjectsAction.selectionChanged(selection);
                menu.add(closeUnrelatedProjectsAction);
            }
        }
    }

    /**
     * Handles a key pressed event by invoking the appropriate action.
     */
    public void handleKeyPressed(KeyEvent event) {
        if (event.keyCode == SWT.F5 && event.stateMask == 0) {
            if (refreshAction.isEnabled()) {
                refreshAction.refreshAll();
            }

            // Swallow the event
            event.doit = false;
        }
    }

    /**
     * Returns whether there are builders configured on the given project.
     *
     * @return <code>true</code> if it has builders,
     *   <code>false</code> if not, or if this could not be determined
     */
    boolean hasBuilder(IProject project) {
        try {
            ICommand[] commands = project.getDescription().getBuildSpec();
            if (commands.length > 0)
                return true;
        } catch (CoreException e) {
            // Cannot determine if project has builders. Project is closed 
            // or does not exist. Fall through to return false.
        }
        return false;
    }

    protected void makeActions() {
        Shell shell = navigator.getSite().getShell();
        openProjectAction = new OpenResourceAction(shell);
        closeProjectAction = new CloseResourceAction(shell);
        closeUnrelatedProjectsAction = new CloseUnrelatedProjectsAction(shell);
        refreshAction = new RefreshAction(shell);
        refreshAction
                .setDisabledImageDescriptor(getImageDescriptor("dlcl16/refresh_nav.gif"));//$NON-NLS-1$
        refreshAction
                .setImageDescriptor(getImageDescriptor("elcl16/refresh_nav.gif"));//$NON-NLS-1$	
        buildAction = new BuildAction(shell,
                IncrementalProjectBuilder.INCREMENTAL_BUILD);
    }

    public void updateActionBars() {
        IStructuredSelection selection = (IStructuredSelection) getContext()
                .getSelection();
        refreshAction.selectionChanged(selection);
        buildAction.selectionChanged(selection);
        openProjectAction.selectionChanged(selection);
        closeUnrelatedProjectsAction.selectionChanged(selection);
        closeProjectAction.selectionChanged(selection);
    }
}
