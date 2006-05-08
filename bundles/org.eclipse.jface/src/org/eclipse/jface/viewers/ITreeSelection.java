/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * A selection containing tree paths.
 * 
 * @since 3.2
 *
 */
public interface ITreeSelection extends IStructuredSelection {

	/**
	 * Returns the paths in this selection
	 * 
	 * @return the paths in this selection
	 */
	public TreePath[] getPaths();

	/**
	 * Returns the paths in this selection whose last segment is equal
	 * to the given element
	 * 
	 * @param element the element to get the tree paths for
	 * 
	 * @return the array of tree paths
	 */
	public TreePath[] getPathsFor(Object element);

}
