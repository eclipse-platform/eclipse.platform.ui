/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
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

import org.eclipse.core.runtime.*;

/**
 * Interface for resource filters.  A filter determines which file system
 * objects will be visible when a local refresh is performed for an IContainer.
 *
 * @see IFolder#addFilter(String, int, String, int, IProgressMonitor)
 * @see IFolder#removeFilter(String, int, String, int, IProgressMonitor)
 * @see IFolder#getFilters()
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.6
 */
public interface IResourceFilter {

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
	 * 
	 */
	public static final int EXCLUDE_ALL = 2;

	/**
	 * Flag for resource filters indicating that this filter applies to files.
	 */
	public static final int FILES = 4;

	/**
	 * Flag for resource filters indicating that this filter applies to folders.
	 * 
	 */
	public static final int FOLDERS= 8;

	/**
	 * Flag for resource filters indicating that the container children of the
	 * path inherit from this filter as well.
	 * 
	 */
	public static final int INHERITABLE = 16;

	/**
	 * Return the project relative Path towards which this filter is set.
	 * 
	 * @return the project relative Path towards which this filter is set
	 */
	public IPath getPath();
	
	/**
	 * Return the filter type, either INCLUDE_ONLY or EXCLUDE_ALL
	 * 
	 * @return (INCLUDE_ONLY or EXCLUDE_ALL) and/or INHERITABLE
	 */
	public int getType();

	/**
	 * Return the filter id, which matches the resource filter provider ID.
	 * 
	 * @return the resource filter provider id.
	 */
	public String getId();

	/**
	 * Return the filter arguments, or null if no arguments exist.
	 * 
	 * @return the argument string, or null
	 */
	public String getArguments();

	/**
	 * Returns the project which contains this filter.
	 * <p>
	 * This is a resource handle operation; neither the resource 
	 * nor the resulting project need exist.
	 * </p>
	 *
	 * @return the project handle
	 */
	public IProject getProject();
}
