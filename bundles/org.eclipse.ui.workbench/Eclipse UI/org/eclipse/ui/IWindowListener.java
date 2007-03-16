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
