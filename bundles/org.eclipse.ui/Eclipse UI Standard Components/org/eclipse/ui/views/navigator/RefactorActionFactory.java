package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.views.navigator.*;

/**
 * The RefactorActionFactory is the factory for
 * creating the actions for resources that change its physical
 * location such as rename, move and copy.
 * It also handles referencing actions like the addBookmark
 * action.
 */

public class RefactorActionFactory
	extends ActionFactory {

	protected CopyResourceAction copyResourceAction;
	protected DeleteResourceAction deleteResourceAction;
	protected ResourceNavigatorRenameAction renameResourceAction;
	protected ResourceNavigatorMoveAction moveResourceAction;
	protected CopyProjectAction copyProjectAction;
	protected MoveProjectAction moveProjectAction;
	protected AddBookmarkAction addBookmarkAction;

	protected IWorkbenchPartSite partSite;
	protected TreeViewer treeViewer;

	/**
	 * Creates a new instance of the receiver using a 
	 * tree viewer and a workbench part site.
	 * 
	 * @param viewer the tree viewer
	 * @param site the workbench part site
	 */
	public RefactorActionFactory(TreeViewer viewer, IWorkbenchPartSite site) {
		partSite = site;
		treeViewer = viewer;
	}

	/*
	 * @see ActionFactory#makeActions()
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
	 * @see ActionFactory#fillPopUpMenu(IMenuManager,IStructuredSelection)
	 */
	public void fillPopUpMenu(IMenuManager menu, IStructuredSelection selection) {
		
		//Update the selections of those who need a refresh before filling
		
		copyResourceAction.selectionChanged(selection);
		moveResourceAction.selectionChanged(selection);
		renameResourceAction.selectionChanged(selection);
		copyProjectAction.selectionChanged(selection);
		moveProjectAction.selectionChanged(selection);
		
		boolean anyResourceSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(
					selection,
					IResource.PROJECT | IResource.FOLDER | IResource.FILE);
		boolean onlyFilesSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.FILE);
		boolean onlyFoldersOrFilesSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(
					selection,
					IResource.FOLDER | IResource.FILE);
		boolean onlyProjectsSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT);

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
 	 * Handle a key release.
 	 */
	public void handleKeyReleased(KeyEvent event) {
		deleteResourceAction.handleKeyReleased(event);
		renameResourceAction.handleKeyReleased(event);
	}
}