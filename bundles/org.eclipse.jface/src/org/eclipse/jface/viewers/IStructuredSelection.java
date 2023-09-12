/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Christoph Läubrich - add support for stream() method
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A selection containing elements.
 */
@SuppressWarnings("rawtypes")
public interface IStructuredSelection extends ISelection, Iterable {
	/**
	 * Returns the first element in this selection, or <code>null</code>
	 * if the selection is empty.
	 *
	 * @return an element, or <code>null</code> if none
	 */
	public Object getFirstElement();

	/**
	 * Returns an iterator over the elements of this selection.
	 *
	 * @return an iterator over the selected elements
	 */
	@Override
	public Iterator iterator();

	/**
	 * Returns the number of elements selected in this selection.
	 *
	 * @return the number of elements selected
	 */
	public int size();

	/**
	 * Returns the elements in this selection as an array.
	 *
	 * @return the selected elements as an array
	 */
	public Object[] toArray();

	/**
	 * Returns the elements in this selection as a <code>List</code>.
	 *
	 * @return the selected elements as a list
	 */
	public List toList();

	/**
	 * Returns the elements in this selection as a <code>Stream</code>.
	 *
	 * @return the selected elements as a stream
	 * @since 3.32
	 */
	@SuppressWarnings("unchecked")
	default Stream<Object> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
}
