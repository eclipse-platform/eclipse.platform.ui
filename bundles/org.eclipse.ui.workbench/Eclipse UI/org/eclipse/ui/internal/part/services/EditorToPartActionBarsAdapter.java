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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;

public class EditorToPartActionBarsAdapter implements IPartActionBars {

    private IActionBars parent;
    private IMenuManager partMenuManager = new NullMenuManager();
    private ICoolBarManager cbm;
    
    public EditorToPartActionBarsAdapter(IActionBars parent) {
        this.parent = parent;
        
        if (parent instanceof IActionBars2) {
            cbm = ((IActionBars2)parent).getCoolBarManager();
        } else {
            cbm = new NullCoolBarManager();
        }
    }
    
    public ICoolBarManager getCoolBarManager() {
        return cbm;
    }
    
    public void clearGlobalActionHandlers() {
        parent.clearGlobalActionHandlers();
    }

    public IAction getGlobalActionHandler(String actionId) {
        return parent.getGlobalActionHandler(actionId);
    }

    public IMenuManager getMenuBarManager() {
        return parent.getMenuManager();
    }

    public IMenuManager getPartMenuManager() {
        return partMenuManager;
    }

    public IToolBarManager getToolBarManager() {
        return parent.getToolBarManager();
    }

    public void setGlobalActionHandler(String actionId, IAction handler) {
        parent.setGlobalActionHandler(actionId, handler);
    }

    public void updateActionBars() {
        parent.updateActionBars();
    }
}
