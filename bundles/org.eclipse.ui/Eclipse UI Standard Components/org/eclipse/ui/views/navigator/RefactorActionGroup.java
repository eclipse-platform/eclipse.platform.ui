package org.eclipse.ui.views.navigator;

/**********************************************************************
Copyright (c) 2000, 2001, 2002, International Business Machines Corp and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.actions.TextActionHandler;

/**
 * This is the action group for refactor actions,
 * including global action handlers for copy, paste and delete.
 * 
 * @since 2.0
 */
public class RefactorActionGroup extends ResourceNavigatorActionGroup {
	private Clipboard clipboard;

	private CopyAction copyAction;
	private DeleteResourceAction deleteAction;
	private PasteAction pasteAction;
	private ResourceNavigatorRenameAction renameAction;
	private ResourceNavigatorMoveAction moveAction;
	private TextActionHandler textActionHandler;
	
	public RefactorActionGroup(IResourceNavigator navigator) {
		super(navigator);
	}

	protected void makeActions() {
		TreeViewer treeViewer = (TreeViewer) navigator.getViewer();
		Shell shell = navigator.getSite().getShell();
		clipboard = new Clipboard(shell.getDisplay());
		pasteAction = new PasteAction(shell, clipboard);
		copyAction = new CopyAction(shell, clipboard, pasteAction);
		moveAction = new ResourceNavigatorMoveAction(shell, treeViewer);
		renameAction = new ResourceNavigatorRenameAction(shell, treeViewer);
		deleteAction = new DeleteResourceAction(shell);
	}

	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();

		boolean anyResourceSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(
					selection,
					IResource.PROJECT | IResource.FOLDER | IResource.FILE);

		copyAction.selectionChanged(selection);
		menu.add(copyAction);
		pasteAction.selectionChanged(selection);
		menu.add(pasteAction);

		if (anyResourceSelected) {
			deleteAction.selectionChanged(selection);
			menu.add(deleteAction);
			moveAction.selectionChanged(selection);
			menu.add(moveAction);
			renameAction.selectionChanged(selection);
			menu.add(renameAction);
		}
	}

	public void fillActionBars(IActionBars actionBars) {
		textActionHandler = new TextActionHandler(actionBars); // hooks handlers
		textActionHandler.setCopyAction(copyAction);
		textActionHandler.setPasteAction(pasteAction);
		textActionHandler.setDeleteAction(deleteAction);
		renameAction.setTextActionHandler(textActionHandler);
		
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.MOVE, moveAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.RENAME, renameAction);
	}

	public void updateActionBars() {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();

		copyAction.selectionChanged(selection);
		pasteAction.selectionChanged(selection);
		deleteAction.selectionChanged(selection);
		moveAction.selectionChanged(selection);
		renameAction.selectionChanged(selection);
	}
	
	/**
 	 * Handles a key pressed event by invoking the appropriate action.
 	 */
	public void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (deleteAction.isEnabled()) {
				deleteAction.run();
			}
		}
		else if (event.keyCode == SWT.F2 && event.stateMask == 0) {
			if (renameAction.isEnabled()) {
				renameAction.run();
			}
		}
	}
	
	public void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}
		super.dispose();
	}
}