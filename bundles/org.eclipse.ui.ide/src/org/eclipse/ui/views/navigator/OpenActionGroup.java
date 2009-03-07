/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenInNewWindowAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.internal.views.navigator.ResourceNavigatorMessages;

/**
 * This is the action group for the open actions.
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public class OpenActionGroup extends ResourceNavigatorActionGroup {

    private OpenFileAction openFileAction;

    /**
     * The id for the Open With submenu.
     */
    public static final String OPEN_WITH_ID = PlatformUI.PLUGIN_ID
            + ".OpenWithSubMenu"; //$NON-NLS-1$

	/**
	 * Creates a new action group for open actions.
	 * 
	 * @param navigator the resource navigator
	 */
    public OpenActionGroup(IResourceNavigator navigator) {
        super(navigator);
    }

    protected void makeActions() {
        openFileAction = new OpenFileAction(navigator.getSite().getPage());
    }

    public void fillContextMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) getContext()
                .getSelection();

        boolean anyResourceSelected = !selection.isEmpty()
                && ResourceSelectionUtil.allResourcesAreOfType(selection,
                        IResource.PROJECT | IResource.FOLDER | IResource.FILE);
        boolean onlyFilesSelected = !selection.isEmpty()
                && ResourceSelectionUtil.allResourcesAreOfType(selection,
                        IResource.FILE);

        if (onlyFilesSelected) {
            openFileAction.selectionChanged(selection);
            menu.add(openFileAction);
            fillOpenWithMenu(menu, selection);
        }

        if (anyResourceSelected) {
            addNewWindowAction(menu, selection);
        }
    }

    /**
     * Adds the OpenWith submenu to the context menu.
     * 
     * @param menu the context menu
     * @param selection the current selection
     */
    private void fillOpenWithMenu(IMenuManager menu,
            IStructuredSelection selection) {

        // Only supported if exactly one file is selected.
        if (selection.size() != 1) {
			return;
		}
        Object element = selection.getFirstElement();
        if (!(element instanceof IFile)) {
			return;
		}

        MenuManager submenu = new MenuManager(ResourceNavigatorMessages.ResourceNavigator_openWith, OPEN_WITH_ID);
        submenu.add(new OpenWithMenu(navigator.getSite().getPage(),
                (IFile) element));
        menu.add(submenu);
    }

    /**
     * Adds the Open in New Window action to the context menu.
     * 
     * @param menu the context menu
     * @param selection the current selection
     */
    private void addNewWindowAction(IMenuManager menu,
            IStructuredSelection selection) {

        // Only supported if exactly one container (i.e open project or folder) is selected.
        if (selection.size() != 1) {
			return;
		}
        Object element = selection.getFirstElement();
        if (!(element instanceof IContainer)) {
			return;
		}
        if (element instanceof IProject && !(((IProject) element).isOpen())) {
			return;
		}

        menu.add(new OpenInNewWindowAction(navigator.getSite()
                .getWorkbenchWindow(), (IContainer) element));
    }

    /**
     * Runs the default action (open file).
     */
    public void runDefaultAction(IStructuredSelection selection) {
        Object element = selection.getFirstElement();
        if (element instanceof IFile) {
            openFileAction.selectionChanged(selection);
            openFileAction.run();
        }
    }
}
