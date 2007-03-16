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

import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

/**
 * The abstract superclass for actions that listen to page activation and
 * open/close events. This implementation tracks the active page (see 
 * <code>getActivePage</code>) and provides a convenient place to monitor
 * page lifecycle events that could affect the availability of the action.
 * <p>
 * Subclasses must implement the following <code>IAction</code> method:
 * <ul>
 *   <li><code>run</code> - to do the action's work</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend any of the <code>IPartListener</code> methods if the
 * action availablity needs to be recalculated:
 * <ul>
 *   <li><code>partActivated</code></li> 
 *   <li><code>partDeactivated</code></li>
 *   <li><code>partOpened</code></li>
 *   <li><code>partClosed</code></li>
 *   <li><code>partBroughtToTop</code></li>
 * </ul>
 * </p>
 * <p>
 * Subclasses may extend any of the <code>IPageListener</code> methods if the
 * action availablity needs to be recalculated:
 * <ul>
 *   <li><code>pageActivated</code></li> 
 *   <li><code>pageClosed</code></li>
 *   <li><code>pageOpened</code></li>
 * </ul>
 * </p>
 * <p>
 * This method implements the <code>IPartListener</code> and
 * <code>IPageListener</code>interfaces, and automatically registers listeners
 * so that it can keep its enablement state up to date. Ordinarily, the
 * window's references to these listeners will be dropped automatically when
 * the window closes. However, if the client needs to get rid of an action
 * while the window is still open, the client must call 
 * {@link IWorkbenchAction#dispose dispose} to give the action an
 * opportunity to deregister its listeners and to perform any other cleanup.
 * </p>
 */
public abstract class PageEventAction extends PartEventAction implements
        IPageListener, ActionFactory.IWorkbenchAction {
    /**
     * The active page, or <code>null</code> if none.
     */
    private IWorkbenchPage activePage;

    /**
     * The workbench window this action is registered with.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Creates a new action with the given text. Register this
     * action with the workbench window for page lifecycle
     * events.
     *
     * @param text the string used as the text for the action, 
     *   or <code>null</code> if there is no text
     * @param window the workbench window this action is
     *   registered with
     */
    protected PageEventAction(String text, IWorkbenchWindow window) {
        super(text);
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        this.activePage = window.getActivePage();
        this.workbenchWindow.addPageListener(this);
        this.workbenchWindow.getPartService().addPartListener(this);
    }

    /**
     * Returns the currently active page in the workbench window.
     *
     * @return currently active page in the workbench window,
     * or <code>null</code> in none
     */
    public final IWorkbenchPage getActivePage() {
        return activePage;
    }

    /**
     * Returns the workbench window this action applies to.
     *
     * @return the workbench window, or <code>null</code> if this action has been
     * disposed
     */
    public final IWorkbenchWindow getWorkbenchWindow() {
        return workbenchWindow;
    }

    /**
     * The <code>PageEventAction</code> implementation of this 
     * <code>IPageListener</code> method records that the given page is active.
     * Subclasses may extend this method if action availability has to be
     * recalculated.
     */
    public void pageActivated(IWorkbenchPage page) {
        this.activePage = page;
    }

    /**
     * The <code>PageEventAction</code> implementation of this 
     * <code>IPageListener</code> method clears the active page if it just closed.
     * Subclasses may extend this method if action availability has to be
     * recalculated.
     */
    public void pageClosed(IWorkbenchPage page) {
        if (page == activePage) {
            activePage = null;
        }
    }

    /**
     * The <code>PageEventAction</code> implementation of this 
     * <code>IPageListener</code> method does nothing. Subclasses should extend
     * this method if action availability has to be recalculated.
     */
    public void pageOpened(IWorkbenchPage page) {
        // do nothing
    }

    /**
     * The <code>PageEventAction</code> implementation of this 
     * <code>ActionFactory.IWorkbenchAction</code> method
     * deregisters the part and page listener adding by the constructor.
     * Subclasses should extend this method to do additional
     * cleanup.
     * 
     * @since 3.0
     */
    public void dispose() {
        if (workbenchWindow == null) {
            // action has already been disposed
            return;
        }
        workbenchWindow.removePageListener(this);
        workbenchWindow.getPartService().removePartListener(this);
        workbenchWindow = null;
    }
}
