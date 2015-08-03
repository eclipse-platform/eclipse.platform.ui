/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 265561, 262287, 268203, 268688, 301774
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.set;

import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.set.SimpleSetProperty;

/**
 * @param <S>
 *            type of the source object
 * @param <E>
 *            type of the elements in the set
 * @since 1.2
 *
 */
public class SimplePropertyObservableSet<S, E> extends AbstractObservableSet<E>
		implements IPropertyObservable<SimpleSetProperty<S, E>> {
	private S source;
	private SimpleSetProperty<S, E> property;

	private volatile boolean updating = false;

	private volatile int modCount = 0;

	private INativePropertyListener<S> listener;

	private Set<E> cachedSet;
	private boolean stale;

	/**
	 * @param realm
	 * @param source
	 * @param property
	 */
	public SimplePropertyObservableSet(Realm realm, S source, SimpleSetProperty<S, E> property) {
		super(realm);
		this.source = source;
		this.property = property;
	}

	@Override
	protected void firstListenerAdded() {
		if (!isDisposed() && listener == null) {
			listener = property.adaptListener(new ISimplePropertyListener<S, SetDiff<E>>() {
				@Override
				public void handleEvent(final SimplePropertyEvent<S, SetDiff<E>> event) {
					if (!isDisposed() && !updating) {
						getRealm().exec(new Runnable() {
							@Override
							public void run() {
								if (event.type == SimplePropertyEvent.CHANGE) {
									modCount++;
									notifyIfChanged(event.diff);
								} else if (event.type == SimplePropertyEvent.STALE && !stale) {
									stale = true;
									fireStale();
								}
							}
						});
					}
				}
			});
		}

		getRealm().exec(new Runnable() {
			@Override
			public void run() {
				cachedSet = new HashSet<>(getSet());
				stale = false;

				if (listener != null)
					listener.addTo(source);
			}
		});
	}

	@Override
	protected void lastListenerRemoved() {
		if (listener != null)
			listener.removeFrom(source);

		cachedSet.clear();
		cachedSet = null;
		stale = false;
	}

	@Override
	protected Set<E> getWrappedSet() {
		return getSet();
	}

	@Override
	public Object getElementType() {
		return property.getElementType();
	}

	// Queries

	private Set<E> getSet() {
		return property.getSet(source);
	}

	@Override
	public boolean contains(Object o) {
		getterCalled();
		return getSet().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		getterCalled();
		return getSet().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		return getSet().isEmpty();
	}

	@Override
	public Object[] toArray() {
		getterCalled();
		return getSet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		getterCalled();
		return getSet().toArray(a);
	}

	// Single change operations

	private void updateSet(Set<E> set, SetDiff<E> diff) {
		if (!diff.isEmpty()) {
			boolean wasUpdating = updating;
			updating = true;
			try {
				property.updateSet(source, diff);
				modCount++;
			} finally {
				updating = wasUpdating;
			}

			notifyIfChanged(null);
		}
	}

	@Override
	public boolean add(E o) {
		checkRealm();

		Set<E> set = getSet();
		if (set.contains(o))
			return false;

		SetDiff<E> diff = Diffs.createSetDiff(Collections.singleton(o), Collections.<E>emptySet());
		updateSet(set, diff);

		return true;
	}

	@Override
	public Iterator<E> iterator() {
		getterCalled();
		return new Iterator<E>() {
			int expectedModCount = modCount;
			Set<E> set = new HashSet<E>(getSet());
			Iterator<E> iterator = set.iterator();
			E last = null;

			@Override
			public boolean hasNext() {
				getterCalled();
				checkForComodification();
				return iterator.hasNext();
			}

			@Override
			public E next() {
				getterCalled();
				checkForComodification();
				last = iterator.next();
				return last;
			}

			@Override
			public void remove() {
				checkRealm();
				checkForComodification();

				SetDiff<E> diff = Diffs.createSetDiff(Collections.<E>emptySet(), Collections.singleton(last));
				updateSet(set, diff);

				iterator.remove(); // stay in sync

				last = null;
				expectedModCount = modCount;
			}

			private void checkForComodification() {
				if (expectedModCount != modCount)
					throw new ConcurrentModificationException();
			}
		};
	}

	@Override
	public boolean remove(Object o) {
		getterCalled();

		Set<E> set = getSet();
		if (!set.contains(o))
			return false;

		@SuppressWarnings("unchecked")
		// if o is contained, it is an E
		SetDiff<E> diff = Diffs.createSetDiff(Collections.<E>emptySet(), Collections.singleton((E) o));
		updateSet(set, diff);

		return true;
	}

	// Bulk change operations

	@Override
	public boolean addAll(Collection<? extends E> c) {
		getterCalled();

		if (c.isEmpty())
			return false;

		Set<E> set = getSet();
		if (set.containsAll(c))
			return false;

		Set<E> additions = new HashSet<E>(c);
		additions.removeAll(set);

		if (additions.isEmpty())
			return false;

		SetDiff<E> diff = Diffs.createSetDiff(additions, Collections.<E>emptySet());
		updateSet(set, diff);

		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		getterCalled();

		if (c.isEmpty())
			return false;

		Set<E> set = getSet();
		if (set.isEmpty())
			return false;

		Set<Object> removals = new HashSet<Object>(c);
		removals.retainAll(set);
		@SuppressWarnings("unchecked")
		// because we have removed everything that is not an E
		Set<E> typedRemovals = (Set<E>) removals;

		if (removals.isEmpty())
			return false;

		SetDiff<E> diff = Diffs.createSetDiff(Collections.<E>emptySet(), typedRemovals);
		updateSet(set, diff);

		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		getterCalled();

		Set<E> set = getSet();
		if (set.isEmpty())
			return false;

		if (c.isEmpty()) {
			clear();
			return true;
		}

		Set<E> removals = new HashSet<E>(set);
		removals.removeAll(c);

		if (removals.isEmpty())
			return false;

		SetDiff<E> diff = Diffs.createSetDiff(Collections.<E>emptySet(), removals);
		updateSet(set, diff);

		return true;
	}

	@Override
	public void clear() {
		getterCalled();

		Set<E> set = getSet();
		if (set.isEmpty())
			return;

		SetDiff<E> diff = Diffs.createSetDiff(Collections.<E>emptySet(), set);
		updateSet(set, diff);
	}

	private void notifyIfChanged(SetDiff<E> diff) {
		if (hasListeners()) {
			Set<E> oldSet = cachedSet;
			Set<E> newSet = cachedSet = new HashSet<E>(getSet());
			if (diff == null)
				diff = Diffs.computeSetDiff(oldSet, newSet);
			if (!diff.isEmpty() || stale) {
				stale = false;
				fireSetChange(diff);
			}
		}
	}

	@Override
	public boolean isStale() {
		getterCalled();
		return stale;
	}

	@Override
	public boolean equals(Object o) {
		getterCalled();
		return getSet().equals(o);
	}

	@Override
	public int hashCode() {
		getterCalled();
		return getSet().hashCode();
	}

	@Override
	public Object getObserved() {
		return source;
	}

	@Override
	public SimpleSetProperty<S, E> getProperty() {
		return property;
	}

	@Override
	public synchronized void dispose() {
		if (!isDisposed()) {
			if (listener != null)
				listener.removeFrom(source);
			property = null;
			source = null;
			listener = null;
			stale = false;
		}
		super.dispose();
	}
}
