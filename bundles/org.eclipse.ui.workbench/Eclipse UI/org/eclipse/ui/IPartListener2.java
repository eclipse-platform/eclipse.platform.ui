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
 * Interface for listening to part lifecycle events.
 * <p>
 * This is a replacement for <code>IPartListener</code>.
 * <p> 
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPartService#addPartListener(IPartListener2)
 */
public interface IPartListener2 {

    /**
     * Notifies this listener that the given part has been activated.
     *
     * @param partRef the part that was activated
     * @see IWorkbenchPage#activate
     */
    public void partActivated(IWorkbenchPartReference partRef);

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
     * @param partRef the part that was surfaced
     * @see IWorkbenchPage#bringToTop
     */
    public void partBroughtToTop(IWorkbenchPartReference partRef);

    /**
     * Notifies this listener that the given part has been closed.
     * <p>
     * Note that if other perspectives in the same page share the view,
     * this notification is not sent.  It is only sent when the view
     * is being removed from the page entirely (it is being disposed).
     * </p>
     *
     * @param partRef the part that was closed
     * @see IWorkbenchPage#hideView
     */
    public void partClosed(IWorkbenchPartReference partRef);

    /**
     * Notifies this listener that the given part has been deactivated.
     *
     * @param partRef the part that was deactivated
     * @see IWorkbenchPage#activate
     */
    public void partDeactivated(IWorkbenchPartReference partRef);

    /**
     * Notifies this listener that the given part has been opened.
     * <p>
     * Note that if other perspectives in the same page share the view,
     * this notification is not sent.  It is only sent when the view
     * is being newly opened in the page (it is being created).
     * </p>
     *
     * @param partRef the part that was opened
     * @see IWorkbenchPage#showView
     */
    public void partOpened(IWorkbenchPartReference partRef);

    /**
     * Notifies this listener that the given part is hidden or obscured by another part.
     *
     * @param partRef the part that is hidden or obscured by another part
     */
    public void partHidden(IWorkbenchPartReference partRef);

    /**
     * Notifies this listener that the given part is visible.
     *
     * @param partRef the part that is visible
     */
    public void partVisible(IWorkbenchPartReference partRef);

    /**
     * Notifies this listener that the given part's input was changed.
     *
     * @param partRef the part whose input was changed
     */
    public void partInputChanged(IWorkbenchPartReference partRef);
}
