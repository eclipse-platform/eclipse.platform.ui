/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	 * <p>
	 * <b>Note:</b> This event is not fired if no perspective is open (the window is
	 * empty).
	 * </p>
	 *
	 * @param window the window that was activated
	 */
	void windowActivated(IWorkbenchWindow window);

	/**
	 * Notifies this listener that the given window has been deactivated.
	 * <p>
	 * <b>Note:</b> This event is not fired if no perspective is open (the window is
	 * empty).
	 * </p>
	 *
	 * @param window the window that was activated
	 */
	void windowDeactivated(IWorkbenchWindow window);

	/**
	 * Notifies this listener that the given window has been closed.
	 *
	 * @param window the window that was closed
	 * @see IWorkbenchWindow#close
	 */
	void windowClosed(IWorkbenchWindow window);

	/**
	 * Notifies this listener that the given window has been opened.
	 *
	 * @param window the window that was opened
	 * @see IWorkbench#openWorkbenchWindow
	 */
	void windowOpened(IWorkbenchWindow window);

}
