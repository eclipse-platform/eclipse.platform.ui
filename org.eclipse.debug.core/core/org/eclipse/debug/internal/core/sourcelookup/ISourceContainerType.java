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
 * A source container type represents a kind of container of source code.
 * For example, a source container type may be a project or a directory. A specific
 * project or directory is represented by an instance of a source container type,
 * which is called a source container (<code>ISourceContainer</code>).
 * <p>
 * A source container type is contributed via the <code>sourceContainerTypes</code>
 * extension point.
 * </p>
 * 
 * TODO: example plug-in XML definition/description
 * 
 * @see ISourceContainer
 * @since 3.0
 */
public interface ISourceContainerType {

	/**
	 * Creates and returns a new source container of this type
	 * corresponding to the given memento.
	 * 
	 * @param memento a memento for a source conatiner of this source container type
	 * @return a source container corresponding to the given memento
	 * @exception CoreException if unable to construct a source container based
	 *  on the given memento
	 */
	public ISourceContainer createSourceContainer(String memento) throws CoreException;
	
	/**
	 * Constructs and returns a memento for the given source container. A memento
	 * can be used to reconstruct a source container.
	 * 
	 * @param container The container for which a memento should be created. The
	 *  container must of this source container type.
	 * @return a memento for the source container
	 * @exception CoreException if unable to create a memento
	 */
	public String getMemento(ISourceContainer container) throws CoreException;
	
	
	/**
	 * Returns the name of this source container type that can be used for
	 * presentation purposes. For example, <code>Working Set</code> or
	 * <code>Project</code>.  The value returned is
	 * identical to the name specified in plugin.xml by the <code>name</code>
	 * attribute.
	 * 
	 * @return the name of this source container type
	 */
	public String getName();

	/**
	 * Returns the unique identifier associated with this source container type.
	 * The value returned is identical to the identifier specified in plugin.xml by
	 * the <code>id</code> attribute.
	 * 
	 * @return the unique identifier associated with this source container type
	 */
	public String getId();

	
}
