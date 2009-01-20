/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
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
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.set.SimpleSetProperty;

/**
 * @since 1.2
 * 
 */
public class SimpleSetPropertyObservableSet extends AbstractObservableSet
		implements IPropertyObservable {
	private Object source;
	private SimpleSetProperty property;

	private volatile boolean updating = false;

	private volatile int modCount = 0;

	private INativePropertyListener listener;

	private Set cachedSet;

	/**
	 * @param realm
	 * @param source
	 * @param property
	 */
	public SimpleSetPropertyObservableSet(Realm realm, Object source,
			SimpleSetProperty property) {
		super(realm);
		this.source = source;
		this.property = property;
	}

	protected void firstListenerAdded() {
		if (!isDisposed()) {
			cachedSet = getSet();

			if (listener == null) {
				listener = property
						.adaptListener(new ISimplePropertyListener() {
							public void handlePropertyChange(
									final SimplePropertyEvent event) {
								modCount++;
								if (!isDisposed() && !updating) {
									getRealm().exec(new Runnable() {
										public void run() {
											notifyIfChanged((SetDiff) event.diff);
										}
									});
								}
							}
						});
			}
			property.addListener(source, listener);
		}
	}

	protected void lastListenerRemoved() {
		if (listener != null) {
			property.removeListener(source, listener);
		}

		cachedSet = null;
	}

	protected Set getWrappedSet() {
		return getSet();
	}

	public Object getElementType() {
		return property.getElementType();
	}

	// Queries

	private Set getSet() {
		return property.getSet(source);
	}

	public boolean contains(Object o) {
		getterCalled();
		return getSet().contains(o);
	}

	public boolean containsAll(Collection c) {
		getterCalled();
		return getSet().containsAll(c);
	}

	public boolean isEmpty() {
		getterCalled();
		return getSet().isEmpty();
	}

	public Object[] toArray() {
		getterCalled();
		return getSet().toArray();
	}

	public Object[] toArray(Object[] a) {
		getterCalled();
		return getSet().toArray(a);
	}

	// Single change operations

	public boolean add(Object o) {
		checkRealm();

		Set set = new HashSet(getSet());
		if (!set.add(o))
			return false;

		SetDiff diff = Diffs.createSetDiff(Collections.singleton(o),
				Collections.EMPTY_SET);

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setSet(source, set, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return true;
	}

	public Iterator iterator() {
		getterCalled();
		return new Iterator() {
			int expectedModCount = modCount;
			Set set = new HashSet(getSet());
			Iterator iterator = set.iterator();
			Object last = null;

			public boolean hasNext() {
				getterCalled();
				checkForComodification();
				return iterator.hasNext();
			}

			public Object next() {
				getterCalled();
				checkForComodification();
				last = iterator.next();
				return last;
			}

			public void remove() {
				checkRealm();
				checkForComodification();

				iterator.remove(); // stay in sync
				SetDiff diff = Diffs.createSetDiff(Collections.EMPTY_SET,
						Collections.singleton(last));

				boolean wasUpdating = updating;
				updating = true;
				try {
					property.setSet(source, set, diff);
					modCount++;
				} finally {
					updating = wasUpdating;
				}

				notifyIfChanged(null);

				last = null;
				expectedModCount = modCount;
			}

			private void checkForComodification() {
				if (expectedModCount != modCount)
					throw new ConcurrentModificationException();
			}
		};
	}

	public boolean remove(Object o) {
		getterCalled();

		Set set = new HashSet(getSet());
		if (!set.remove(o))
			return false;

		SetDiff diff = Diffs.createSetDiff(Collections.EMPTY_SET, Collections
				.singleton(o));

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setSet(source, set, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return true;
	}

	// Bulk change operations

	public boolean addAll(Collection c) {
		getterCalled();

		if (c.isEmpty())
			return false;

		Set set = new HashSet(getSet());

		Set additions = new HashSet(c);
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object element = it.next();
			if (set.add(element))
				additions.add(element);
		}

		if (additions.isEmpty())
			return false;

		SetDiff diff = Diffs.createSetDiff(additions, Collections.EMPTY_SET);

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setSet(source, set, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return true;
	}

	public boolean removeAll(Collection c) {
		getterCalled();

		Set set = getSet();
		if (set.isEmpty())
			return false;
		if (c.isEmpty())
			return false;

		set = new HashSet(set);

		Set removals = new HashSet(c);
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object element = it.next();
			if (set.remove(element))
				removals.add(element);
		}

		if (removals.isEmpty())
			return false;

		SetDiff diff = Diffs.createSetDiff(Collections.EMPTY_SET, removals);

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setSet(source, set, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return true;
	}

	public boolean retainAll(Collection c) {
		getterCalled();

		Set set = getSet();
		if (set.isEmpty())
			return false;

		if (c.isEmpty()) {
			clear();
			return true;
		}

		set = new HashSet(set);

		Set removals = new HashSet();
		for (Iterator it = set.iterator(); it.hasNext();) {
			Object element = it.next();
			if (!c.contains(element)) {
				it.remove();
				removals.add(element);
			}
		}

		if (removals.isEmpty())
			return false;

		SetDiff diff = Diffs.createSetDiff(Collections.EMPTY_SET, removals);

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setSet(source, set, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return true;
	}

	public void clear() {
		getterCalled();

		Set set = getSet();
		if (set.isEmpty())
			return;

		SetDiff diff = Diffs.createSetDiff(Collections.EMPTY_SET, set);

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setSet(source, Collections.EMPTY_SET, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);
	}

	private void notifyIfChanged(SetDiff diff) {
		if (hasListeners()) {
			Set oldSet = cachedSet;
			Set newSet = cachedSet = property.getSet(source);
			if (diff == null)
				diff = Diffs.computeSetDiff(oldSet, newSet);
			if (!diff.isEmpty())
				fireSetChange(diff);
		}
	}

	public boolean equals(Object o) {
		getterCalled();
		return getSet().equals(o);
	}

	public int hashCode() {
		getterCalled();
		return getSet().hashCode();
	}

	public Object getObserved() {
		return source;
	}

	public IProperty getProperty() {
		return property;
	}

	public synchronized void dispose() {
		if (!isDisposed()) {
			if (listener != null)
				property.removeListener(source, listener);
			property = null;
			source = null;
			listener = null;
		}
		super.dispose();
	}
}
