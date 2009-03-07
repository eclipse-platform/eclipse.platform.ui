/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *        IBM Corporation - initial API and implementation 
 *        Sebastian Davids <sdavids@gmx.de> - Images for menu items (27481)
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * This is the action group for all the resource navigator actions.
 * It delegates to several subgroups for most of the actions.
 * 
 * @see GotoActionGroup
 * @see OpenActionGroup
 * @see RefactorActionGroup
 * @see SortAndFilterActionGroup
 * @see WorkspaceActionGroup
 * 
 * @since 2.0
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
public abstract class ResourceNavigatorActionGroup extends ActionGroup {

    /**
     * The resource navigator.
     */
    protected IResourceNavigator navigator;
	
    /**
     * Constructs a new navigator action group and creates its actions.
     * 
     * @param navigator the resource navigator
     */
    public ResourceNavigatorActionGroup(IResourceNavigator navigator) {
        this.navigator = navigator;
        makeActions();
    }

    /**
     * Returns the image descriptor with the given relative path.
     */
    protected ImageDescriptor getImageDescriptor(String relativePath) {
       return IDEWorkbenchPlugin.getIDEImageDescriptor(relativePath);
     
    }

    /**
     * Returns the resource navigator.
     */
    public IResourceNavigator getNavigator() {
        return navigator;
    }

    /**
     * Handles a key pressed event by invoking the appropriate action.
     * Does nothing by default.
     */
    public void handleKeyPressed(KeyEvent event) {
    }

    /**
     * Makes the actions contained in this action group.
     */
    protected abstract void makeActions();

    /**
     * Runs the default action in the group.
     * Does nothing by default.
     * 
     * @param selection the current selection
     */
    public void runDefaultAction(IStructuredSelection selection) {
    }

}
