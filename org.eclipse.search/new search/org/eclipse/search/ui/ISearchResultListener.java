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
 * Listener interface for changes to <code>ISearchResult</code>.
 * 
 * This API is preliminary and subject to change at any time.
 * 
 * @since 3.0
 */
public interface ISearchResultListener {
	/**
	 * Called to notify listeners of changes in a <code>ISearchResult</code>.
	 * The event object <code>e</code> can only guarantueed to be valid for
	 * the duration of the call.
	 * 
	 * @param e The event object describing the change. Note that
	 *            implementers of <code>ISearchResult</code> will be sending
	 *            subclasses of <code>SearchResultEvent</code>.
	 */
	void searchResultChanged(SearchResultEvent e);
}
