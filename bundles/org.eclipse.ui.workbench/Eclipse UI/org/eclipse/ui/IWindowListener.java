package org.eclipse.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Interface for listening to window lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IWindowListener {
/**
 * Notifies this listener that the given window has been activated.
 *
 * @param window the window that was activated
 */
public void windowActivated(IWorkbenchWindow window);
/**
 * Notifies this listener that the given window has been deactivated.
 *
 * @param window the window that was activated
 */
public void windowDeactivated(IWorkbenchWindow window);
/**
 * Notifies this listener that the given window has been closed.
 *
 * @param window the window that was closed
 * @see IWorkbenchWindow#close
 */
public void windowClosed(IWorkbenchWindow window);
/**
 * Notifies this listener that the given window has been opened.
 *
 * @param window the window that was opened
 * @see IWorkbench#openWorkbenchWindow
 */
public void windowOpened(IWorkbenchWindow window);

}
