package org.eclipse.search.internal.core;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.core.resources.IResource;

/**
 * Defines a scope to which search results are limited.
 */
public interface ISearchScope {

	/**
	 * Checks whether the given element is enclosed by
	 * this scope or not.
	 *
	 * @param	element	the resource to be checked
	 * @return	<code>true</code> if the resource is inside the search scope
	 * 
	 */
	public boolean encloses(IResource element);
}