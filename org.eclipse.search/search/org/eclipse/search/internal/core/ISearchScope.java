/*
 * (c) Copyright IBM Corp. 2000, 2003.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.core;

import org.eclipse.core.resources.IResourceProxy;

/**
 * Defines a scope to which search results are limited.
 */
public interface ISearchScope {

	/**
	 * Checks whether the given element is enclosed by
	 * this scope or not.
	 *
	 * @param	element	the resource proxy to be checked
	 * @return	<code>true</code> if the resource is inside the search scope
	 * 
	 */
	public boolean encloses(IResourceProxy element);

	/**
	 * Returns a human readable description of this scope
	 *
	 * @return	the description of this scope as <code>String</code>
	 * 
	 */
	public String getDescription();
}