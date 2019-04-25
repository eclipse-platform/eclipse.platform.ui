/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import org.eclipse.jface.viewers.IElementComparer;

/**
 * A wrapper class for viewer elements, which uses an {@link IElementComparer}
 * for computing {@link Object#equals(Object) equality} and
 * {@link Object#hashCode() hashes}.
 *
 * @param <T>
 *            the type of the wrapped object
 *
 * @since 1.2
 */
public class ViewerElementWrapper<T> {
	private final T element;
	private final IElementComparer comparer;

	/**
	 * Constructs a ViewerElementWrapper wrapping the given element
	 *
	 * @param element
	 *            the element being wrapped
	 * @param comparer
	 *            the comparer to use for computing equality and hash codes.
	 */
	public ViewerElementWrapper(T element, IElementComparer comparer) {
		if (comparer == null)
			throw new NullPointerException();
		this.element = element;
		this.comparer = comparer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ViewerElementWrapper)) {
			return false;
		}
		return comparer.equals(element, ((ViewerElementWrapper<T>) obj).element);
	}

	@Override
	public int hashCode() {
		return comparer.hashCode(element);
	}

	T unwrap() {
		return element;
	}
}