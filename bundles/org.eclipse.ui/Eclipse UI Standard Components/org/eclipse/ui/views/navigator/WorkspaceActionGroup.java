package org.eclipse.ui.views.navigator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.*;

/**
 * This is the action group for workspace actions such as Build, Refresh Local,
 * and Open/Close Project.
 */
public class WorkspaceActionGroup extends ActionGroup {

	private IResourceNavigatorPart navigator;
	private BuildAction buildAction;
	private BuildAction rebuildAction;
	private OpenResourceAction openProjectAction;
	private CloseResourceAction closeProjectAction;
	private RefreshAction refreshAction;

	public WorkspaceActionGroup(IResourceNavigatorPart navigator) {
		this.navigator = navigator;
		makeActions();
	}

	private void makeActions() {
		Shell shell = navigator.getSite().getShell();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		openProjectAction = new OpenResourceAction(shell);
		workspace.addResourceChangeListener(openProjectAction, IResourceChangeEvent.POST_CHANGE);
		closeProjectAction = new CloseResourceAction(shell);
		workspace.addResourceChangeListener(closeProjectAction, IResourceChangeEvent.POST_CHANGE);
		refreshAction = new RefreshAction(shell);
		buildAction =
			new BuildAction(shell, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		rebuildAction = new BuildAction(shell, IncrementalProjectBuilder.FULL_BUILD);
	}
	
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		
		boolean onlyProjectsSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT);

		if (!selection.isEmpty()) {
			// Allow manual incremental build only if auto build is off.
			if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
				buildAction.selectionChanged(selection);
				menu.add(buildAction);
			}
			rebuildAction.selectionChanged(selection);
			menu.add(rebuildAction);
		}
		
		if (onlyProjectsSelected) {
			openProjectAction.selectionChanged(selection);
			menu.add(openProjectAction);
			closeProjectAction.selectionChanged(selection);
			menu.add(closeProjectAction);
		}
		
		menu.add(new Separator());
		
		refreshAction.selectionChanged(selection);
		menu.add(refreshAction);
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
	
	public void updateActionBars() {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();
		refreshAction.selectionChanged(selection);
		buildAction.selectionChanged(selection);
		rebuildAction.selectionChanged(selection);
		openProjectAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);
	}
	
	/**
 	 * Handles a key pressed event by invoking the appropriate action.
 	 */
	public void handleKeyPressed(KeyEvent event) {
		if (event.keyCode == SWT.F5) {
			refreshAction.refreshAll();
		}
	}
	
	public void dispose() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(openProjectAction);
		workspace.removeResourceChangeListener(closeProjectAction);
	}
}
