/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.core;

/**
 * Plays a similar role than SearchReqeustor in eclipse Searches. I.e. a search requestor
 * is some entity accepting the results of a search. Typically the requestor displays the
 * result to the user.
 * <p>
 * This API differs a little from the Eclipse SearchRequestor in that searches are 'live'.
 * I.e the results are updating while the user is typing the query.
 * As the query is changing, this may cause results that were added earlier being changed or
 * revoked.
 *
 * @author Kris De Volder
 */
public class QuickTextSearchRequestor {

	/**
	 * Called when a line of text containing the search text is found.
	 */
	public void add(LineItem match) {}

	/**
	 * Called when a previously added line of text needs to be redisplayed (this happens if
	 * the query has changed but still matches the line. I.e. the line is still a match, but
	 * the highlighting of the search term is different.
	 */
	public void update(LineItem match) {}

	/**
	 * Called when a line of text previously added is no longer a match for the current query.
	 * I.e. the line should no longer be displayed.
	 */
	public void revoke(LineItem line) {}

	/**
	 * Called when all previous results have become revoked at once.
	 * This happens when a query is changed in such a way that it can't be updated
	 * incrementally but needs to be completely restarted.
	 */
	public void clear() {}
}
