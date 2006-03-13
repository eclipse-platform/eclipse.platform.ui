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

package org.eclipse.jface.internal.databinding.internal.viewers;

/**
 * @since 1.0
 *
 */
public interface IIndexMappingDiff {
	
	/**
	 * @return an array of indices for which the function value has changed
	 */
	public int[] getIndices();
	
	/**
	 * @param index
	 * @return the old value of the function at the given index
	 */
	public Object getOldMappingValue(int index);

	/**
	 * @param index
	 * @return the new value of the function at the given index
	 */
	public Object getNewMappingValue(int index);

}
