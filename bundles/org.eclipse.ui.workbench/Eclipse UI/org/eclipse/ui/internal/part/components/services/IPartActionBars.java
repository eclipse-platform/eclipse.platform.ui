/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.components.services;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * Not intended to be implemented by clients
 * 
 * @since 3.1
 */
public interface IPartActionBars {
    /**
     * Clears the global action handler list.
     * <p>
     * Note: Clients who manipulate the global action list are
     * responsible for calling <code>updateActionBars</code> so that the changes
     * can be propagated throughout the workbench.
     * </p>
     */
    public void clearGlobalActionHandlers();

    /**
     * Returns the global action handler for the action with the given id.  
     *
     * @param actionId an action id declared in the registry
     * @return an action handler which implements the action id, or
     *   <code>null</code> if none is registered
     * @see IWorkbenchActionConstants
     * @see #setGlobalActionHandler(String, IAction)
     */
    public IAction getGlobalActionHandler(String actionId);

    /**
     * Returns the menu manager for a single dropdown menu associated with the part.
     * Top-level elements in this menu may be individual actions.
     * <p>
     * Note: Clients who add or remove items from the returned menu manager are
     * responsible for calling <code>updateActionBars</code> so that the changes
     * can be propagated throughout the workbench.
     * </p>
     *
     * @return the menu manager
     */
    public IMenuManager getPartMenuManager();

    /**
     * Returns a menu manager that the part can use to contribute to a menu bar.
     * Top-level elements in this menu should be things that could be contributed
     * to the top-level menubar.
     *  
     * <p>
     * Note: Clients who add or remove items from the returned menu manager are
     * responsible for calling <code>updateActionBars</code> so that the changes
     * can be propagated throughout the workbench.
     * </p>
     * 
     * @return the menu manager
     */
    public IMenuManager getMenuBarManager();
    
    /**
     * Returns the tool bar manager.
     * <p>
     * Note: Clients who add or remove items from the returned tool bar manager are
     * responsible for calling <code>updateActionBars</code> so that the changes
     * can be propagated throughout the workbench.
     * </p>
     *
     * @return the tool bar manager
     */
    public IToolBarManager getToolBarManager();

    /**
     * Returns the cool bar manager.
     * <p>
     * Note: Clients who add or remove items from the returned cool bar manager are
     * responsible for calling <code>updateActionBars</code> so that the changes
     * can be propagated throughout the workbench.
     * </p>
     *
     * @return the cool bar manager.
     */
    public ICoolBarManager getCoolBarManager();
    
    /**
     * Sets the global action handler for the action with the given id.
     * <p>
     * Note: Clients who manipulate the global action list are
     * responsible for calling <code>updateActionBars</code> so that the changes
     * can be propagated throughout the workbench.
     * </p>
     *
     * @param actionId an action id declared in the registry
     * @param handler an action which implements the action id, or
     *  <code>null</code> to clear any existing handler
     * @see IWorkbenchActionConstants
     */
    public void setGlobalActionHandler(String actionId, IAction handler);

    /**
     * Updates the action bars.
     * <p>
     * Clients who add or remove items from the menu, tool bar, or status line
     * managers should call this method to propagated the changes throughout 
     * the workbench.
     * </p>
     */
    public void updateActionBars();
}
