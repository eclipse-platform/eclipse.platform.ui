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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.SearchMessages;
/**
 * A facade for access to the new search UI facilities.
 * 
 * @since 3.0
 */
public class NewSearchUI {
	/**
	 * Activates a search result view in the current workbench window page. If a
	 * search view is already open in the current workbench window page, it is
	 * acivated. Otherwise a new search view is opened and activated.
	 * 
	 * @return the activate search result view or <code>null</code> if the
	 *         search result view couldn't be activated
	 */
	public static ISearchResultViewPart activateSearchResultView() {
		return InternalSearchUI.getInstance().activateSearchView();
	}
	/**
	 * Gets the search result view shown in the current workbench window.
	 * 
	 * @return the search result view or <code>null</code>, if none is open
	 *         in the current workbench window page
	 */
	public static ISearchResultViewPart getSearchResultView() {
		return InternalSearchUI.getInstance().getSearchView();
	}
	/**
	 * Runs the given search query. This method may run the given query in a
	 * separate thread if <code>ISearchQuery#canRunInBackground()</code>
	 * returns <code>true</code>. Running a query adds it to the set of known
	 * queries and notifies any registered <code>IQueryListener</code>s about
	 * the addition.
	 * 
	 * @param query
	 *            the query to execute
	 */
	public static void runQuery(ISearchQuery query) {
		if (query.canRunInBackground())
			InternalSearchUI.getInstance().runSearchInBackground(query);
		else {
			IStatus status=InternalSearchUI.getInstance().runSearchInForeground(null, query);
			if (status != null) {
				if (!status.isOK())
					SearchPlugin.log(status);
				if (status.getSeverity() == IStatus.ERROR) {
					ErrorDialog.openError(SearchPlugin.getActiveWorkbenchShell(), SearchMessages.getString("NewSearchUI.error.title"), SearchMessages.getString("NewSearchUI.error.label"), status); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}
	/**
	 * Runs the given search query. This method will execute the query in the
	 * same thread as the caller. This method blocks until the query is
	 * finished. Running a query adds it to the set of known queries and notifies
	 * any registered <code>IQueryListener</code>s about the addition.
	 * 
	 * @param context
	 *            the runnable context to run the query in
	 * @param query
	 *            the query to execute
	 */
	public static IStatus runQueryInForeground(IRunnableContext context, ISearchQuery query) {
		return InternalSearchUI.getInstance().runSearchInForeground(context, query);
	}
	/**
	 * Registers the given listener to receive notification of changes to
	 * queries. The listener will be notified whenever a query has been added,
	 * removed, is starting or has finished. Has no effect if an identical
	 * listener is already registered.
	 * 
	 * @param l
	 *            the listener to be added
	 */
	public static void addQueryListener(IQueryListener l) {
		InternalSearchUI.getInstance().addQueryListener(l);
	}
	/**
	 * Removes the given query listener. Does nothing if the listener is not
	 * present.
	 * 
	 * @param l
	 *            the listener to be removed.
	 */
	public static void removeQueryListener(IQueryListener l) {
		InternalSearchUI.getInstance().removeQueryListener(l);
	}
	/**
	 * Returns all search queries know to the search ui (i.e. registered via
	 * <code>runQuery()</code> or <code>runQueryInForeground())</code>.
	 * 
	 * @return all search results
	 */
	public static ISearchQuery[] getQueries() {
		return InternalSearchUI.getInstance().getQueries();
	}
	/**
	 * Returns whether the given query is currently running. Queries may be run
	 * by client request or by actions in the search UI.
	 * 
	 * @param query
	 *            the query
	 * @return whether the given query is currently running
	 * @see NewSearchUI#runQuery(ISearchQuery)
	 * @see NewSearchUI#runQueryInForeground(IRunnableContext, ISearchQuery)
	 */
	public static boolean isQueryRunning(ISearchQuery query) {
		return InternalSearchUI.getInstance().isQueryRunning(query);
	}
}