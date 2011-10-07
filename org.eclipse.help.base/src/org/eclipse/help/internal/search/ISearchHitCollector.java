/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.util.List;

/**
 * Search hit collector. The search engine adds hits to it.
 */
public interface ISearchHitCollector {
	
	/**
	 * Adds hits to the result.
	 * 
	 * @param hits the List of raw hits
	 */
	public void addHits(List<SearchHit> hits, String wordsSearched);
	
	/**
	 * An exception occurred in the search. Implementing subclasses should either
	 * rethrow the exception or save a local copy and test for it later.
	 * @param exception
	 * @throws QueryTooComplexException
	 */
	public void addQTCException(QueryTooComplexException exception) throws QueryTooComplexException;
}

