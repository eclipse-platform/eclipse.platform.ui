package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.*;

/**
 * The RefactorActionContributionFactory is the factory for
 * creating the actions for resources that change its physical
 * location such as rename, move and copy.
 * It also handles referencing actions like the addBookmark
 * action.
 */

public class RefactorActionContributionFactory
	extends ActionContributionFactory {

	protected CopyResourceAction copyResourceAction;
	protected DeleteResourceAction deleteResourceAction;
	protected ResourceNavigatorRenameAction renameResourceAction;
	protected ResourceNavigatorMoveAction moveResourceAction;
	protected CopyProjectAction copyProjectAction;
	protected MoveProjectAction moveProjectAction;
	protected AddBookmarkAction addBookmarkAction;

	protected TreeViewer treeViewer;

	/**
	 * Create a new instance of the receiver using a 
	 * TreeViewer.
	 * @param viewer TreeViewer
	 */
	public RefactorActionContributionFactory(TreeViewer viewer) {
		treeViewer = viewer;
	}

	/*
	 * @see ActionContributionFactory#updateActions(IStructuredSelection)
	 */
	public void updateActions(IStructuredSelection selection) {
		copyResourceAction.selectionChanged(selection);
		moveResourceAction.selectionChanged(selection);
		renameResourceAction.selectionChanged(selection);
		copyProjectAction.selectionChanged(selection);
		moveProjectAction.selectionChanged(selection);
	}

	/*
	 * @see ActionContributionFactory#makeActions()
	 */
	public void makeActions() {

		Shell shell = getShell();
		moveResourceAction = new ResourceNavigatorMoveAction(shell, treeViewer);
		copyResourceAction = new CopyResourceAction(shell);
		moveProjectAction = new MoveProjectAction(shell);
		copyProjectAction = new CopyProjectAction(shell);
		renameResourceAction = new ResourceNavigatorRenameAction(shell, treeViewer);
		deleteResourceAction = new DeleteResourceAction(shell);
		addBookmarkAction = new AddBookmarkAction(shell);
	}

	/**
	 * Get the shell of the viewer that these actions are
	 * being created on.
	 * @returns Shell
	 */

	protected Shell getShell() {
		return treeViewer.getTree().getShell();
	}

	/*
	 * @see ActionContributionFactory#fillMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillMenu(IMenuManager menu, IStructuredSelection selection) {
		boolean anyResourceSelected =
			!selection.isEmpty()
				&& SelectionUtil.allResourcesAreOfType(
					selection,
					IResource.PROJECT | IResource.FOLDER | IResource.FILE);
		boolean onlyFilesSelected =
			!selection.isEmpty()
				&& SelectionUtil.allResourcesAreOfType(selection, IResource.FILE);
		boolean onlyFoldersOrFilesSelected =
			!selection.isEmpty()
				&& SelectionUtil.allResourcesAreOfType(
					selection,
					IResource.FOLDER | IResource.FILE);
		boolean onlyProjectsSelected =
			!selection.isEmpty()
				&& SelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT);

		if (onlyFoldersOrFilesSelected) {
			menu.add(copyResourceAction);
			menu.add(moveResourceAction);
		} else if (onlyProjectsSelected) {
			menu.add(copyProjectAction);
			menu.add(moveProjectAction);
		}
		if (anyResourceSelected) {
			menu.add(renameResourceAction);
			menu.add(deleteResourceAction);
		}
		if (onlyFilesSelected)
			menu.add(addBookmarkAction);

	}

	/**
	 * Add actions that need to be part of a global
	 * action handler
	 */

	public void addGlobalActions(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.DELETE,
			deleteResourceAction);
		actionBars.setGlobalActionHandler(
			IWorkbenchActionConstants.BOOKMARK,
			addBookmarkAction);
	}
	
	/**
	 * Updates the global actions with the given selection and action bars.
	 * This is invoked at different times from updateActions
	 * as global updating is more frequent.
	 * @param selection IStructuredSelection - the new selected items
	 * @param actionBars IActionBars - the action bars that need an update
	 */
	public void updateGlobalActions(IStructuredSelection selection, IActionBars actionBars) {
		deleteResourceAction.selectionChanged(selection);
		addBookmarkAction.selectionChanged(selection);

		// Ensure Copy global action targets correct action,
		// either copyProjectAction or copyResourceAction,
		// depending on selection.
		copyProjectAction.selectionChanged(selection);
		copyResourceAction.selectionChanged(selection);
		if (copyProjectAction.isEnabled())
			actionBars.setGlobalActionHandler(
				IWorkbenchActionConstants.COPY,
				copyProjectAction);
		else
			actionBars.setGlobalActionHandler(
				IWorkbenchActionConstants.COPY,
				copyResourceAction);
		renameResourceAction.selectionChanged(selection);
	}
	
	/**
	 * Add in a key listener for any actions that listen
	 * to the key strokes.
	 */

	public void addKeyListeners() {

		treeViewer.getControl().addKeyListener(deleteResourceAction);
		treeViewer.getControl().addKeyListener(renameResourceAction);
	}
}