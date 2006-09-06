/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.structuremergeviewer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.services.IDisposable;

/**
 * An extension to the {@link IStructureCreator} interface that supports the
 * use of shared documents.
 * <p>
 * This interface is not intended to be implemented by clients. Client should instead
 * subclass {@link DocumentStructureCreator}.
 * 
 * @since 3.3
 */
public interface IStructureCreator2 extends IStructureCreator {
	
	/**
	 * Creates a tree structure consisting of <code>IStructureComparator</code>s
	 * from the given object and returns its root object. Implementing this
	 * method typically involves parsing the input object. In case of an error
	 * (e.g. a parsing error) the value <code>null</code> is returned.
	 * <p>
	 * This method is equivalent to
	 * {@link IStructureCreator#getStructure(Object)} with the exception that if
	 * the returned {@link IStructureComparator} also implements the
	 * {@link IDisposable} interface, clients are expected to dispose of the
	 * comparator when it is no longer used. This is done to allow structure creators
	 * to make use of shared resources such a file buffers.
	 * 
	 * @param input
	 *            the object from which to create the tree of
	 *            <code>IStructureComparator</code>
	 * @return the root node of the structure or <code>null</code> in case of
	 *         error
	 * @throws CoreException
	 * @see IStructureCreator#getStructure(Object)
	 */
	IStructureComparator createStructure(Object input) throws CoreException;
}
