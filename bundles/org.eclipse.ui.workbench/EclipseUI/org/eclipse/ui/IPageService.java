/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 * A page service tracks the page and perspective lifecycle events within a
 * workbench window.
 * <p>
 * This service can be acquired from your service locator:
 * </p>
 *
 * <pre>
 * IPageService service = (IPageService) getSite().getService(IPageService.class);
 * </pre>
 * <ul>
 * <li>This service is not available globally, only from the workbench window
 * level down.</li>
 * </ul>
 *
 * @see IWorkbenchWindow
 * @see IPageListener
 * @see IPerspectiveListener
 * @see org.eclipse.ui.services.IServiceLocator#getService(Class)
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPageService {
	/**
	 * Adds the given listener for page lifecycle events. Has no effect if an
	 * identical listener is already registered.
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If not,
	 * they will be removed when the IServiceLocator used to acquire this service is
	 * disposed.
	 * </p>
	 *
	 * @param listener a page listener
	 * @see #removePageListener(IPageListener)
	 */
	void addPageListener(IPageListener listener);

	/**
	 * Adds the given listener for a page's perspective lifecycle events. Has no
	 * effect if an identical listener is already registered.
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If not,
	 * they will be removed when the IServiceLocator used to acquire this service is
	 * disposed.
	 * </p>
	 *
	 * @param listener a perspective listener
	 * @see #removePerspectiveListener(IPerspectiveListener)
	 */
	void addPerspectiveListener(IPerspectiveListener listener);

	/**
	 * Returns the active page.
	 *
	 * @return the active page, or <code>null</code> if no page is currently active
	 */
	IWorkbenchPage getActivePage();

	/**
	 * Removes the given page listener. Has no effect if an identical listener is
	 * not registered.
	 *
	 * @param listener a page listener
	 */
	void removePageListener(IPageListener listener);

	/**
	 * Removes the given page's perspective listener. Has no effect if an identical
	 * listener is not registered.
	 *
	 * @param listener a perspective listener
	 */
	void removePerspectiveListener(IPerspectiveListener listener);
}
