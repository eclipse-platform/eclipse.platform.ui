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
 *     Matthew Hall - bug 124684
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * A {@link Set} of elements in a {@link StructuredViewer}. Elements of the set
 * are compared using an {@link IElementComparer} instead of
 * {@link #equals(Object)}.
 * <p>
 * This class is <i>not</i> a strict implementation the {@link Set} interface.
 * It intentionally violates the {@link Set} contract, which requires the use of
 * {@link #equals(Object)} when comparing elements. This class is designed for
 * use with {@link StructuredViewer} which uses {@link IElementComparer} for
 * element comparisons.
 *
 * @param <E>
 *            the type of elements maintained by this set
 *
 * @since 1.2
 */
public class ViewerElementSet<E> implements Set<E> {
	private final Set<ViewerElementWrapper<E>> wrappedSet;
	private final IElementComparer comparer;

	/**
	 * Constructs a ViewerElementSet using the given {@link IElementComparer}.
	 *
	 * @param comparer
	 *            the {@link IElementComparer} used for comparing elements.
	 */
	public ViewerElementSet(IElementComparer comparer) {
		Assert.isNotNull(comparer);
		this.wrappedSet = new HashSet<>();
		this.comparer = comparer;
	}

	/**
	 * Constructs a ViewerElementSet containing all the elements in the
	 * specified collection.
	 *
	 * @param collection
	 *            the collection whose elements are to be added to this set.
	 * @param comparer
	 *            the {@link IElementComparer} used for comparing elements.
	 */
	public ViewerElementSet(Collection<? extends E> collection, IElementComparer comparer) {
		this(comparer);
		addAll(collection);
	}

	@Override
	public boolean add(E o) {
		return wrappedSet.add(new ViewerElementWrapper<>(o, comparer));
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean changed = false;
		for (Iterator<? extends E> iterator = c.iterator(); iterator.hasNext();)
			changed |= wrappedSet.add(new ViewerElementWrapper<>(iterator.next(),
					comparer));
		return changed;
	}

	@Override
	public void clear() {
		wrappedSet.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		return wrappedSet.contains(new ViewerElementWrapper<>((E) o, comparer));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(Collection<?> c) {
		for (Iterator<?> iterator = c.iterator(); iterator.hasNext();)
			if (!wrappedSet.contains(new ViewerElementWrapper<>((E) iterator.next(), comparer)))
				return false;
		return true;
	}

	@Override
	public boolean isEmpty() {
		return wrappedSet.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		final Iterator<ViewerElementWrapper<E>> wrappedIterator = wrappedSet.iterator();
		return new Iterator<E>() {
			@Override
			public boolean hasNext() {
				return wrappedIterator.hasNext();
			}

			@Override
			public E next() {
				return wrappedIterator.next().unwrap();
			}

			@Override
			public void remove() {
				wrappedIterator.remove();
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		return wrappedSet.remove(new ViewerElementWrapper<>((E) o, comparer));
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (Iterator<?> iterator = c.iterator(); iterator.hasNext();)
			changed |= remove(iterator.next());
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// Have to do this the slow way to ensure correct comparisons. i.e.
		// cannot delegate to c.contains(it) since we can't be sure will
		// compare elements the way we want.
		boolean changed = false;
		@SuppressWarnings("unchecked")
		E[] retainAll = (E[]) c.toArray();
		outer: for (Iterator<?> iterator = iterator(); iterator.hasNext();) {
			@SuppressWarnings("unchecked")
			E element = (E) iterator.next();
			for (int i = 0; i < retainAll.length; i++) {
				if (comparer.equals(element, retainAll[i])) {
					continue outer;
				}
			}
			iterator.remove();
			changed = true;
		}
		return changed;
	}

	@Override
	public int size() {
		return wrappedSet.size();
	}

	@Override
	public Object[] toArray() {
		return toArray(new Object[wrappedSet.size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		int size = wrappedSet.size();
		ViewerElementWrapper<T>[] wrappedArray = wrappedSet
				.toArray(new ViewerElementWrapper[size]);
		T[] result = a;
		if (a.length < size) {
			result = (T[]) Array.newInstance(a.getClass()
					.getComponentType(), size);
		}
		for (int i = 0; i < size; i++)
			result[i] = wrappedArray[i].unwrap();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Set))
			return false;
		Set<?> that = (Set<?>) obj;
		return size() == that.size() && containsAll(that);
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (Object element : this) {
			hash += Objects.hashCode(element);
		}
		return hash;
	}

	/**
	 * Returns a Set for holding viewer elements, using the given
	 * {@link IElementComparer} for comparisons.
	 *
	 * @param comparer
	 *            the element comparer to use in element comparisons. If null,
	 *            the returned set will compare elements according to the
	 *            standard contract for {@link Set} interface contract.
	 * @return a Set for holding viewer elements, using the given
	 *         {@link IElementComparer} for comparisons.
	 */
	public static <E> Set<E> withComparer(IElementComparer comparer) {
		if (comparer == null)
			return new HashSet<>();
		return new ViewerElementSet<>(comparer);
	}
}
