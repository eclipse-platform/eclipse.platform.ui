/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.actions.MoveResourceAction;
import org.eclipse.ui.actions.RenameResourceAction;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.team.internal.ui.Policy;

/**
 * This action group is modeled after the class of the same name in 
 * the org.eclipse.ui.workbench plugin. We couldn't reuse that class
 * because of a hard dependency on the navigator.
 */
public class RefactorActionGroup extends SyncViewerActionGroup {

	private Clipboard clipboard;

	private CopyAction copyAction;
	private DeleteResourceAction deleteAction;
	private PasteAction pasteAction;
	private MoveResourceAction moveAction;
	private RenameResourceAction renameAction;
	private TextActionHandler textActionHandler;

	protected RefactorActionGroup(SynchronizeView syncView) {
		super(syncView);
		makeActions();
	}

	public void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}
		super.dispose();
	}

	public void fillContextMenu(IMenuManager parentMenu) {
		IStructuredSelection selection = getSelection();

		boolean anyResourceSelected =
			!selection.isEmpty()
				&& allResourcesAreOfType(
					selection,
					IResource.PROJECT | IResource.FOLDER | IResource.FILE);

		MenuManager menu = new MenuManager(Policy.bind("RefactorActionGroup.0")); //$NON-NLS-1$
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
		parentMenu.add(menu);
	}

	public void fillActionBars(IActionBars actionBars) {
		textActionHandler = new TextActionHandler(actionBars); // hooks handlers
		textActionHandler.setCopyAction(copyAction);
		textActionHandler.setPasteAction(pasteAction);
		textActionHandler.setDeleteAction(deleteAction);
		renameAction.setTextActionHandler(textActionHandler);
		
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.MOVE, moveAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.RENAME, renameAction);
		actionBars.setGlobalActionHandler(IWorkbenchActionConstants.DELETE, deleteAction);		
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
	}

	protected void makeActions() {
		Shell shell = getSyncView().getSite().getShell();
		clipboard = new Clipboard(shell.getDisplay());
		
		pasteAction = new PasteAction(shell, clipboard);
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		pasteAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		pasteAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		pasteAction.setHoverImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_HOVER));
				
		copyAction = new CopyAction(shell, clipboard, pasteAction);
		copyAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		copyAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyAction.setHoverImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_HOVER));
		
		moveAction = new MoveResourceAction(shell);
		renameAction = new RenameResourceAction(shell);
		
		deleteAction = new DeleteResourceAction(shell);
		deleteAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		deleteAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));		
		deleteAction.setHoverImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_HOVER));
	}

	public void updateActionBars() {
		IStructuredSelection selection = getSelection();
		copyAction.selectionChanged(selection);
		pasteAction.selectionChanged(selection);
		deleteAction.selectionChanged(selection);
		moveAction.selectionChanged(selection);
		renameAction.selectionChanged(selection);
	}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection)getSyncView().getSelection();
	}

	private boolean allResourcesAreOfType(IStructuredSelection selection, int resourceMask) {
		Iterator resources = selection.iterator();
		while (resources.hasNext()) {
			Object next = resources.next();
			IResource resource = null;
			if (next instanceof IResource) {
				resource = (IResource)next;
			} else if (next instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable)next;
				resource = (IResource)adaptable.getAdapter(IResource.class);
			}
			if (resource == null || (resource.getType() & resourceMask) == 0) {
				return false;
			}
		}
		return true;
	}
}
