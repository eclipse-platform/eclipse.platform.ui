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

package org.eclipse.core.internal.databinding.property.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.list.SimpleListProperty;

/**
 * @param <S>
 *            type of the source object
 * @param <E>
 *            type of the elements in the list
 * @since 1.2
 *
 */
public class SimplePropertyObservableList<S, E> extends AbstractObservableList<E>
		implements IPropertyObservable<SimpleListProperty<S, E>> {
	private S source;
	private SimpleListProperty<S, E> property;

	private volatile boolean updating = false;

	private volatile int simplePropertyModCount = 0;

	private INativePropertyListener<S> listener;

	private List<E> cachedList;
	private boolean stale;

	/**
	 * @param realm
	 * @param source
	 * @param property
	 */
	public SimplePropertyObservableList(Realm realm, S source, SimpleListProperty<S, E> property) {
		super(realm);
		this.source = source;
		this.property = property;
	}

	@Override
	protected void firstListenerAdded() {
		if (!isDisposed() && listener == null) {
			listener = property.adaptListener(new ISimplePropertyListener<S, ListDiff<E>>() {
				@Override
				public void handleEvent(final SimplePropertyEvent<S, ListDiff<E>> event) {
					if (!isDisposed() && !updating) {
						getRealm().exec(new Runnable() {
							@Override
							public void run() {
								if (event.type == SimplePropertyEvent.CHANGE) {
									simplePropertyModCount++;
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
				cachedList = new ArrayList<>(getList());
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

		cachedList = null;
		stale = false;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	public Object getElementType() {
		return property.getElementType();
	}

	// Queries

	private List<E> getList() {
		return property.getList(source);
	}

	@Override
	protected int doGetSize() {
		return getList().size();
	}

	@Override
	public boolean contains(Object o) {
		getterCalled();
		return getList().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		getterCalled();
		return getList().containsAll(c);
	}

	@Override
	public E get(int index) {
		getterCalled();
		return getList().get(index);
	}

	@Override
	public int indexOf(Object o) {
		getterCalled();
		return getList().indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		return getList().isEmpty();
	}

	@Override
	public int lastIndexOf(Object o) {
		getterCalled();
		return getList().lastIndexOf(o);
	}

	@Override
	public Object[] toArray() {
		getterCalled();
		return getList().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		getterCalled();
		return getList().toArray(a);
	}

	// Single change operations

	private void updateList(List<E> list, ListDiff<E> diff) {
		if (!diff.isEmpty()) {
			boolean wasUpdating = updating;
			updating = true;
			try {
				property.updateList(source, diff);
				simplePropertyModCount++;
			} finally {
				updating = wasUpdating;
			}

			notifyIfChanged(null);
		}
	}

	@Override
	public boolean add(E o) {
		checkRealm();

		List<E> list = getList();

		ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(list.size(), true, o));
		updateList(list, diff);

		return true;
	}

	@Override
	public void add(int index, E o) {
		checkRealm();

		List<E> list = getList();

		if (index < 0 || index > list.size())
			throw new IndexOutOfBoundsException();

		ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(index, true, o));
		updateList(list, diff);
	}

	@Override
	public Iterator<E> iterator() {
		getterCalled();
		return new Iterator<E>() {
			int expectedModCount = simplePropertyModCount;
			List<E> list = new ArrayList<E>(getList());
			ListIterator<E> iterator = list.listIterator();

			E lastElement = null;
			int lastIndex = -1;

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
				E next = lastElement = iterator.next();
				lastIndex = iterator.previousIndex();
				return next;
			}

			@Override
			public void remove() {
				checkRealm();
				checkForComodification();
				if (lastIndex == -1)
					throw new IllegalStateException();

				iterator.remove(); // stay in sync
				ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(lastIndex, false, lastElement));

				updateList(list, diff);

				lastElement = null;
				lastIndex = -1;

				expectedModCount = simplePropertyModCount;
			}

			private void checkForComodification() {
				if (expectedModCount != simplePropertyModCount)
					throw new ConcurrentModificationException();
			}
		};
	}

	@Override
	public E move(int oldIndex, int newIndex) {
		checkRealm();

		List<E> list = getList();
		int size = list.size();
		if (oldIndex < 0 || oldIndex >= size || newIndex < 0 || newIndex >= size)
			throw new IndexOutOfBoundsException();

		if (oldIndex == newIndex)
			return list.get(oldIndex);

		E element = list.get(oldIndex);

		ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(oldIndex, false, element),
				Diffs.createListDiffEntry(newIndex, true, element));
		updateList(list, diff);

		return element;
	}

	@Override
	public boolean remove(Object o) {
		checkRealm();

		List<E> list = getList();

		int index = list.indexOf(o);
		if (index == -1)
			return false;

		@SuppressWarnings("unchecked")
		// o can only be of type E
		ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(index, false, (E) o));
		updateList(list, diff);

		return true;
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		getterCalled();
		return new ListIterator<E>() {
			int expectedModCount = simplePropertyModCount;
			List<E> list = new ArrayList<E>(getList());
			ListIterator<E> iterator = list.listIterator(index);

			E lastElement = null;
			int lastIndex = -1;

			@Override
			public boolean hasNext() {
				getterCalled();
				checkForComodification();
				return iterator.hasNext();
			}

			@Override
			public int nextIndex() {
				getterCalled();
				checkForComodification();
				return iterator.nextIndex();
			}

			@Override
			public E next() {
				getterCalled();
				checkForComodification();
				lastElement = iterator.next();
				lastIndex = iterator.previousIndex();
				return lastElement;
			}

			@Override
			public boolean hasPrevious() {
				getterCalled();
				checkForComodification();
				return iterator.hasPrevious();
			}

			@Override
			public int previousIndex() {
				getterCalled();
				checkForComodification();
				return iterator.previousIndex();
			}

			@Override
			public E previous() {
				getterCalled();
				checkForComodification();
				lastElement = iterator.previous();
				lastIndex = iterator.nextIndex();
				return lastElement;
			}

			@Override
			public void add(E o) {
				checkRealm();
				checkForComodification();
				int index = iterator.nextIndex();

				ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(index, true, o));
				updateList(list, diff);

				iterator.add(o); // keep in sync

				lastElement = null;
				lastIndex = -1;
				expectedModCount = simplePropertyModCount;
			}

			@Override
			public void set(E o) {
				checkRealm();
				checkForComodification();

				ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(lastIndex, false, lastElement),
						Diffs.createListDiffEntry(lastIndex, true, o));
				updateList(list, diff);

				iterator.set(o);

				lastElement = o;
				expectedModCount = simplePropertyModCount;
			}

			@Override
			public void remove() {
				checkRealm();
				checkForComodification();
				if (lastIndex == -1)
					throw new IllegalStateException();

				ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(lastIndex, false, lastElement));
				updateList(list, diff);

				iterator.remove(); // keep in sync

				lastElement = null;
				lastIndex = -1;
				expectedModCount = simplePropertyModCount;
			}

			private void checkForComodification() {
				if (expectedModCount != simplePropertyModCount)
					throw new ConcurrentModificationException();
			}
		};
	}

	@Override
	public E remove(int index) {
		checkRealm();

		List<E> list = getList();
		E element = list.get(index);

		ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(index, false, element));
		updateList(list, diff);

		return element;
	}

	@Override
	public E set(int index, E o) {
		checkRealm();

		List<E> list = getList();
		E oldElement = list.get(index);

		ListDiff<E> diff = Diffs.createListDiff(Diffs.createListDiffEntry(index, false, oldElement),
				Diffs.createListDiffEntry(index, true, o));
		updateList(list, diff);

		return oldElement;
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		getterCalled();
		return Collections.unmodifiableList(getList().subList(fromIndex, toIndex));
	}

	// Bulk change operations

	@Override
	public boolean addAll(Collection<? extends E> c) {
		checkRealm();

		if (c.isEmpty())
			return false;

		List<E> list = getList();
		return addAll(list, list.size(), c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		checkRealm();

		if (c.isEmpty())
			return false;

		return addAll(getList(), index, c);
	}

	private boolean addAll(List<E> list, int index, Collection<? extends E> c) {
		if (index < 0 || index > list.size())
			throw new IndexOutOfBoundsException();

		List<ListDiffEntry<E>> entries = new ArrayList<ListDiffEntry<E>>(c.size());
		int offsetIndex = 0;
		for (E element : c) {
			entries.add(Diffs.createListDiffEntry(index + offsetIndex, true, element));
			offsetIndex++;
		}
		ListDiff<E> diff = Diffs.createListDiff(entries);

		updateList(list, diff);

		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		checkRealm();

		if (c.isEmpty())
			return false;

		List<E> list = getList();
		if (list.isEmpty())
			return false;

		List<ListDiffEntry<E>> entries = new ArrayList<ListDiffEntry<E>>();
		for (ListIterator<E> it = list.listIterator(); it.hasNext();) {
			int index = it.nextIndex() - entries.size();
			E element = it.next();
			if (c.contains(element)) {
				entries.add(Diffs.createListDiffEntry(index, false, element));
			}
		}

		if (entries.isEmpty())
			return false;

		ListDiff<E> diff = Diffs.createListDiff(entries);
		updateList(list, diff);

		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		checkRealm();

		List<E> list = getList();
		if (list.isEmpty())
			return false;

		if (c.isEmpty()) {
			clear();
			return true;
		}

		List<ListDiffEntry<E>> entries = new ArrayList<ListDiffEntry<E>>();
		for (ListIterator<E> it = list.listIterator(); it.hasNext();) {
			int index = it.nextIndex() - entries.size();
			E element = it.next();
			if (!c.contains(element)) {
				entries.add(Diffs.createListDiffEntry(index, false, element));
			}
		}

		if (entries.isEmpty())
			return false;

		ListDiff<E> diff = Diffs.createListDiff(entries);
		updateList(list, diff);

		return true;
	}

	@Override
	public void clear() {
		checkRealm();

		List<E> list = getList();
		if (list.isEmpty())
			return;

		List<ListDiffEntry<E>> entries = new ArrayList<ListDiffEntry<E>>();
		for (ListIterator<E> it = list.listIterator(list.size()); it.hasPrevious();) {
			// always report 0 as the remove index
			int index = it.previousIndex();
			E element = it.previous();
			entries.add(Diffs.createListDiffEntry(index, false, element));
		}
		ListDiff<E> diff = Diffs.createListDiff(entries);

		updateList(list, diff);
	}

	private void notifyIfChanged(ListDiff<E> diff) {
		if (hasListeners()) {
			List<E> oldList = cachedList;
			List<E> newList = cachedList = new ArrayList<E>(getList());
			if (diff == null)
				diff = Diffs.computeListDiff(oldList, newList);
			if (!diff.isEmpty() || stale) {
				stale = false;
				fireListChange(diff);
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
		return getList().equals(o);
	}

	@Override
	public int hashCode() {
		getterCalled();
		return getList().hashCode();
	}

	@Override
	public Object getObserved() {
		return source;
	}

	@Override
	public SimpleListProperty<S, E> getProperty() {
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
