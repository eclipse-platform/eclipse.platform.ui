/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
 *                        - add support for factory methods
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.Collection;
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

	/**
	 * Creates a new {@link IStructuredSelection} wrapping the possibly
	 * <code>null</code> object.
	 *
	 * @param obj the element (might be <code>null</code>) to create a
	 *            {@link IStructuredSelection} from.
	 * @return a {@link IStructuredSelection} with this single element, or an empty
	 *         selection if the object is <code>null</code>
	 */
	static <E> IStructuredSelection ofNullable(E obj) {
		if (obj == null) {
			return StructuredSelection.EMPTY;
		}
		return new StructuredSelection(obj);

	}

	/**
	 * Creates a new {@link IStructuredSelection} wrapping the given object.
	 *
	 * @param obj the element (must not be <code>null</code>) to create a single
	 *            element {@link IStructuredSelection} from it.
	 * @return a {@link IStructuredSelection} with this single element
	 */
	static <E> IStructuredSelection of(E obj) {
		return new StructuredSelection(obj);
	}

	/**
	 * Created a new {@link IStructuredSelection} containing the given object
	 *
	 * @param objects the object to contain in the selection
	 * @return a {@link IStructuredSelection} containing the given elements
	 */
	@SafeVarargs
	static <E> IStructuredSelection of(E... objects) {
		if (objects.length == 0) {
			return StructuredSelection.EMPTY;
		}
		return new StructuredSelection(objects);
	}

	/**
	 * create a new {@link IStructuredSelection} using the given collection
	 *
	 * @param collection
	 * @return a {@link IStructuredSelection} containing the elements of the
	 *         collection
	 */
	static <E> IStructuredSelection of(Collection<E> collection) {
		if (collection.isEmpty()) {
			return StructuredSelection.EMPTY;
		}
		if (collection instanceof List<?> list) {
			return new StructuredSelection(list);
		}
		return new StructuredSelection(collection.toArray(), false);
	}

	/**
	 * Returns an empty {@link IStructuredSelection}
	 *
	 * @return a {@link IStructuredSelection} that is empty.
	 */
	static IStructuredSelection empty() {
		return StructuredSelection.EMPTY;
	}
}
