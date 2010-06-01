/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.actions.TextActionHandler;
import org.eclipse.ui.ide.ResourceSelectionUtil;
import org.eclipse.ui.navigator.ICommonMenuConstants;

/**
 * @since 3.2
 * 
 */
public class EditActionGroup extends ActionGroup {

	private Clipboard clipboard;

	private CopyAction copyAction;

	private DeleteResourceAction deleteAction;

	private PasteAction pasteAction;

	private TextActionHandler textActionHandler;

	private Shell shell;

	/**
	 * 
	 * @param aShell
	 */
	public EditActionGroup(Shell aShell) {
		shell = aShell;
		makeActions();
	}

	public void dispose() {
		if (clipboard != null) {
			clipboard.dispose();
			clipboard = null;
		}
		super.dispose();
	}

	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		boolean anyResourceSelected = !selection.isEmpty()
				&& ResourceSelectionUtil.allResourcesAreOfType(selection, IResource.PROJECT | IResource.FOLDER | IResource.FILE);

		copyAction.selectionChanged(selection);
		menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, copyAction);
		pasteAction.selectionChanged(selection);
		// menu.insertAfter(copyAction.getId(), pasteAction);
		menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, pasteAction);

		if (anyResourceSelected) {
			deleteAction.selectionChanged(selection);
			// menu.insertAfter(pasteAction.getId(), deleteAction);
			menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, deleteAction);
		}
	}

	public void fillActionBars(IActionBars actionBars) {

		if (textActionHandler == null) {
			textActionHandler = new TextActionHandler(actionBars); // hook
																	// handlers
		}
		textActionHandler.setCopyAction(copyAction);
		textActionHandler.setPasteAction(pasteAction);
		textActionHandler.setDeleteAction(deleteAction);
		// renameAction.setTextActionHandler(textActionHandler);
		updateActionBars();

		textActionHandler.updateActionBars();
	}

	/**
	 * Handles a key pressed event by invoking the appropriate action.
	 * 
	 * @param event
	 *            The Key Event
	 */
	public void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (deleteAction.isEnabled()) {
				deleteAction.run();
			}

			// Swallow the event.
			event.doit = false;
		}
	}

	protected void makeActions() {
		clipboard = new Clipboard(shell.getDisplay());

		pasteAction = new PasteAction(shell, clipboard);
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		pasteAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		pasteAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		pasteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_PASTE);

		copyAction = new CopyAction(shell, clipboard, pasteAction);
		copyAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		copyAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		copyAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_COPY);

		IShellProvider sp = new IShellProvider() {
			public Shell getShell() {
				return shell;
			}
		};

		deleteAction = new DeleteResourceAction(sp);
		deleteAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		deleteAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		deleteAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_DELETE);
	}

	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		copyAction.selectionChanged(selection);
		pasteAction.selectionChanged(selection);
		deleteAction.selectionChanged(selection);
	}
}
