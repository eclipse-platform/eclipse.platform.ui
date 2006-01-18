/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.ui.history;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IViewSite;

/**
 * TODO: provide a comment and an abstract class
 * 
 * @since 3.2
 */
public interface IHistoryPage {
	/**
	 * Fetches and populates the history page for the given IResource. Clients
	 * should provide an implementation for their individual pages.
	 * 
	 * @param resource	the resource for which history is being requested for
	 * @return true if the page was able to display the history for the resource, false otherwise
	 */
	public boolean showHistory(IResource resource, boolean refetch);

	/**
	 * Returns true if this history page can show a history for the given resource, false if it cannot
	 * @param resource the resource that is to have history shown
	 * @return boolean 
	 */
	public boolean canShowHistoryFor(IResource resource);

	/**
	 * Requests a refresh of the information presented by the history page.
	 *
	 */
	public void refresh();

	/**
	 * Returns the name of the object whose history the page is showing
	 * @return String containing the name of the object 
	 */
	public String getName();

	/**
	 * Set the site for the page - this needs to be replaced with a proper
	 * IHistoryPageSite in order to allow history pages to be displayed in 
	 * both views and dialogs
	 * @param viewSite
	 * 
	 * TODO: Create an IHistoryPageSite
	 */
	public void setSite(IViewSite viewSite);
}
