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
 * @since 3.2
 * 
 */
public interface IMappingDiff {
	/**
	 * @return the set of elements for which the function value has changed
	 */
	public Set getElements();

	/**
	 * @param element
	 * @return the old value of the function for the given element
	 */
	public Object getOldMappingValue(Object element);

	/**
	 * @param element
	 * @return the new value of the function for the given element
	 */
	public Object getNewMappingValue(Object element);
}
