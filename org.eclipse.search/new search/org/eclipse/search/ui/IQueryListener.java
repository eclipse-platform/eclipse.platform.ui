/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.search.ui;
/**
 * A listener for changes to the set of search queries.
 * This interface is supposed to be implemented by clients.
 * 
 * This API is preliminary and subject to change at any time.
 * 
 * @since 3.0
 */
public interface IQueryListener {
	/**
	 * Called when an query has been added to the system.
	 * 
	 * @param query The query that has been added
	 */
	void queryAdded(ISearchQuery query);
	/**
	 * Called when a query has been removed.
	 * 
	 * @param query The query that has been removed
	 */
	void queryRemoved(ISearchQuery query);
	
	void queryStarting(ISearchQuery query);
	void queryFinished(ISearchQuery query);
}
