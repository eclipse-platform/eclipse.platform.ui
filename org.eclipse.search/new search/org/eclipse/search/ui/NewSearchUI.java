/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui;
import org.eclipse.core.runtime.IStatus;

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
	 * Activates a search result view in the current perspective.
	 */
	public static ISearchResultViewPart activateSearchResultView() {
		return InternalSearchUI.getInstance().activateSearchView();
	}
	
	/**
	 * Gets the search result view shown in the current perspective.
	 */
	public static ISearchResultViewPart getSearchResultView() {
		return InternalSearchUI.getInstance().getSearchView();
	}
	/**
	 * Runs the given search query. This method may run the given query in a separate thread 
	 * if <code>ISearchQuery#canRunInBackground()</code> returns <code>true</code>.
	 * Running a query adds it to the set of known queries.
	 * 
	 * @param query The query to execute.
	 */
	public static void runQuery(ISearchQuery query) {
		if (query.canRunInBackground())
			InternalSearchUI.getInstance().runSearchInBackground(query);
		else
			InternalSearchUI.getInstance().runSearchInForeground(null, query);
	}
	
	/**
	 * Runs the given search query. This method will execute the query in the same thread 
	 * as the caller. This method blocks until the query is finished.
	 * Running a query adds it to the set of known queries.
	 * 
	 * @param context The runnable context to run the query in.
	 * @param query The query to execute.
	 */
	public static IStatus runQueryInForeground(IRunnableContext context, ISearchQuery query) {
		return InternalSearchUI.getInstance().runSearchInForeground(context, query);
	}
	
	/**
	 * Adds the given query listener. Does nothing when the listener is already
	 * present.
	 * 
	 * @param l The listener to be added.
	 */
	public static void addQueryListener(IQueryListener l) {
		InternalSearchUI.getInstance().addQueryListener(l);
	}
	/**
	 * Removes the given query listener. Does nothing if the listener is
	 * not present.
	 * 
	 * @param l The listener to be removed.
	 */
	public static void removeQueryListener(IQueryListener l) {
		InternalSearchUI.getInstance().removeQueryListener(l);
	}
	
	/**
	 * Returns all search results know to the search ui.
	 * 
	 * @return All search result.
	 */
	public static ISearchQuery[] getQueries() {
		return InternalSearchUI.getInstance().getQueries();
	}
}
