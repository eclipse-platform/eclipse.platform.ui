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
package org.eclipse.ui.internal.part.services;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;

public class NullPartActionBars implements IPartActionBars {

    private IMenuManager menuManager = new NullMenuManager();
    private IToolBarManager toolbarManager = new NullToolBarManager();
    private ICoolBarManager nullCbm = new NullCoolBarManager();
    
    public ICoolBarManager getCoolBarManager() {
        return nullCbm;
    }
    
    public void clearGlobalActionHandlers() {

    }

    public IAction getGlobalActionHandler(String actionId) {
        return null;
    }

    public IMenuManager getPartMenuManager() {
        return menuManager;
    }

    public IMenuManager getMenuBarManager() {
        return menuManager;
    }

    public IToolBarManager getToolBarManager() {
        return toolbarManager;
    }

    public void setGlobalActionHandler(String actionId, IAction handler) {
    }

    public void updateActionBars() {
    }

}
