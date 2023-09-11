/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A selection containing elements.
 */
@SuppressWarnings("rawtypes")
public interface IStructuredSelection extends ISelection, Iterable {
	/**
	 * Returns the first element in this selection, or <code>null</code> if the
	 * selection is empty.
	 *
	 * @return an element, or <code>null</code> if none
	 */
	default Object getFirstElement() {
		return stream().findFirst().orElse(null);
	}

	/**
	 * Returns an optional describing the first element of the requested type or an
	 * empty optional if the selection is empty or does not contain any compatible
	 * element in regard to the specified type
	 *
	 * @param type the expected type
	 * @param <T>  the desired type
	 * @return an optional describing the result
	 * @since 3.32
	 */
	default <T> Optional<T> getFirstElementOf(Class<T> type) {
		return streamOf(type).findFirst();
	}

	/**
	 * Returns an optional describing the single selected element of the requested
	 * type or an empty optional if the selection is empty or does not contain
	 * exactly one compatible element in regard to the specified type
	 *
	 * @param type the expected type
	 * @param <T>  the desired type
	 * @return an optional describing the result
	 * @since 3.32
	 */
	default <T> Optional<T> getSingleElementOf(Class<T> type) {
		if (size() == 1) {
			return Optional.ofNullable(getFirstElement()).filter(type::isInstance).map(type::cast);
		}
		return Optional.empty();
	}

	/**
	 * Returns the elements in this selection as a stream.
	 *
	 * @return a (possibly empty) stream of the elements contained in this selection
	 * @since 3.32
	 */
	@SuppressWarnings("unchecked")
	default Stream<Object> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	/**
	 * Return all the elements in this selection as a stream that are of the given
	 * type.
	 *
	 * @param type the desired type of elements
	 * @param <T>  the element type
	 * @return a stream of compatible elements
	 * @since 3.32
	 */
	@SuppressWarnings("unchecked")
	default <T> Stream<T> streamOf(Class<T> type) {
		Objects.requireNonNull(type);
		return StreamSupport.stream(spliterator(), false).filter(type::isInstance).map(type::cast);
	}

	/**
	 * Returns an iterator over the elements of this selection.
	 *
	 * @return an iterator over the selected elements
	 */
	@Override
	default Iterator iterator() {
		return stream().iterator();
	}

	/**
	 * Returns the number of elements selected in this selection.
	 *
	 * @return the number of elements selected
	 */
	public int size();

	@Override
	default boolean isEmpty() {
		return stream().findAny().isEmpty();
	}

	/**
	 * Returns the elements in this selection as an array.
	 *
	 * @return the selected elements as an array
	 */
	default Object[] toArray() {
		return stream().toArray();
	}

	/**
	 * Returns the elements in this selection as a <code>List</code>.
	 *
	 * @return the selected elements as a list
	 */
	default List toList() {
		return stream().toList();
	}
}
