/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.services;

import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.components.ComponentException;
import org.eclipse.ui.components.IDisposable;
import org.eclipse.ui.components.IServiceProvider;
import org.eclipse.ui.internal.part.multiplexer.INestedComponent;
import org.eclipse.ui.internal.part.multiplexer.ISharedContext;

/**
 * @since 3.1
 */
public class ChildActionBars implements INestedComponent, IActionBars, IDisposable {

    private SubActionBars implementation;

    public ChildActionBars(IServiceProvider childContainer, ISharedContext shared) throws ComponentException {
        IServiceProvider sharedContainer = shared.getSharedComponents();
        implementation = new SubActionBars((IActionBars)sharedContainer.getService(IActionBars.class));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.interfaces.INestedComponent#activate()
     */
    public void activate() {
        implementation.activate(true);
        implementation.updateActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.interfaces.INestedComponent#deactivate()
     */
    public void deactivate() {
        implementation.deactivate(true);
    }
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        implementation.addPropertyChangeListener(listener);
    }
    public void clearGlobalActionHandlers() {
        implementation.clearGlobalActionHandlers();
    }
    public IAction getGlobalActionHandler(String actionId) {
        return implementation.getGlobalActionHandler(actionId);
    }
    public Map getGlobalActionHandlers() {
        return implementation.getGlobalActionHandlers();
    }
    public IMenuManager getMenuManager() {
        return implementation.getMenuManager();
    }
    public IStatusLineManager getStatusLineManager() {
        return implementation.getStatusLineManager();
    }
    public IToolBarManager getToolBarManager() {
        return implementation.getToolBarManager();
    }
    public void partChanged(IWorkbenchPart part) {
        implementation.partChanged(part);
    }
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        implementation.removePropertyChangeListener(listener);
    }
    public void setGlobalActionHandler(String actionId, IAction handler) {
        implementation.setGlobalActionHandler(actionId, handler);
    }
    public void updateActionBars() {
        implementation.updateActionBars();
    }
    public void dispose() {
        implementation.dispose();
    }
}
