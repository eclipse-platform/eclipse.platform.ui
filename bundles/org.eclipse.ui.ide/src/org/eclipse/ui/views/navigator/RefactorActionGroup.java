/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Images for menu items (27481)
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.actions.TextActionHandler;

/**
 * This is the action group for refactor actions,
 * including global action handlers for copy, paste and delete.
 * 
 * @since 2.0
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
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

    public void dispose() {
        if (clipboard != null) {
            clipboard.dispose();
            clipboard = null;
        }
        super.dispose();
    }

    public void fillContextMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) getContext()
                .getSelection();

        boolean anyResourceSelected = !selection.isEmpty()
                && ResourceSelectionUtil.allResourcesAreOfType(selection,
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

        actionBars.setGlobalActionHandler(ActionFactory.MOVE.getId(),
                moveAction);
        actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(),
                renameAction);
    }

    /**
     * Handles a key pressed event by invoking the appropriate action.
     */
    public void handleKeyPressed(KeyEvent event) {
        if (event.character == SWT.DEL && event.stateMask == 0) {
            if (deleteAction.isEnabled()) {
                deleteAction.run();
            }

            // Swallow the event.
            event.doit = false;

        } else if (event.keyCode == SWT.F2 && event.stateMask == 0) {
            if (renameAction.isEnabled()) {
                renameAction.run();
            }

            // Swallow the event.
            event.doit = false;
        }
    }

    protected void makeActions() {
        TreeViewer treeViewer = navigator.getViewer();
        IShellProvider provider = navigator.getSite();
        clipboard = new Clipboard(provider.getShell().getDisplay());

        pasteAction = new PasteAction(provider.getShell(), clipboard);
        ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
        pasteAction.setDisabledImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
        pasteAction.setImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));

        copyAction = new CopyAction(provider.getShell(), clipboard, pasteAction);
        copyAction.setDisabledImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
        copyAction.setImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

        moveAction = new ResourceNavigatorMoveAction(provider.getShell(), treeViewer);
        renameAction = new ResourceNavigatorRenameAction(provider.getShell(), treeViewer);

        deleteAction = new DeleteResourceAction(provider);
        deleteAction.setDisabledImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
        deleteAction.setImageDescriptor(images
                .getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
    }

    public void updateActionBars() {
        IStructuredSelection selection = (IStructuredSelection) getContext()
                .getSelection();

        copyAction.selectionChanged(selection);
        pasteAction.selectionChanged(selection);
        deleteAction.selectionChanged(selection);
        moveAction.selectionChanged(selection);
        renameAction.selectionChanged(selection);
    }

}
