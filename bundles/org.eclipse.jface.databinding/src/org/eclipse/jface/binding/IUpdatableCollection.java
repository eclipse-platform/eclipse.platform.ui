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
package org.eclipse.jface.binding;

/**
 * @since 3.2
 * 
 */
public interface IUpdatableCollection extends IUpdatable {

	/**
	 * @return the size
	 */
	public int getSize();

	/**
	 * @param value
	 * @param index
	 * @return the index where the element was added
	 */
	public int addElement(Object value, int index);

	/**
	 * @param index
	 */
	public void removeElement(int index);

	// TODO rename to updateElement?
	/**
	 * @param index
	 * @param value
	 */
	public void setElement(int index, Object value);

	/**
	 * @param index
	 * @return the element at the given index
	 */
	public Object getElement(int index);

	/**
	 * @return the element type
	 */
	public Class getElementType();
}
