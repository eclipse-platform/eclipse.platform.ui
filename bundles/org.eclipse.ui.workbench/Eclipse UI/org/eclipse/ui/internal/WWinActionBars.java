/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.services.IServiceLocator;

public class WWinActionBars implements IActionBars2 {
    private WorkbenchWindow window;

    /**
     * PerspActionBars constructor comment.
     */
    public WWinActionBars(WorkbenchWindow window) {
        super();
        this.window = window;
    }

    /**
     * Clears the global action handler list.
     */
    public void clearGlobalActionHandlers() {
    }

    /**
     * Returns the cool bar manager.
     * 
     */
    public ICoolBarManager getCoolBarManager() {
        return window.getCoolBarManager2();
    }

    /**
     * Get the handler for a window action.
     *
     * @param actionID an action ID declared in the registry
     * @return an action handler which implements the action ID, or
     *		<code>null</code> if none is registered.
     */
    public IAction getGlobalActionHandler(String actionID) {
        return null;
    }

    /**
     * Returns the menu manager.  If items are added or
     * removed from the manager be sure to call <code>updateActionBars</code>.
     *
     * @return the menu manager
     */
    public IMenuManager getMenuManager() {
        return window.getMenuManager();
    }

	public final IServiceLocator getServiceLocator() {
		return window;
	}

    /**
     * Returns the status line manager.  If items are added or
     * removed from the manager be sure to call <code>updateActionBars</code>.
     *
     * @return the status line manager
     */
    public IStatusLineManager getStatusLineManager() {
        return window.getStatusLineManager();
    }

    /**
     * Returns the tool bar manager.
     * 
     */
    public IToolBarManager getToolBarManager() {
        // This should never be called
        Assert.isTrue(false);
        return null;
    }

    /**
     * Add a handler for a window action.
     *
     * The standard action ID's for the workbench are defined in
     * <code>IWorkbenchActions</code>.
     *
     * @see IWorkbenchActions
     *
     * @param actionID an action ID declared in the registry
     * @param handler an action which implements the action ID.  
     *		<code>null</code> may be passed to deregister a handler.
     */
    public void setGlobalActionHandler(String actionID, IAction handler) {
    }

	/**
     * Commits all UI changes.  This should be called
     * after additions or subtractions have been made to a 
     * menu, status line, or toolbar.
     */
    public void updateActionBars() {
        window.updateActionBars();
    }
}
