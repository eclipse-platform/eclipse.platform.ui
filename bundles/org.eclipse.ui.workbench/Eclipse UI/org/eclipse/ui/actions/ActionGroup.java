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
package org.eclipse.ui.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;

/**
 * An <code>ActionGroup</code> represents a group of actions
 * which are added to a context menu, or the action bars of a part, together.
 * The group is given a context which can be used to determine which actions
 * are added, and what their enabled state should be.
 * <p>
 * This class is intended only as a convenience for managing groups of actions.
 * Clients are not required to use this class in order to add actions to context
 * menus or action bars.
 * </p>
 * <p>
 * Clients should subclass this class and extend or override the appropriate fill methods.
 * </p>
 * 
 * @since 2.0
 */
public abstract class ActionGroup {

    /**
     * The action context, used to determine which actions are added,
     * and what their enabled state should be.
     */
    private ActionContext context;

    /** 
     * Returns the context used to determine which actions are added,
     * and what their enabled state should be.
     */
    public ActionContext getContext() {
        return context;
    }

    /** 
     * Sets the context used to determine which actions are added,
     * and what their enabled state should be.
     * 
     * @param context the context to use
     */
    public void setContext(ActionContext context) {
        this.context = context;
    }

    /** 
     * Adds the applicable actions to a context menu,
     * based on the state of the <code>ActionContext</code>.
     * <p>
     * The default implementation does nothing.  
     * Subclasses may override or extend this method.
     * </p>
     * 
     * @param menu the context menu manager
     */
    public void fillContextMenu(IMenuManager menu) {
        // do nothing
    }

    /** 
     * Adds the applicable actions to a part's action bars,
     * including setting any global action handlers.
     * <p>
     * The default implementation does nothing.
     * Subclasses may override or extend this method.
     * </p>
     * 
     * @param actionBars the part's action bars
     */
    public void fillActionBars(IActionBars actionBars) {
        // do nothing
    }

    /**
     * Updates the state of the actions added to the action bars,
     * including any global action handlers,
     * based on the state of the <code>ActionContext</code>.
     * <p>
     * The default implementation does nothing.
     * Subclasses may override or extend this method.
     * </p>
     */
    public void updateActionBars() {
        // do nothing
    }

    /** 
     * This method is called by the user of an action group to signal that the group is
     * no longer needed. Subclasses typically implement this method to deregister
     * any listeners or to free other resources.
     * <p>
     * The default implementation calls <code>setContext(null)</code>.
     * Subclasses may extend this method.
     * </p>
     */
    public void dispose() {
        setContext(null);
    }
}
