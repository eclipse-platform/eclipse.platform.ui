/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 * Interface for listening to page lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPageService#addPageListener
 */
public interface IPageListener {
	/**
	 * Notifies this listener that the given page has been activated.
	 *
	 * @param page the page that was activated
	 * @see IWorkbenchWindow#setActivePage
	 */
	void pageActivated(IWorkbenchPage page);

	/**
	 * Notifies this listener that the given page has been closed.
	 *
	 * @param page the page that was closed
	 * @see IWorkbenchPage#close
	 */
	void pageClosed(IWorkbenchPage page);

	/**
	 * Notifies this listener that the given page has been opened.
	 *
	 * @param page the page that was opened
	 * @see IWorkbenchWindow#openPage
	 */
	void pageOpened(IWorkbenchPage page);
}
