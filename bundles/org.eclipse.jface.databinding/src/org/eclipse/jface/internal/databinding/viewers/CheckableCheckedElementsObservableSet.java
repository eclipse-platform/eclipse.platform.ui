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
 *     Matthew Hall - initial API and implementation (bug 124684)
 *     Matthew Hall - bug 259380, 283204
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IElementComparer;

/**
 * @param <E> the type of the elements in this set
 *
 * @since 1.2
 */
public class CheckableCheckedElementsObservableSet<E> extends AbstractObservableSet<E> {
	private ICheckable checkable;
	private Set<E> wrappedSet;
	private Object elementType;
	private IElementComparer elementComparer;
	private ICheckStateListener listener;

	/**
	 * Constructs a new instance of the given realm, and checkable,
	 *
	 * @param realm
	 *            the observable's realm
	 * @param wrappedSet
	 *            the set being wrapped
	 * @param elementType
	 *            type of elements in the set
	 * @param elementComparer
	 *            element comparer
	 * @param checkable
	 *            the ICheckable to track
	 */
	public CheckableCheckedElementsObservableSet(Realm realm, final Set<E> wrappedSet, Object elementType,
			IElementComparer elementComparer, ICheckable checkable) {
		super(realm);
		Assert.isNotNull(checkable, "Checkable cannot be null"); //$NON-NLS-1$
		Assert.isNotNull(wrappedSet, "Wrapped set cannot be null"); //$NON-NLS-1$
		this.checkable = checkable;
		this.wrappedSet = wrappedSet;
		this.elementType = elementType;
		this.elementComparer = elementComparer;

		listener = new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				@SuppressWarnings("unchecked")
				E element = (E) event.getElement();
				if (event.getChecked()) {
					if (wrappedSet.add(element))
						fireSetChange(Diffs.createSetDiff(Collections.singleton(element), Collections.emptySet()));
				} else {
					if (wrappedSet.remove(element))
						fireSetChange(Diffs.createSetDiff(Collections.emptySet(), Collections.singleton(element)));
				}
			}
		};
		checkable.addCheckStateListener(listener);
	}

	@Override
	protected Set<E> getWrappedSet() {
		return wrappedSet;
	}

	Set<E> createDiffSet() {
		return ViewerElementSet.withComparer(elementComparer);
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	public boolean add(E o) {
		getterCalled();
		boolean added = wrappedSet.add(o);
		if (added) {
			checkable.setChecked(o, true);
			fireSetChange(Diffs.createSetDiff(Collections.singleton(o), Collections.emptySet()));
		}
		return added;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		getterCalled();
		boolean removed = wrappedSet.remove(o);
		if (removed) {
			checkable.setChecked(o, false);
			fireSetChange(Diffs.createSetDiff(Collections.emptySet(), Collections.singleton((E) o)));
		}
		return removed;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		getterCalled();
		Set<E> additions = createDiffSet();
		for (E element : c) {
			if (wrappedSet.add(element)) {
				checkable.setChecked(element, true);
				additions.add(element);
			}
		}
		boolean changed = !additions.isEmpty();
		if (changed)
			fireSetChange(Diffs.createSetDiff(additions, Collections.emptySet()));
		return changed;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(Collection<?> c) {
		getterCalled();
		Set<E> removals = createDiffSet();
		for (Object element : c) {
			if (wrappedSet.remove(element)) {
				checkable.setChecked(element, false);
				removals.add((E) element);
			}
		}
		boolean changed = !removals.isEmpty();
		if (changed)
			fireSetChange(Diffs.createSetDiff(Collections.emptySet(), removals));
		return changed;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean retainAll(Collection<?> c) {
		getterCalled();

		// To ensure that elements are compared correctly, e.g. ViewerElementSet
		Set<E> toRetain = createDiffSet();
		toRetain.addAll((Collection<E>) c);

		Set<E> removals = createDiffSet();
		for (Iterator<E> iterator = wrappedSet.iterator(); iterator.hasNext();) {
			E element = iterator.next();
			if (!toRetain.contains(element)) {
				iterator.remove();
				checkable.setChecked(element, false);
				removals.add(element);
			}
		}
		boolean changed = !removals.isEmpty();
		if (changed)
			fireSetChange(Diffs.createSetDiff(Collections.emptySet(), removals));
		return changed;
	}

	@Override
	public void clear() {
		getterCalled();
		Set<E> removals = createDiffSet();
		removals.addAll(wrappedSet);
		removeAll(removals);
	}

	@Override
	public Iterator<E> iterator() {
		getterCalled();
		final Iterator<E> wrappedIterator = wrappedSet.iterator();
		return new Iterator<E>() {
			private E last = null;

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
				checkable.setChecked(last, false);
				fireSetChange(Diffs.createSetDiff(Collections.emptySet(), Collections.singleton(last)));
			}
		};
	}

	@Override
	public synchronized void dispose() {
		if (checkable != null) {
			checkable.removeCheckStateListener(listener);
			checkable = null;
			listener = null;
		}
		super.dispose();
	}
}
