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
 *     Matthew Hall - bug 230267
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * An {@link IObservableSet} of elements in a {@link StructuredViewer}. Elements
 * of the set are compared using an {@link IElementComparer} instead of
 * {@link #equals(Object)}.
 * <p>
 * This class is <i>not</i> a strict implementation the {@link IObservableSet}
 * interface. It intentionally violates the {@link Set} contract, which requires
 * the use of {@link #equals(Object)} when comparing elements. This class is
 * designed for use with {@link StructuredViewer} which uses
 * {@link IElementComparer} for element comparisons.
 *
 * @param <E>
 *            the type of the elements in this set
 *
 * @since 1.2
 */
public class ObservableViewerElementSet<E> extends AbstractObservableSet<E> {
	private Set<E> wrappedSet;
	private Object elementType;
	private IElementComparer comparer;

	/**
	 * Constructs an ObservableViewerElementSet on the given {@link Realm} which
	 * uses the given {@link IElementComparer} to compare elements.
	 *
	 * @param realm
	 *            the realm of the constructed set.
	 * @param elementType
	 *            the element type of the constructed set.
	 * @param comparer
	 *            the {@link IElementComparer} used to compare elements.
	 */
	public ObservableViewerElementSet(Realm realm, Object elementType,
			IElementComparer comparer) {
		super(realm);

		Assert.isNotNull(comparer);
		this.wrappedSet = new ViewerElementSet<>(comparer);
		this.elementType = elementType;
		this.comparer = comparer;
	}

	@Override
	protected Set<E> getWrappedSet() {
		return wrappedSet;
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	public Iterator<E> iterator() {
		getterCalled();
		final Iterator<E> wrappedIterator = wrappedSet.iterator();
		return new Iterator<E>() {
			E last;

			@Override
			public boolean hasNext() {
				getterCalled();
				return wrappedIterator.hasNext();
			}

			@Override
			public E next() {
				getterCalled();
				return last = wrappedIterator.next();
			}

			@Override
			public void remove() {
				getterCalled();
				wrappedIterator.remove();
				fireSetChange(Diffs.createSetDiff(Collections.emptySet(), Collections.singleton(last)));
			}
		};
	}

	@Override
	public boolean add(E o) {
		getterCalled();
		boolean changed = wrappedSet.add(o);
		if (changed)
			fireSetChange(Diffs.createSetDiff(Collections.singleton(o), Collections.emptySet()));
		return changed;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		getterCalled();
		Set<E> additions = new ViewerElementSet<>(comparer);
		for (E element : c) {
			if (wrappedSet.add(element))
				additions.add(element);
		}
		boolean changed = !additions.isEmpty();
		if (changed)
			fireSetChange(Diffs.createSetDiff(additions, Collections.emptySet()));
		return changed;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		getterCalled();
		boolean changed = wrappedSet.remove(o);
		if (changed)
			fireSetChange(Diffs.createSetDiff(Collections.emptySet(), Collections.singleton((E) o)));
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		getterCalled();
		Set<E> removals = new ViewerElementSet<>(comparer);
		for (Object e : c) {
			@SuppressWarnings("unchecked")
			E element = (E) e;
			if (wrappedSet.remove(element))
				removals.add(element);
		}
		boolean changed = !removals.isEmpty();
		if (changed)
			fireSetChange(Diffs.createSetDiff(Collections.emptySet(), removals));
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		getterCalled();
		Set<E> removals = new ViewerElementSet<E>(comparer);
		@SuppressWarnings("unchecked")
		E[] toRetain = (E[]) c.toArray();
		outer: for (Iterator<?> iterator = wrappedSet.iterator(); iterator.hasNext();) {
			@SuppressWarnings("unchecked")
			E element = (E) iterator.next();
			// Cannot rely on c.contains(element) because we must compare
			// elements using IElementComparer.
			for (E toRet : toRetain) {
				if (comparer.equals(element, toRet)) {
					continue outer;
				}
			}
			iterator.remove();
			removals.add(element);
		}
		boolean changed = !removals.isEmpty();
		if (changed)
			fireSetChange(Diffs.createSetDiff(Collections.emptySet(), removals));
		return changed;
	}

	@Override
	public void clear() {
		getterCalled();
		if (!wrappedSet.isEmpty()) {
			Set<E> removals = wrappedSet;
			wrappedSet = new ViewerElementSet<>(comparer);
			fireSetChange(Diffs.createSetDiff(Collections.emptySet(), removals));
		}
	}

	/**
	 * Returns an {@link IObservableSet} for holding viewer elements, using the
	 * given {@link IElementComparer} for comparisons.
	 *
	 * @param realm
	 *            the realm of the returned observable
	 * @param elementType
	 *            the element type of the returned set
	 * @param comparer
	 *            the element comparer to use in element comparisons (may be
	 *            null). If null, the returned set will compare elements
	 *            according to the standard contract for {@link Set} interface
	 *            contract.
	 * @return a Set for holding viewer elements, using the given
	 *         {@link IElementComparer} for comparisons.
	 */
	public static <E> IObservableSet<E> withComparer(Realm realm, Object elementType, IElementComparer comparer) {
		if (comparer == null)
			return new WritableSet<>(realm, Collections.emptySet(), elementType);
		return new ObservableViewerElementSet<>(realm, elementType, comparer);
	}
}