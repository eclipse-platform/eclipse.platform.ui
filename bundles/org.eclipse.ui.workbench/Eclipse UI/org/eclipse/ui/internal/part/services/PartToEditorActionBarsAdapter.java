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
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.internal.components.Assert;
import org.eclipse.ui.internal.part.components.services.IPartActionBars;
import org.eclipse.ui.internal.part.components.services.IStatusFactory;
import org.eclipse.ui.internal.part.components.services.IStatusHandler;

public class PartToEditorActionBarsAdapter implements IActionBars2 {

    private IPartActionBars parent;
    private IStatusLineManager statusLine;
    
    public PartToEditorActionBarsAdapter(IPartActionBars toAdapt, IStatusHandler statusHandler, IStatusFactory statusFactory) {
        Assert.isNotNull(toAdapt);
        Assert.isNotNull(statusHandler);
        Assert.isNotNull(statusFactory);
        parent = toAdapt;
        this.statusLine = new StatusLineManagerAdapter(statusHandler, statusFactory);
    }
    
    public ICoolBarManager getCoolBarManager() {
        return parent.getCoolBarManager();
    }
    
    public void clearGlobalActionHandlers() {
        parent.clearGlobalActionHandlers();
    }

    public IAction getGlobalActionHandler(String actionId) {
        return parent.getGlobalActionHandler(actionId);
    }

    public IMenuManager getMenuManager() {
        return parent.getMenuBarManager();
    }

    public IStatusLineManager getStatusLineManager() {
        return statusLine;
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
