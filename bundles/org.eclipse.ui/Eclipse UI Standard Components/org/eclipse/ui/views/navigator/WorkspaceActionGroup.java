package org.eclipse.ui.views.navigator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.actions.*;

/**
 * This is the action group for workspace actions such as Build, Refresh Local,
 * and Open/Close Project.
 */
public class WorkspaceActionGroup extends ActionGroup {

	private IResourceNavigatorPart navigator;
	private BuildAction buildAction;
	private BuildAction rebuildAllAction;
	private OpenResourceAction openProjectAction;
	private CloseResourceAction closeProjectAction;
	private RefreshAction localRefreshAction;

	public WorkspaceActionGroup(IResourceNavigatorPart navigator) {
		this.navigator = navigator;
		makeActions();
	}

	private void makeActions() {
		Shell shell = navigator.getSite().getShell();
		openProjectAction = new OpenResourceAction(shell);
		closeProjectAction = new CloseResourceAction(shell);
		localRefreshAction = new RefreshAction(shell);
		buildAction =
			new BuildAction(shell, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		rebuildAllAction = new BuildAction(shell, IncrementalProjectBuilder.FULL_BUILD);
	}
	
	public void fillContextMenu(IMenuManager menu) {
		// Update the selections of those who need a refresh before filling
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		
		boolean onlyProjectsSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT);

		if (onlyProjectsSelected) {
			openProjectAction.selectionChanged(selection);
			menu.add(openProjectAction);
			closeProjectAction.selectionChanged(selection);
			menu.add(closeProjectAction);
		}
		
		if (!selection.isEmpty()) {
			// Allow manual incremental build only if auto build is off.
			if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
				buildAction.selectionChanged(selection);
				menu.add(buildAction);
			}
			rebuildAllAction.selectionChanged(selection);
			menu.add(rebuildAllAction);
		}
		
		localRefreshAction.selectionChanged(selection);
		menu.add(localRefreshAction);
	}
	
	/**
 	 * Handles a key pressed event by invoking the appropriate action.
 	 */
	public void handleKeyPressed(KeyEvent event) {
		if (event.keyCode == SWT.F5) {
			localRefreshAction.refreshAll();
		}
	}
}
