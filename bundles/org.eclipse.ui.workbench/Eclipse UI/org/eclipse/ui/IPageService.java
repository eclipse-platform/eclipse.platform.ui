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
package org.eclipse.ui;

/**
 * A page service tracks the page and perspective lifecycle events
 * within a workbench window.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IWorkbenchWindow
 * @see IPageListener
 * @see IPerspectiveListener
 */
public interface IPageService {
    /**
     * Adds the given listener for page lifecycle events.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener a page listener
     */
    public void addPageListener(IPageListener listener);

    /**
     * Adds the given listener for a page's perspective lifecycle events.
     * Has no effect if an identical listener is already registered.
     *
     * @param listener a perspective listener
     */
    public void addPerspectiveListener(IPerspectiveListener listener);

    /*
     * Returns the active page.
     *
     * @return the active page, or <code>null</code> if no page is currently active
     */
    public IWorkbenchPage getActivePage();

    /**
     * Removes the given page listener.
     * Has no affect if an identical listener is not registered.
     *
     * @param listener a page listener
     */
    public void removePageListener(IPageListener listener);

    /**
     * Removes the given page's perspective listener.
     * Has no affect if an identical listener is not registered.
     *
     * @param listener a perspective listener
     */
    public void removePerspectiveListener(IPerspectiveListener listener);
}
