package org.eclipse.ui.views.internal.navigator;

import org.eclipse.core.resources.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.views.navigator.SelectionUtil;

/**
 * The WorkbenchStateActionContributionFactory is the 
 * factory oject that handles actions that open, close and 
 * build projects or the workspace and refresh options.
 */

public class WorkbenchStateActionContributionFactory
	extends ActionContributionFactory {

	protected BuildAction buildAction;
	protected BuildAction rebuildAllAction;
	protected OpenResourceAction openProjectAction;
	protected CloseResourceAction closeProjectAction;
	protected RefreshAction localRefreshAction;

	protected Control control;

	public WorkbenchStateActionContributionFactory(Control parentControl) {
		this.control = parentControl;
	}

	/*
	 * @see ActionContributionFactory#updateActions(IStructuredSelection)
	 */
	public void updateActions(IStructuredSelection selection) {
		buildAction.selectionChanged(selection);
		rebuildAllAction.selectionChanged(selection);
		closeProjectAction.selectionChanged(selection);
		localRefreshAction.selectionChanged(selection);
	}

	/*
	 * @see ActionContributionFactory#makeActions()
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
	 * @see ActionContributionFactory#fillMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillMenu(IMenuManager menu, IStructuredSelection selection) {
		boolean onlyProjectsSelected =
			!selection.isEmpty()
				&& SelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT);

		if (onlyProjectsSelected) {
			menu.add(openProjectAction);
			menu.add(closeProjectAction);
			// Allow manual incremental build only if auto build is off.
			if (!ResourcesPlugin.getWorkspace().isAutoBuilding())
				menu.add(buildAction);
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
	 * Add in a key listener for any actions that listen
	 * to the key strokes.
	 */

	public void addKeyListeners() {

		control.addKeyListener(localRefreshAction);
	}

}