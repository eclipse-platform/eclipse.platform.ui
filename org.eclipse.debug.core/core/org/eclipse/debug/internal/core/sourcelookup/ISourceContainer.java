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
package org.eclipse.debug.internal.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;

/**
 * A source container is a container of source code. A source container is
 * capable of searching for source elements by name. For example, a source
 * conatiner may be a project or a directory capable of searching for files
 * by name. A source container may be a composite container - i.e. contain
 * other source containers.
 * 
 * @see ISourceLookupParticipant
 * @see ISourceContainerType
 * @since 3.0
 */
public interface ISourceContainer {

	/**
	 * Returns a collection of source elements in this container corresponding to the
	 * given name. Returns an empty collection if no source elements are found. When
	 * <code>findDuplicates</code> is <code>false</code> the returned collection
	 * should contain at most one source element. If this is a composite container,
	 * the containers contained by this container are also searched.
	 * <p>
	 * The format of the given name is implementation specific but generally conforms
	 * to the format of a file name. If a source container does not recognize the
	 * name format provided, an empty collection should be returned. A source container
	 * may or may not require names to be fully qualified (i.e. be qualified with directory
	 * names).
	 * </p>
	 * @param name the name of the source element to search for
	 * @param findDuplicates whether searching should continue after the first match is
	 *  found, or if all all potential source elements corresponding to the given name
	 *  should be found	
	 * @return a collection of source elements corresponding to the given name
	 * @exception CoreException if an exception occurrs while searching for source elements
	 */
	public Object[] findSourceElements(String name, boolean findDuplicates) throws CoreException;

	/**
	 * The name of this source container that can be used for presentation purposes.
	 * For example, the name of a project.
	 *  
	 * @return the name of this source container
	 */
	public String getName();

	/**
	 * Returns the source containers this container is composed of. An empty
	 * collection is returned if this container is not a composite container.
	 * For example, a workspace source container may be composed project source
	 * containers.
	 * 
	 * @return the source containers this conatiner is composed of, possibly
	 *  an empty collection
	 */
	public ISourceContainer[] getSourceContainers();

	/**
	 * Returns whether this container is a composite container. A composite
	 * container is composed of other source containers. For example a workspace
	 * source container may be composed of project source containers.
	 * 
	 * @return whether this container is a composite container
	 */ 	
	public boolean isComposite();
	
	/**
	 * Returns this container's type.
	 * 
	 * @return this container's type 
	 */
	public ISourceContainerType getType();
	
}
