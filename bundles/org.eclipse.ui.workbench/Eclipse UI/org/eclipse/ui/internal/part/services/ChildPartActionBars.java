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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.SubCoolBarManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.ui.internal.components.framework.ComponentException;
import org.eclipse.ui.internal.components.framework.IDisposable;
import org.eclipse.ui.internal.components.framework.IServiceProvider;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;
import org.eclipse.ui.internal.part.multiplexer.INestedComponent;
import org.eclipse.ui.internal.part.multiplexer.ISharedContext;

public class ChildPartActionBars implements IPartActionBars, INestedComponent, IDisposable {

    private IPartActionBars parent;
    
    private Map actionHandlers;

    private boolean active = false;

    private IMenuManager partMenuMgr;

    private IMenuManager menuBarMgr;
    
    private SubToolBarManager toolBarMgr;
    
    private SubCoolBarManager subCbm;
    
    public ChildPartActionBars(IServiceProvider childContainer, ISharedContext shared) throws ComponentException {
        IServiceProvider sharedContainer = shared.getSharedComponents();
        parent = (IPartActionBars)sharedContainer.getService(IPartActionBars.class);
    }
    
    public void clearGlobalActionHandlers() {
        if (actionHandlers != null) {
            if (active) {
                unregisterAllHandlers();
            }
            
            actionHandlers.clear();
        }
    }

    private void unregisterAllHandlers() {
        Collection keySet = actionHandlers.keySet();
        for (Iterator iter = keySet.iterator(); iter.hasNext();) {
            String handler = (String) iter.next();
            
            parent.setGlobalActionHandler(handler, null);
        }
    }

    private void registerAllHandlers() {
        Collection keySet = actionHandlers.keySet();
        for (Iterator iter = keySet.iterator(); iter.hasNext();) {
            String handler = (String) iter.next();
            
            parent.setGlobalActionHandler(handler, (IAction) actionHandlers.get(handler));
        }
    }

    
    public IAction getGlobalActionHandler(String actionId) {
        if (actionHandlers == null)
            return null;
        return (IAction) actionHandlers.get(actionId);
    }

    /**
     * Returns a new sub menu manager.
     *
     * @param parent the parent menu manager
     * @return the menu manager
     */
    protected SubMenuManager createSubMenuManager(IMenuManager parent) {
        return new SubMenuManager(parent);
    }
    
    public IMenuManager getPartMenuManager() {
        if (partMenuMgr == null) {
            partMenuMgr = createSubMenuManager(parent.getPartMenuManager());
            partMenuMgr.setVisible(active);
        }
        return partMenuMgr;
    }

    public IMenuManager getMenuBarManager() {
        if (menuBarMgr == null) {
            menuBarMgr = createSubMenuManager(parent.getPartMenuManager());
            menuBarMgr.setVisible(active);
        }
        return menuBarMgr;
    }

    /**
     * Returns a new sub toolbar manager.
     *
     * @param parent the parent toolbar manager
     * @return the tool bar manager
     */
    protected SubToolBarManager createSubToolBarManager(IToolBarManager parent) {
        return new SubToolBarManager(parent);
    }
    
    public IToolBarManager getToolBarManager() {
        if (toolBarMgr == null) {
            toolBarMgr = createSubToolBarManager(parent.getToolBarManager());
            toolBarMgr.setVisible(active);
        }
        return toolBarMgr;
    }

    public ICoolBarManager getCoolBarManager() {
        if (subCbm == null) {
            subCbm = new SubCoolBarManager(parent.getCoolBarManager());
            subCbm.setVisible(active);
        }
        
        return subCbm;        
    }
    
    public void setGlobalActionHandler(String actionID, IAction handler) {
        if (handler != null) {
            if (actionHandlers == null)
                actionHandlers = new HashMap(11);
            actionHandlers.put(actionID, handler);
        } else {
            if (actionHandlers != null)
                actionHandlers.remove(actionID);
        }
        if (active) {
            parent.setGlobalActionHandler(actionID, handler);
        }
    }

    public void updateActionBars() {
        if (active) {
            parent.updateActionBars();
        }
    }
    
    public void activate(Part newActivePart) {
        setActive(true);
    }

    public void deactivate(Object newActive) {
        setActive(false);
    }
    
    private void setActive(boolean isActive) {
        if (isActive == active) {
            return;
        }
        
        active = isActive;
        
        if (partMenuMgr != null)
            partMenuMgr.setVisible(isActive);

        if (menuBarMgr != null)
            menuBarMgr.setVisible(isActive);
        
        if (toolBarMgr != null)
            toolBarMgr.setVisible(isActive);
        
        if (actionHandlers != null) {
            if (active) {
                registerAllHandlers();
            } else {
                unregisterAllHandlers();
            }
            parent.updateActionBars();
        }
        
        if (subCbm != null) {
            subCbm.setVisible(isActive);
        }
        
    }
    
    public void dispose() {
        if (actionHandlers != null)
            actionHandlers.clear();
    }    
}

