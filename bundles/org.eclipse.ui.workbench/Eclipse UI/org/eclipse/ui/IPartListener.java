/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
 * Interface for listening to part lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPartService#addPartListener(IPartListener)
 */
public interface IPartListener {

    /**
     * Notifies this listener that the given part has been activated.
     *
     * @param part the part that was activated
     * @see IWorkbenchPage#activate
     */
    public void partActivated(IWorkbenchPart part);

    /**
     * Notifies this listener that the given part has been brought to the top.
     * <p>
     * These events occur when an editor is brought to the top in the editor area,
     * or when a view is brought to the top in a page book with multiple views.
     * They are normally only sent when a part is brought to the top 
     * programmatically (via <code>IPerspective.bringToTop</code>). When a part is
     * activated by the user clicking on it, only <code>partActivated</code> is sent.
     * </p>
     *
     * @param part the part that was surfaced
     * @see IWorkbenchPage#bringToTop
     */
    public void partBroughtToTop(IWorkbenchPart part);

    /**
     * Notifies this listener that the given part has been closed.
     *
     * @param part the part that was closed
     * @see IWorkbenchPage#hideView(IViewPart)
     */
    public void partClosed(IWorkbenchPart part);

    /**
     * Notifies this listener that the given part has been deactivated.
     *
     * @param part the part that was deactivated
     * @see IWorkbenchPage#activate(IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part);

    /**
     * Notifies this listener that the given part has been opened.
     *
     * @param part the part that was opened
     * @see IWorkbenchPage#showView(String)
     */
    public void partOpened(IWorkbenchPart part);
}
