/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.search2.internal.ui.InternalSearchUI;
/**
 * A facade for the new search ui. This API is preliminary and subject to
 * change at any time.
 * 
 * @since 3.0
 */
public class NewSearchUI {
	/**
	 * @return Returns the singleton search manager.
	 */
	public static ISearchResultManager getSearchManager() {
		return InternalSearchUI.getInstance().getSearchManager();
	}
	/**
	 * Activates the search result view in the current perspective.
	 */
	public static void activateSearchResultView() {
		InternalSearchUI.getInstance().activateSearchView();
	}
	/**
	 * Runs the given search job and associates it with the given search
	 * result. This method runs the query in the background.
	 * 
	 * @param query The query to execute.
	 * @param search The search result the given query should fill.
	 */
	public static void runSearchInBackground(ISearchQuery query, ISearchResult result) {
		InternalSearchUI.getInstance().runSearchInBackground(query, result);
	}
	/**
	 * Runs the given search job and associates it with the given search
	 * result. This method will execute the query in the same thread as the
	 * caller. This method blocks until the query is finished.
	 * 
	 * @param context The runnable context to run the query in.
	 * @param query The query to execute.
	 * @param search The search result the given query should fill.
	 */
	public static void runSearchInForeground(IRunnableContext context, ISearchQuery job, ISearchResult result) {
		InternalSearchUI.getInstance().runSearchInForeground(context, job,
				result);
	}
}
