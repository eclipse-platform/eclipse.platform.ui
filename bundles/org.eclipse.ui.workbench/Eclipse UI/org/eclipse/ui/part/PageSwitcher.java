/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 ******************************************************************************/

package org.eclipse.ui.part;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.handlers.CyclePageHandler;

/**
 * Provides the implementation for switching pages in a view. A view may track
 * pages however it wishes. The view should subclass PageSwitcher to provide the
 * abstract methods, and then instantiate their page switcher once.
 *
 * @since 3.4
 */
public abstract class PageSwitcher {

	/**
	 * Register the handlers for page switching with this view or editor.
	 *
	 * @param part The part to register against.
	 */
	public PageSwitcher(IWorkbenchPart part) {
		IHandlerService service = part.getSite().getService(IHandlerService.class);
		service.activateHandler(IWorkbenchCommandConstants.NAVIGATE_NEXT_PAGE, new CyclePageHandler(this));
		service.activateHandler(IWorkbenchCommandConstants.NAVIGATE_PREVIOUS_PAGE, new CyclePageHandler(this));
	}

	/**
	 * Displays the given page in the view. The page must already exist in the view.
	 *
	 * @param page the page to display, never <code>null</code>.
	 */
	public abstract void activatePage(Object page);

	/**
	 * Returns an {@link ImageDescriptor} for the page.
	 *
	 * @param page the page to retrieve an {@link ImageDescriptor}
	 * @return An {@link ImageDescriptor} for the page, may be <code>null</code>.
	 */
	public abstract ImageDescriptor getImageDescriptor(Object page);

	/**
	 * Returns a readable name to identify the page.
	 *
	 * @param page the page to get the name
	 * @return the name of the page
	 */
	public abstract String getName(Object page);

	/**
	 * Returns the pages available in the view. These may be used for populating the
	 * pop-up dialog when switching pages. These are the objects that will be used
	 * in {@link #activatePage(Object)}.
	 *
	 * @return an array of pages
	 */
	public abstract Object[] getPages();

	/**
	 * Returns the index of the currently active page. The default implementation
	 * returns 0. Subclasses can override.
	 *
	 * @return the 0-based index of the currently active page from
	 *         {@link #getPages()}, or an arbitrary value if {@link #getPages()} is
	 *         an empty array.
	 */
	public int getCurrentPageIndex() {
		return 0;
	}

}
