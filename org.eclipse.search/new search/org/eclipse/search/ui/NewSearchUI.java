/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.ui;

import org.eclipse.jface.operation.IRunnableContext;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search.ui.text.ITextSearchResult;

/**
 * A facade for the new search ui.
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 *
 */
public class NewSearchUI {

	/**
	 * @return Returns the singleton search manager.
	 */
	public static ISearchResultManager getSearchManager() {
		return InternalSearchUI.getInstance().getSearchManager();
	}

	/**
	 * Cancels the search job associated with the given 
	 * search result.
	 * @param search
	 */
	public static void cancelSearch(ISearchResult search) {
		InternalSearchUI.getInstance().cancelSearch(search);
	}

	/**
	 * Activates the search result view in the current
	 * perspective.
	 */
	public static void activateSearchResultView() {
		InternalSearchUI.getInstance().activateSearchView();
	}

	/**
	 * Runs the given search job and associates it with 
	 * the given search result. 
	 * @param search The search result this search job acts upon.
	 * @param wsJob  The job to execute.
	 * @param b      Whether the search should run in the background.
	 */
	public static void runSearchInBackground(ITextSearchResult search, ISearchJob job) {
		InternalSearchUI.getInstance().runSearchInBackground(search, job);
	}
	
	/**
	 * Runs the given search job and associates it with 
	 * the given search result. 
	 * @param search The search result this search job acts upon.
	 * @param wsJob  The job to execute.
	 * @param b      Whether the search should run in the background.
	 */
	public static void runSearchInForeground(IRunnableContext monitor, ITextSearchResult search, ISearchJob job) {
		InternalSearchUI.getInstance().runSearchInForeground(monitor, search, job);
	}
	

}
