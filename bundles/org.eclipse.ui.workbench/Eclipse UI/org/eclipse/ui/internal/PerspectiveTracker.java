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
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;

/**
 * Utility class for tracking the active perspective in a window.
 *
 * @since 3.1
 */
public class PerspectiveTracker extends PerspectiveAdapter implements
        IPageListener {

    private IWorkbenchWindow window;

    private IAction action;

    /**
     * Creates a perspective tracker for the given window.
     * Subclasses should override <code>update(IPerspectiveDescriptor)</code>
     * to get notified of perspective changes.
     * 
     * @param window the window to track
     */
    protected PerspectiveTracker(IWorkbenchWindow window) {
        Assert.isNotNull(window);
        this.window = window;
        window.addPageListener(this);
        window.addPerspectiveListener(this);
    }

    /**
     * Creates a perspective tracker for the given window which will 
     * enable the given action only when there is an active perspective.
     * 
     * @param window the window to track
     * @param action the action to enable or disable
     */
    public PerspectiveTracker(IWorkbenchWindow window, IAction action) {
        this(window);
        this.action = action;
        update();
    }

    /**
     * Disposes the tracker.
     */
    public void dispose() {
        if (window != null) {
            window.removePageListener(this);
            window.removePerspectiveListener(this);
        }
    }

    public void pageActivated(IWorkbenchPage page) {
        update();
    }

    public void pageClosed(IWorkbenchPage page) {
        update();
    }

    public void pageOpened(IWorkbenchPage page) {
        // ignore
    }

    public void perspectiveActivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {
        update();
    }

    /**
     * Determines the active perspective in the window 
     * and calls <code>update(IPerspectiveDescriptor)</code>.
     */
    private void update() {
        if (window != null) {
            IPerspectiveDescriptor persp = null;
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                persp = page.getPerspective();
            }
            update(persp);
        }
    }

    /**
     * Performs some function based on the active perspective in the window.
     * <p>
     * The default implementation enables the action (if given) if there
     * is an active perspective, otherwise it disables it.
     * </p>
     * <p> 
     * Subclasses may override or extend.
     * </p>
     * 
     * @param persp the active perspective in the window, or <code>null</code> if none
     */
    protected void update(IPerspectiveDescriptor persp) {
        if (action != null) {
            action.setEnabled(persp != null);
        }
    }

}
