/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.search;

/**
 * This class is a storage container for search processing
 * 
 * @since 3.6
 *
 */
public class SearchProcessorInfo {

	private String alternateTerms[] = null;
	private String query = null;
	
	/**
	 * After {@link AbstractSearchProcessor#preSearch(String)} is called, 
	 * this method can be used to change the search query used.
	 * 
	 * @return new search query String, or <code>null</code> for no change.
	 */
	public String getQuery() {
		return query;
	}
	
	/**
	 * Sets the query to search with.
	 * 
	 * @see #getQuery()
	 * @param query
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/** 
	 * After {@link AbstractSearchProcessor#preSearch(String)} is called, 
	 * this method can be used to return an array of alternative search terms
	 * a user may wish to consider.
	 * 
	 * @return String[] of alternate terms, or <code>null</code> for no alternate terms.
	 * An empty array has the same effect as returning <code>null</code>.
	 */
	public String[] getAlternateTerms() {
		return alternateTerms;
	}

	/**
	 * Sets the alternate terms to be considered.
	 * 
	 * @see #getAlternateTerms()
	 * @param alternateTerms
	 */
	public void setAlternateTerms(String[] alternateTerms) {
		this.alternateTerms = alternateTerms;
	}
	
}
