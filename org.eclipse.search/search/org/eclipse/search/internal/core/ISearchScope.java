/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
