/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation 
 *     Sebastian Davids <sdavids@gmx.de> - Images for menu items
************************************************************************/
package org.eclipse.ui.views.navigator;

import java.util.Iterator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.*;

/**
 * This is the action group for workspace actions such as Build, Refresh Local,
 * and Open/Close Project.
 */
public class WorkspaceActionGroup extends ResourceNavigatorActionGroup {

	private BuildAction buildAction;
	private BuildAction rebuildAction;
	private OpenResourceAction openProjectAction;
	private CloseResourceAction closeProjectAction;
	private RefreshAction refreshAction;

	public WorkspaceActionGroup(IResourceNavigator navigator) {
		super(navigator);
	}

	public void dispose() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(openProjectAction);
		workspace.removeResourceChangeListener(closeProjectAction);
		super.dispose();
	}
	
	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.REFRESH,
			refreshAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.BUILD_PROJECT,
			buildAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.REBUILD_PROJECT,
			rebuildAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.OPEN_PROJECT,
			openProjectAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.CLOSE_PROJECT,
			closeProjectAction);
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
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		boolean isProjectSelection = true; 
		boolean hasOpenProjects = false;
		boolean hasClosedProjects = false;
		boolean hasBuilder = true;	// false if any project is closed or does not have builder 
		Iterator resources = selection.iterator();

		while (resources.hasNext() &&
				(!hasOpenProjects || !hasClosedProjects || 
				 hasBuilder || isProjectSelection)) {
			Object next = resources.next();
			IProject project = null;
			
			if (next instanceof IProject)
				project = (IProject) next;
			else if (next instanceof IAdaptable)
				project = (IProject) ((IAdaptable) next).getAdapter(IProject.class);
			
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
		if (!selection.isEmpty() && isProjectSelection && 
			!ResourcesPlugin.getWorkspace().isAutoBuilding() && hasBuilder) {
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
		}
		catch (CoreException e) {
			// Cannot determine if project has builders. Project is closed 
			// or does not exist. Fall through to return false.
		}
		return false;
	}
	
	protected void makeActions() {
		Shell shell = navigator.getSite().getShell();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		openProjectAction = new OpenResourceAction(shell);
		workspace.addResourceChangeListener(openProjectAction, IResourceChangeEvent.POST_CHANGE);
		closeProjectAction = new CloseResourceAction(shell);
		workspace.addResourceChangeListener(closeProjectAction, IResourceChangeEvent.POST_CHANGE);
		refreshAction = new RefreshAction(shell);
		refreshAction.setDisabledImageDescriptor(getImageDescriptor("dlcl16/refresh_nav.gif"));//$NON-NLS-1$
		refreshAction.setImageDescriptor(getImageDescriptor("elcl16/refresh_nav.gif"));//$NON-NLS-1$
		refreshAction.setHoverImageDescriptor(getImageDescriptor("clcl16/refresh_nav.gif"));//$NON-NLS-1$		
		
		buildAction =
			new BuildAction(shell, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		rebuildAction = new BuildAction(shell, IncrementalProjectBuilder.FULL_BUILD);
	}

	public void updateActionBars() {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		refreshAction.selectionChanged(selection);
		buildAction.selectionChanged(selection);
		rebuildAction.selectionChanged(selection);
		openProjectAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);
	}	
}
