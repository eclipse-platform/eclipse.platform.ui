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
public interface IUpdatableTable extends IUpdatableCollection {

	/**
	 * Returns the types for all columns.
	 * @return the column types
	 */
	public Class[] getColumnTypes();
	
	/**
	 * @param index
	 * @return the values 
	 */
	public Object[] getValues(int index);

	/**
	 * @param index
	 * @param element
	 * @param values
	 */
	public void setElementAndValues(int index, Object element, Object[] values);

	/**
	 * @param index
	 * @param element
	 * @param values
	 * @return the position where the new element was inserted
	 */
	public int addElementWithValues(int index, Object element, Object[] values);

	/**
	 * @param index
	 * @param values
	 */
	public void setValues(int index, Object[] values);
	
}
