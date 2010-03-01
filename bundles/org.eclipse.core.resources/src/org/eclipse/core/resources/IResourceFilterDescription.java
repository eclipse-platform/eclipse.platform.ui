/*******************************************************************************
 * Copyright (c) 2008, 2010 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *     IBM - ongoing development
 *******************************************************************************/
package org.eclipse.core.resources;

/**
 * A description of a resource filter.
 * 
 * A filter determines which file system objects will be visible when a local refresh is 
 * performed for an IContainer.
 *
 * @see IContainer#getFilters()
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.6
 */
public interface IResourceFilterDescription {

	/*====================================================================
	 * Constants defining which members are wanted:
	 *====================================================================*/

	/**
	 * Flag for resource filters indicating that the filter list includes only 
	 * the files matching the filters. All INCLUDE_ONLY filters are applied to
	 * the resource list with an logical OR operation.
	 */
	public static final int INCLUDE_ONLY = 1;

	/**
	 * Flag for resource filters indicating that the filter list excludes all
	 * the files matching the filters.  All EXCLUDE_ALL filters are applied to
	 * the resource list with an logical AND operation.
	 */
	public static final int EXCLUDE_ALL = 2;

	/**
	 * Flag for resource filters indicating that this filter applies to files.
	 */
	public static final int FILES = 4;

	/**
	 * Flag for resource filters indicating that this filter applies to folders.
	 */
	public static final int FOLDERS = 8;

	/**
	 * Flag for resource filters indicating that the container children of the
	 * path inherit from this filter as well.
	 */
	public static final int INHERITABLE = 16;

	/**
	 * Returns the description of the file info matcher corresponding to this resource
	 * filter.
	 * @return the file info matcher description for this resource filter
	 */
	public FileInfoMatcherDescription getFileInfoMatcherDescription();

	/**
	 * Return the resource towards which this filter is set.
	 * 
	 * @return the resource towards which this filter is set
	 */
	public IResource getResource();

	/**
	 * Return the filter type, either INCLUDE_ONLY or EXCLUDE_ALL
	 * 
	 * @return (INCLUDE_ONLY or EXCLUDE_ALL) and/or INHERITABLE
	 */
	public int getType();

}