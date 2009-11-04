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

/**
 * Interface for resource filters.  A filter determines which file system
 * objects will be visible when a local refresh is performed for an IContainer.
 * 
 * @see IFolder#getFilters()
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.6
 */
public interface IFileInfoMatcherDescription {

	/*====================================================================
	 * Constants defining which members are wanted:
	 *====================================================================*/

	/**
	 * Return the filter id, which matches the resource filter provider ID.
	 * 
	 * @return the resource filter provider id.
	 */
	public String getId();

	public void setId(String id);

	/**
	 * Return the filter arguments, or null if no arguments exist.
	 * 
	 * @return the argument string, or null
	 */
	public Object getArguments();

	public void setArguments(Object arguments);
}