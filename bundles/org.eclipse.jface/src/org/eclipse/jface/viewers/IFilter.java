/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * Interface for filters. Can accept or reject items.
 *
 * @since 3.1
 */
@FunctionalInterface
public interface IFilter {
	/**
	 * Determines if the given object passes this filter.
	 *
	 * @param toTest object to compare against the filter
	 *
	 * @return <code>true</code> if the object is accepted by the filter.
	 */
	public boolean select(Object toTest);
}
