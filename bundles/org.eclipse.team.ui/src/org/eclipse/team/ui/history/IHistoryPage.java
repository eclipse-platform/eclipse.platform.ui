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

import org.eclipse.team.ui.TeamUI;


/**
 * Interface for pages that appear in the team history view.
 * <p>
 * This interface is not intended to be implemented by clients.
 * Clients can instead subclass {@link HistoryPage}.
 * 
 * @see TeamUI#getHistoryView()
 * @since 3.2
 */
public interface IHistoryPage {
	
	/**
	 * Fetches and populates the history page for the given Object. Clients
	 * should provide an implementation for their individual pages.
	 * 
	 * @param object the object for which history is being requested for
	 * @param refetch whether the history should be refecthed if the page is already showing the 
	 * history for the given object
	 * @return true if the page was able to display the history for the object, false otherwise
	 */
	public boolean setInput(Object object, boolean refetch);

	/**
	 * Returns the object whose history is currently being displayed in the history page. 
	 * @return object	the object being displayed in the history page or <code>null</code> 
	 * if no input has been set;
	 */
	public Object getInput();
	
	/**
	 * Returns true if this history page can show a history for the given object, false if it cannot
	 * @param object the object that is to have history shown
	 * @return boolean 
	 */
	public boolean isValidInput(Object object);

	/**
	 * Requests a refresh of the information presented by the history page.
	 */
	public void refresh();

	/**
	 * Returns the name of the object whose history the page is showing
	 * @return String containing the name of the object 
	 */
	public String getName();
	
	/**
	 * Returns a one line description of the object whose history is
	 * being displayed. For example, for files, this may be the 
	 * workspace path of the file. The discription may be displayed to
	 * the user as tooltip text or by some other means.
	 * @return a one line description of the object whose history is
	 * being displayed or <code>null</code>
	 */
	public String getDescription();

	/**
	 * Set the site for the page - this needs to be replaced with a proper
	 * {@link IHistoryPageSite} in order to allow history pages to be displayed in 
	 * both views and dialogs.
	 * @param site the history page site
	 */
	public void setSite(IHistoryPageSite site);
	
	/**
	 * Returns the {@link IHistoryPageSite} set for this page.
	 * @return the history page site for this page
	 */
	public IHistoryPageSite getHistoryPageSite();
}
