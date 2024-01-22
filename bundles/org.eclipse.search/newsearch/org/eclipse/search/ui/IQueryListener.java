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
package org.eclipse.search.ui;


/**
 * <p>A listener for changes to the set of search queries. Queries are added by running
 * them via {@link org.eclipse.search.ui.NewSearchUI#runQueryInBackground(ISearchQuery) NewSearchUI#runQueryInBackground(ISearchQuery)} or
 * {@link org.eclipse.search.ui.NewSearchUI#runQueryInForeground(org.eclipse.jface.operation.IRunnableContext,ISearchQuery) NewSearchUI#runQueryInForeground(IRunnableContext,ISearchQuery)}</p>
 * <p>The search UI determines when queries are rerun, stopped or deleted (and will notify
 * interested parties via this interface). Listeners can be added and removed in the {@link org.eclipse.search.ui.NewSearchUI NewSearchUI} class.
 * </p>
 * <p>Clients may implement this interface.</p>
 *
 * @since 3.0
 */
public interface IQueryListener {
	/**
	 * Called when an query has been added to the system.
	 *
	 * @param query the query that has been added
	 */

	void queryAdded(ISearchQuery query);
	/**
	 * Called when a query has been removed.
	 *
	 * @param query the query that has been removed
	 */
	void queryRemoved(ISearchQuery query);

	/**
	 * Called before an <code>ISearchQuery</code> is starting.
	 * @param query the query about to start
	 */
	void queryStarting(ISearchQuery query);

	/**
	 * Called after an <code>ISearchQuery</code> has finished.
	 * @param query the query that has finished
	 */
	void queryFinished(ISearchQuery query);
}
