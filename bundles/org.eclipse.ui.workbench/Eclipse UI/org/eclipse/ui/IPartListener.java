package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Interface for listening to part lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPartService#addPartListener
 */
public interface IPartListener {
/**
 * Notifies this listener that the given part has been activated.
 *
 * @param part the part that was activated
 * @see IPerspective#activate
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
 * @see IPerspective#bringToTop
 */
public void partBroughtToTop(IWorkbenchPart part);
/**
 * Notifies this listener that the given part has been closed.
 *
 * @param part the part that was closed
 * @see IPerspective#close
 */
public void partClosed(IWorkbenchPart part);
/**
 * Notifies this listener that the given part has been deactivated.
 *
 * @param part the part that was deactivated
 * @see IPerspective#activate
 */
public void partDeactivated(IWorkbenchPart part);
/**
 * Notifies this listener that the given part has been opened.
 *
 * @param part the part that was opened
 */
public void partOpened(IWorkbenchPart part);
}
