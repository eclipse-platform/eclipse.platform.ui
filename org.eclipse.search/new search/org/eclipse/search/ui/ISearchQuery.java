/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
/**
 * Represents a particular search query (i.e. "fina all occurrences of 'foo' in
 * workspace"). When executed, the query must update the given search result
 * with the results of the query. This interface must be implemented by
 * clients.
 * 
 * This API is preliminary and subject to change at any time.
 * 
 * @since 3.0
 */
public interface ISearchQuery {
	/**
	 * Runs this query.
	 * 
	 * @param monitor The progress monitor to be used
	 * @param result The search result where the query should put the results.
	 * @return The status after completion of the search job.
	 */
	IStatus run(IProgressMonitor monitor, ISearchResult result);
	/**
	 * Returns the name of this search job. This will be used, for example to
	 * set the <code>Job</code> name if this search job is executed in the
	 * background.
	 * 
	 * @return The user readeable name of this query.
	 */
	String getName();
}
