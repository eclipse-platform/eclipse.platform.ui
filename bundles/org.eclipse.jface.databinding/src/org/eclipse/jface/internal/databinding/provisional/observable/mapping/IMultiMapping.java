/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.observable.mapping;

/**
 * @since 3.2
 *
 */
public interface IMultiMapping {

	/**
	 * @param element
	 * @param indices 
	 * @return the mapping value for the given element
	 */
	public Object[] getMappingValues(Object element, int[] indices);

	/**
	 * @param element
	 * @param indices 
	 * @param values 
	 */
	public void setMappingValues(Object element, int[] indices, Object[] values);

}
