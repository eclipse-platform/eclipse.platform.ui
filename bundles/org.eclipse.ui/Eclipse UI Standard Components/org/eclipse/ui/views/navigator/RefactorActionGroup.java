package org.eclipse.ui.views.navigator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.*;

/**
 * This is the action group for refactor actions,
 * including global action handlers for copy, paste and delete.
 * 
 * @since 2.0
 */
public class RefactorActionGroup extends ActionGroup {
	private IResourceNavigatorPart navigator;

	private Clipboard clipboard;

	private CopyAction copyAction;
	private DeleteResourceAction deleteResourceAction;
	private MoveProjectAction moveProjectAction;
	private PasteAction pasteAction;
	private ResourceNavigatorRenameAction renameResourceAction;
	private ResourceNavigatorMoveAction moveResourceAction;
	private TextActionHandler textActionHandler;

	public RefactorActionGroup(IResourceNavigatorPart navigator) {
		this.navigator = navigator;
		makeActions();
	}

	private void makeActions() {
		TreeViewer treeViewer = (TreeViewer) navigator.getResourceViewer();
		Shell shell = navigator.getSite().getShell();
		clipboard = new Clipboard(shell.getDisplay());
		copyAction = new CopyAction(shell, clipboard);
		pasteAction = new PasteAction(shell, clipboard);
		moveResourceAction = new ResourceNavigatorMoveAction(shell, treeViewer);
		moveProjectAction = new MoveProjectAction(shell);
		renameResourceAction = new ResourceNavigatorRenameAction(shell, treeViewer);
		deleteResourceAction = new DeleteResourceAction(shell);
	}

	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();

		boolean anyResourceSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(
					selection,
					IResource.PROJECT | IResource.FOLDER | IResource.FILE);
		boolean onlyFoldersOrFilesSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(
					selection,
					IResource.FOLDER | IResource.FILE);
		boolean onlyProjectsSelected =
			!selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT);

		if (onlyFoldersOrFilesSelected) {
			moveResourceAction.selectionChanged(selection);
			menu.add(moveResourceAction);
		} else if (onlyProjectsSelected) {
			moveProjectAction.selectionChanged(selection);
			menu.add(moveProjectAction);
		}

		copyAction.selectionChanged(selection);
		menu.add(copyAction);
		pasteAction.selectionChanged(selection);
		menu.add(pasteAction);

		if (anyResourceSelected) {
			renameResourceAction.selectionChanged(selection);
			menu.add(renameResourceAction);
			deleteResourceAction.selectionChanged(selection);
			menu.add(deleteResourceAction);
		}
	}

	public void fillActionBars(IActionBars actionBars) {
		textActionHandler = new TextActionHandler(actionBars);
		textActionHandler.setCopyAction(copyAction);
		textActionHandler.setPasteAction(pasteAction);
		textActionHandler.setDeleteAction(deleteResourceAction);
		renameResourceAction.setTextActionHandler(textActionHandler);
	}

	public void updateActionBars() {
		IStructuredSelection selection =
			(IStructuredSelection) getContext().getSelection();

		copyAction.selectionChanged(selection);
		pasteAction.selectionChanged(selection);
		deleteResourceAction.selectionChanged(selection);
	}
	
	/**
 	 * Handles a key pressed event by invoking the appropriate action.
 	 */
	public void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (deleteResourceAction.isEnabled()) {
				deleteResourceAction.run();
			}
		}
		else if (event.keyCode == SWT.F2 && event.stateMask == 0) {
			if (renameResourceAction.isEnabled()) {
				renameResourceAction.run();
			}
		}
	}
	
	public void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}
	}
}