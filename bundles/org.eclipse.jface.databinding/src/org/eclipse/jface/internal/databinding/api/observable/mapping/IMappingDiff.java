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

package org.eclipse.jface.internal.databinding.api.observable.mapping;

import java.util.Set;

/**
 * @since 1.0
 * 
 */
public interface IMappingDiff {
	/**
	 * @return the set of elements for which the mapping value has changed
	 */
	public Set getElements();

	/**
	 * @return an array containing the affected indices.
	 */
	public int[] getAffectedIndices();

	/**
	 * @param element
	 * @param indices 
	 * @return the old values of the mapping for the given element at the given
	 *         indices. 
	 */
	public Object[] getOldMappingValues(Object element, int[] indices);

	/**
	 * @param element
	 * @param indices 
	 * @return the new value of the mapping for the given element
	 */
	public Object[] getNewMappingValues(Object element, int[] indices);
}
