/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding;


/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 * 
 */
public interface IUpdatableTree extends IUpdatable {

	/**
	 * @param parentElement 
	 * @param value
	 * @param index
	 * @return the index where the element was added
	 */
	public int addElement(Object parentElement, int index, Object value);

	/**
	 * @param parentElement 
	 * @param index
	 */
	public void removeElement(Object parentElement, int index);

	// TODO rename to updateElement?
	/**
	 * @param parentElement 
	 * @param index
	 * @param value
	 */
	public void setElement(Object parentElement, int index, Object value);
	
	/**
	 * @param parentElement
	 * @param values
	 */
	public void setElements(Object parentElement, Object[] values);

	/**
	 * @param parentElement 
	 * @param index
	 * @return the element at the given index
	 */
	public Object getElement(Object parentElement, int index);
	
	/**
	 * @param parentElement
	 * @return children elements of parentElement
	 */
	public Object[] getElements(Object parentElement);

	/**
	 * 
	 * @return the tree's potential types on the tree
	 */
	public Class[] getTypes();	
		
	/**
	 * Returns the parent for the given element, or <code>null</code> 
	 * indicating that the parent can't be computed. 
	 *
	 * @param element the element
	 * @return the parent element, or <code>null</code> if it
	 *   has none or if the parent cannot be computed
	 */
	public Object getParent(Object element);
	
	
}
