package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.views.navigator.ResourceSelectionUtil;

/**
 * The WorkbenchStateActionFactory is the 
 * factory oject that handles actions that open, close and 
 * build projects or the workspace and refresh options.
 * 
 * @deprecated use WorkspaceActionGroup
 */
public class WorkbenchStateActionFactory
	extends ActionFactory {

	protected BuildAction buildAction;
	protected BuildAction rebuildAllAction;
	protected OpenResourceAction openProjectAction;
	protected CloseResourceAction closeProjectAction;
	protected RefreshAction localRefreshAction;

	protected Control control;

	/**
	 * @deprecated
	 */
	public WorkbenchStateActionFactory(Control parentControl) {
		this.control = parentControl;
	}

	/*
	 * @see ActionFactory#makeActions()
	 */
	public void makeActions() {

		Shell shell = control.getShell();
		openProjectAction = new OpenResourceAction(shell);
		closeProjectAction = new CloseResourceAction(shell);
		localRefreshAction = new RefreshAction(shell);
		buildAction =
			new BuildAction(shell, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		rebuildAllAction = new BuildAction(shell, IncrementalProjectBuilder.FULL_BUILD);

	}

	/*
	 * @see ActionFactory#fillMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillPopUpMenu(IMenuManager menu, IStructuredSelection selection) {
		
		//Update the selections of those who need a refresh before filling
		
		buildAction.selectionChanged(selection);
		rebuildAllAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);
		localRefreshAction.selectionChanged(selection);
		
		boolean onlyProjectsSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT);

		if (onlyProjectsSelected) {
			menu.add(openProjectAction);
			menu.add(closeProjectAction);
		}
		
		if (!selection.isEmpty()) {
			// Allow manual incremental build only if auto build is off.
			if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
				menu.add(buildAction);
			}
			menu.add(rebuildAllAction);
		}
		
		menu.add(localRefreshAction);
	}

	/**
	 * Updates the global actions with the given selection.
	 * @param IStructuredSelection selection - the new items
	 */
	public void updateGlobalActions(IStructuredSelection selection) {
		openProjectAction.selectionChanged(selection);
	}

	/**
 	 * Handle a key release.
 	 */
	public void handleKeyReleased(KeyEvent event) {
		localRefreshAction.handleKeyReleased(event);
	}

}