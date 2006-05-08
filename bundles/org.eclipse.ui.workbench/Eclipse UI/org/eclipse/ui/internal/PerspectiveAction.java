/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Abstract superclass of actions which are enabled iff there is an active perspective 
 * in the window.
 * 
 * @since 3.1
 */
public abstract class PerspectiveAction extends Action implements ActionFactory.IWorkbenchAction {
    
    /**
     * The workbench window containing this action.
     */
    private IWorkbenchWindow workbenchWindow;
    
    /**
     * Tracks perspective activation, to update this action's
     * enabled state.
     */
    private PerspectiveTracker tracker;

    /**
     * Constructs a new perspective action for the given window.
     * 
     * @param window the window
     */
    protected PerspectiveAction(IWorkbenchWindow window) {
        Assert.isNotNull(window);
        this.workbenchWindow = window;
        tracker = new PerspectiveTracker(window, this);
    }
    
    /**
     * Returns the window, or <code>null</code> if the action has been disposed.
     *  
     * @return the window or <code>null</code>
     */
    protected IWorkbenchWindow getWindow() {
        return workbenchWindow;
    }
    
    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        IWorkbenchPage page = workbenchWindow.getActivePage();
        if (page != null && page.getPerspective() != null) {
            run(page, page.getPerspective());
        }
    }
    
    /**
     * Runs the action, passing the active page and perspective.
     * 
     * @param page the active page
     * @param persp the active perspective
     */
    protected abstract void run(IWorkbenchPage page, IPerspectiveDescriptor persp);

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // already disposed
            return;
        }
        tracker.dispose();
        workbenchWindow = null;
    }

}

