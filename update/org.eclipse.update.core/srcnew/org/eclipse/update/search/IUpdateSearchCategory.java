/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.search;

/**
 * This interface is used to encapsulate a particular Update
 * search pattern. Each search category is free to scan
 * sites using a specific algorithm. Search category must
 * have a unique ID. The actual search is performed in
 * search queries. A category can provide one or more
 * queries to run during the search.
 */

public interface IUpdateSearchCategory {
/**
 * Returns the unique identifier of this search category.
 */
	public String getId();
/**
 * Accepts the identifier assigned to this category during
 * the registry reading.
 */
	public void setId(String id);

/**
 * Returns an array of update search queries that need to 
 * be run during the search.
 * @return an arry of update search queries
 */	
	IUpdateSearchQuery [] getQueries();
}