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
\	 * 
	 * @param parentElement <code>null</code> for root element
	 * @param value is the new tree element as a child of parentElement
	 * @param index
	 * @return the index where the element was added
	 */
	public int addElement(Object parentElement, int index, Object value);

	/**
	 * @param parentElement <code>null</code> for root element
	 * @param index
	 */
	public void removeElement(Object parentElement, int index);
	
	/**
	 * @param parentElement <code>null</code> for root element
	 * @param index
	 * @param value for the updated tree element
	 */
	public void setElement(Object parentElement, int index, Object value);
	
	/**
	 * @param parentElement <code>null</code> for root element
	 * @param values to set as the children of parentElement
	 */
	public void setElements(Object parentElement, Object[] values);

	/**
	 * @param parentElement <code>null</code> for root element
	 * @param index
	 * @return parentElement's chile at the given index
	 */
	public Object getElement(Object parentElement, int index);
	
	/**
	 * @param parentElement <code>null</code> for root element
	 * @return children elements of parentElement
	 */
	public Object[] getElements(Object parentElement);

	/**
	 * 
	 * @return the potential types of tree elements
	 */
	public Class[] getTypes();	
		
	/**
	 * Returns the parent for the given element, or <code>null</code> 
	 * indicating that the parent is at the root.
	 * 
	 * @param element 
	 * @return parent element
	 */
	public Object getParent(Object element);
	
	
}
