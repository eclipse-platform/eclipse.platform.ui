/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *     Mikael Barbero (Eclipse Foundation) - Bug 254570
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;

/**
 * A concrete implementation of the <code>IStructuredSelection</code> interface,
 * suitable for instantiating.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class StructuredSelection implements IStructuredSelection {

	/**
	 * The element that make up this structured selection.
	 */
	private final Object[] elements;

	/**
	 * The element comparer, or <code>null</code>
	 */
	private final IElementComparer comparer;

	/**
	 * The canonical empty selection. This selection should be used instead of
	 * <code>null</code>.
	 */
	public static final StructuredSelection EMPTY = new StructuredSelection();

	/**
	 * Creates a new empty selection.
	 * See also the static field <code>EMPTY</code> which contains an empty selection singleton.
	 *
	 * @see #EMPTY
	 */
	public StructuredSelection() {
		this.elements = null;
		this.comparer = null;
	}

	/**
	 * Creates a structured selection from the given elements.
	 * The given element array must not be <code>null</code>.
	 *
	 * @param elements an array of elements
	 */
	public StructuredSelection(Object[] elements) {
		Assert.isNotNull(elements);
		this.elements = new Object[elements.length];
		System.arraycopy(elements, 0, this.elements, 0, elements.length);
		this.comparer = null;
	}

	/**
	 * Creates a structured selection containing a single object.
	 * The object must not be <code>null</code>.
	 *
	 * @param element the element
	 */
	public StructuredSelection(Object element) {
		Assert.isNotNull(element);
		this.elements = new Object[] { element };
		this.comparer = null;
	}

	/**
	 * Creates a structured selection from the given <code>List</code>.
	 * @param elements list of selected elements
	 */
	public StructuredSelection(List elements) {
		this(elements, null);
	}

	/**
	 * Creates a structured selection from the given <code>List</code> and
	 * element comparer. If an element comparer is provided, it will be used to
	 * determine equality between structured selection objects provided that
	 * they both are based on the same (identical) comparer. See bug
	 *
	 * @param elements
	 *            list of selected elements
	 * @param comparer
	 *            the comparer, or null
	 * @since 3.4
	 */
	public StructuredSelection(List elements, IElementComparer comparer) {
		Assert.isNotNull(elements);
		this.elements = elements.toArray();
		this.comparer = comparer;
	}

	/**
	 * Returns whether this structured selection is equal to the given object.
	 * Two structured selections are equal if they contain the same elements
	 * in the same order.
	 *
	 * @param o the other object
	 * @return <code>true</code> if they are equal, and <code>false</code> otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		//null and other classes
		if (!(o instanceof StructuredSelection)) {
			return false;
		}
		StructuredSelection s2 = (StructuredSelection) o;

		// either or both empty?
		if (isEmpty()) {
			return s2.isEmpty();
		}
		if (s2.isEmpty()) {
			return false;
		}

		boolean useComparer = comparer != null && comparer == s2.comparer;

		//size
		int myLen = elements.length;
		if (myLen != s2.elements.length) {
			return false;
		}
		//element comparison
		for (int i = 0; i < myLen; i++) {
			if (useComparer) {
				if (!comparer.equals(elements[i], s2.elements[i])) {
					return false;
				}
			} else if (!elements[i].equals(s2.elements[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		if (isEmpty()) {
			return 31 + Objects.hashCode(comparer);
		}

		int r;
		if (comparer != null) {
			r = 31 + comparer.hashCode();
			for (Object e : elements) {
				r = 31 * r + (e == null ? 0 : comparer.hashCode(e));
			}
		} else {
			r = Arrays.hashCode(elements);
		}

		return r;
	}

	@Override
	public Object getFirstElement() {
		return isEmpty() ? null : elements[0];
	}

	@Override
	public boolean isEmpty() {
		return elements == null || elements.length == 0;
	}

	@Override
	public Iterator iterator() {
		return Arrays.asList(elements == null ? new Object[0] : elements)
				.iterator();
	}

	@Override
	public int size() {
		return elements == null ? 0 : elements.length;
	}

	@Override
	public Object[] toArray() {
		return elements == null ? new Object[0] : elements.clone();
	}

	@Override
	public List toList() {
		return Arrays.asList(elements == null ? new Object[0] : elements);
	}

	@Override
	public Stream<Object> stream() {
		if (isEmpty()) {
			return Stream.empty();
		}
		return Arrays.stream(elements);
	}

	/**
	 * Internal method which returns a string representation of this
	 * selection suitable for debug purposes only.
	 *
	 * @return debug string
	 */
	@Override
	public String toString() {
		return isEmpty() ? JFaceResources.getString("<empty_selection>") //$NON-NLS-1$
				: Arrays.toString(elements);
	}
}
