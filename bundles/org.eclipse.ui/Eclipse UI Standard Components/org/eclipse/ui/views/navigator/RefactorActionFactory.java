package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;

/**
 * The RefactorActionFactory is the factory for
 * creating the actions for resources that change its physical
 * location such as rename, move and copy.
 * It also handles referencing actions like the addBookmark
 * action.
 * 
 * @since 2.0
 * @deprecated use RefactorActionGroup
 */
public class RefactorActionFactory
	extends ActionFactory {

	protected CopyAction copyAction;
	protected PasteAction pasteAction;
	protected DeleteResourceAction deleteResourceAction;
	protected ResourceNavigatorRenameAction renameResourceAction;
	protected ResourceNavigatorMoveAction moveResourceAction;
	protected MoveProjectAction moveProjectAction;
	protected AddBookmarkAction addBookmarkAction;

	protected IViewSite viewSite;
	protected TreeViewer treeViewer;
	protected Clipboard clipboard;

	private TextActionHandler textActionHandler;

	/**
	 * Creates a new instance of the receiver using a 
	 * tree viewer and a view site.
	 * 
	 * @param viewer the tree viewer
	 * @param site the view site
	 * @deprecated
	 */
	public RefactorActionFactory(TreeViewer viewer, IViewSite site, Clipboard clipboard) {
		treeViewer = viewer;
		viewSite = site;
		this.clipboard = clipboard;
	}

	/*
	 * @see ActionFactory#makeActions()
	 */
	public void makeActions() {

		Shell shell = getShell();
		copyAction = new CopyAction(shell, clipboard);
		pasteAction = new PasteAction(shell, clipboard);
		moveResourceAction = new ResourceNavigatorMoveAction(shell, treeViewer);
		moveProjectAction = new MoveProjectAction(shell);
		renameResourceAction = new ResourceNavigatorRenameAction(shell, treeViewer);
		deleteResourceAction = new DeleteResourceAction(shell);
		addBookmarkAction = new AddBookmarkAction(shell);

		viewSite.getActionBars().setGlobalActionHandler(
			IWorkbenchActionConstants.BOOKMARK,
			addBookmarkAction);
		
		textActionHandler = new TextActionHandler(viewSite.getActionBars());
		textActionHandler.setCopyAction(copyAction);
		textActionHandler.setPasteAction(pasteAction);
		textActionHandler.setDeleteAction(deleteResourceAction);
		renameResourceAction.setTextActionHandler(textActionHandler);
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
		
		copyAction.selectionChanged(selection);
		pasteAction.selectionChanged(selection);	
		moveResourceAction.selectionChanged(selection);
		moveProjectAction.selectionChanged(selection);
		renameResourceAction.selectionChanged(selection);
		
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
			menu.add(moveResourceAction);
		} else if (onlyProjectsSelected) {
			menu.add(moveProjectAction);
		}

		menu.add(copyAction);
		menu.add(pasteAction);
		
		if (anyResourceSelected) {
			menu.add(renameResourceAction);
			menu.add(deleteResourceAction);
		}
		if (onlyFilesSelected)
			menu.add(addBookmarkAction);

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
		copyAction.selectionChanged(selection);
		pasteAction.selectionChanged(selection);		
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