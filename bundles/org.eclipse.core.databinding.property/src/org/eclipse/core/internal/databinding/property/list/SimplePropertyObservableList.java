/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 265561, 262287, 268203, 268688
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
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.list.SimpleListProperty;

/**
 * @since 1.2
 * 
 */
public class SimplePropertyObservableList extends AbstractObservableList
		implements IPropertyObservable {
	private Object source;
	private SimpleListProperty property;

	private volatile boolean updating = false;

	private volatile int modCount = 0;

	private INativePropertyListener listener;

	private List cachedList;
	private boolean stale;

	/**
	 * @param realm
	 * @param source
	 * @param property
	 */
	public SimplePropertyObservableList(Realm realm, Object source,
			SimpleListProperty property) {
		super(realm);
		this.source = source;
		this.property = property;
	}

	protected void firstListenerAdded() {
		if (!isDisposed()) {
			if (listener == null) {
				listener = property
						.adaptListener(new ISimplePropertyListener() {
							public void handleEvent(
									final SimplePropertyEvent event) {
								if (!isDisposed() && !updating) {
									getRealm().exec(new Runnable() {
										public void run() {
											if (event.type == SimplePropertyEvent.CHANGE) {
												modCount++;
												notifyIfChanged((ListDiff) event.diff);
											} else if (event.type == SimplePropertyEvent.STALE
													&& !stale) {
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
				public void run() {
					cachedList = new ArrayList(getList());
					stale = false;

					if (listener != null)
						listener.addTo(source);
				}
			});
		}
	}

	protected void lastListenerRemoved() {
		if (listener != null)
			listener.removeFrom(source);

		cachedList = null;
		stale = false;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	public Object getElementType() {
		return property.getElementType();
	}

	// Queries

	private List getList() {
		return property.getList(source);
	}

	protected int doGetSize() {
		return getList().size();
	}

	public boolean contains(Object o) {
		getterCalled();
		return getList().contains(o);
	}

	public boolean containsAll(Collection c) {
		getterCalled();
		return getList().containsAll(c);
	}

	public Object get(int index) {
		getterCalled();
		return getList().get(index);
	}

	public int indexOf(Object o) {
		getterCalled();
		return getList().indexOf(o);
	}

	public boolean isEmpty() {
		getterCalled();
		return getList().isEmpty();
	}

	public int lastIndexOf(Object o) {
		getterCalled();
		return getList().lastIndexOf(o);
	}

	public Object[] toArray() {
		getterCalled();
		return getList().toArray();
	}

	public Object[] toArray(Object[] a) {
		getterCalled();
		return getList().toArray(a);
	}

	// Single change operations

	public boolean add(Object o) {
		checkRealm();
		add(getList().size(), o);
		return true;
	}

	public void add(int index, Object o) {
		checkRealm();
		boolean wasUpdating = updating;
		updating = true;
		List list = new ArrayList(getList());
		list.add(index, o);
		ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(index,
				true, o));
		try {
			property.setList(source, list, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);
	}

	public Iterator iterator() {
		getterCalled();
		return new Iterator() {
			int expectedModCount = modCount;
			List list = new ArrayList(getList());
			ListIterator iterator = list.listIterator();

			Object lastElement = null;
			int lastIndex = -1;

			public boolean hasNext() {
				getterCalled();
				checkForComodification();
				return iterator.hasNext();
			}

			public Object next() {
				getterCalled();
				checkForComodification();
				Object next = lastElement = iterator.next();
				lastIndex = iterator.previousIndex();
				return next;
			}

			public void remove() {
				checkRealm();
				checkForComodification();
				if (lastIndex == -1)
					throw new IllegalStateException();

				iterator.remove(); // stay in sync
				ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
						lastIndex, false, lastElement));

				boolean wasUpdating = updating;
				updating = true;
				try {
					property.setList(source, list, diff);
					modCount++;
				} finally {
					updating = wasUpdating;
				}

				notifyIfChanged(null);

				lastElement = null;
				lastIndex = -1;

				expectedModCount = modCount;
			}

			private void checkForComodification() {
				if (expectedModCount != modCount)
					throw new ConcurrentModificationException();
			}
		};
	}

	public Object move(int oldIndex, int newIndex) {
		checkRealm();

		List list = getList();
		int size = list.size();
		if (oldIndex < 0 || oldIndex >= size || newIndex < 0
				|| newIndex >= size)
			throw new IndexOutOfBoundsException();

		if (oldIndex == newIndex)
			return list.get(oldIndex);

		list = new ArrayList(list);
		Object element = list.remove(oldIndex);
		list.add(newIndex, element);

		ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
				oldIndex, false, element), Diffs.createListDiffEntry(newIndex,
				true, element));

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setList(source, list, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return element;
	}

	public boolean remove(Object o) {
		checkRealm();

		int index = getList().indexOf(o);
		if (index == -1)
			return false;

		remove(index);

		return true;
	}

	public ListIterator listIterator() {
		return listIterator(0);
	}

	public ListIterator listIterator(final int index) {
		getterCalled();
		return new ListIterator() {
			int expectedModCount = modCount;
			List list = new ArrayList(getList());
			ListIterator iterator = list.listIterator(index);

			Object lastElement = null;
			int lastIndex = -1;

			public boolean hasNext() {
				getterCalled();
				checkForComodification();
				return iterator.hasNext();
			}

			public int nextIndex() {
				getterCalled();
				checkForComodification();
				return iterator.nextIndex();
			}

			public Object next() {
				getterCalled();
				checkForComodification();
				lastElement = iterator.next();
				lastIndex = iterator.previousIndex();
				return lastElement;
			}

			public boolean hasPrevious() {
				getterCalled();
				checkForComodification();
				return iterator.hasPrevious();
			}

			public int previousIndex() {
				getterCalled();
				checkForComodification();
				return iterator.previousIndex();
			}

			public Object previous() {
				getterCalled();
				checkForComodification();
				lastElement = iterator.previous();
				lastIndex = iterator.nextIndex();
				return lastElement;
			}

			public void add(Object o) {
				checkRealm();
				checkForComodification();
				int index = iterator.nextIndex();

				iterator.add(o); // keep in sync

				List list = getList();
				list.add(index, o);
				ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
						index, true, o));
				boolean wasUpdating = updating;
				updating = true;
				try {
					property.setList(source, list, diff);
					modCount++;
				} finally {
					updating = wasUpdating;
				}

				notifyIfChanged(null);

				lastElement = null;
				lastIndex = -1;
				expectedModCount = modCount;
			}

			public void set(Object o) {
				checkRealm();
				checkForComodification();

				iterator.set(o);
				ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
						lastIndex, false, lastElement), Diffs
						.createListDiffEntry(lastIndex, true, o));

				boolean wasUpdating = updating;
				updating = true;
				try {
					property.setList(source, list, diff);
					modCount++;
				} finally {
					updating = wasUpdating;
				}

				notifyIfChanged(null);

				lastElement = o;
				expectedModCount = modCount;
			}

			public void remove() {
				checkRealm();
				checkForComodification();
				if (lastIndex == -1)
					throw new IllegalStateException();

				iterator.remove(); // keep in sync
				ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(
						lastIndex, false, lastElement));

				boolean wasUpdating = updating;
				updating = true;
				try {
					property.setList(source, list, diff);
					modCount++;
				} finally {
					updating = wasUpdating;
				}

				notifyIfChanged(null);

				lastElement = null;
				lastIndex = -1;
				expectedModCount = modCount;
			}

			private void checkForComodification() {
				if (expectedModCount != modCount)
					throw new ConcurrentModificationException();
			}
		};
	}

	public Object remove(int index) {
		checkRealm();

		List list = new ArrayList(getList());
		Object element = list.remove(index);
		ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, element));

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setList(source, list, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return element;
	}

	public Object set(int index, Object o) {
		checkRealm();

		List list = new ArrayList(getList());
		Object oldElement = list.set(index, o);

		ListDiff diff = Diffs.createListDiff(Diffs.createListDiffEntry(index,
				false, oldElement), Diffs.createListDiffEntry(index, true, o));

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setList(source, list, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return oldElement;
	}

	public List subList(int fromIndex, int toIndex) {
		getterCalled();
		return Collections.unmodifiableList(getList().subList(fromIndex,
				toIndex));
	}

	// Bulk change operations

	public boolean addAll(Collection c) {
		checkRealm();

		return addAll(getList().size(), c);
	}

	public boolean addAll(int index, Collection c) {
		checkRealm();

		if (c.isEmpty())
			return false;

		List list = new ArrayList(getList());
		list.addAll(index, c);

		ListDiffEntry[] entries = new ListDiffEntry[c.size()];
		int offsetIndex = 0;
		for (Iterator it = c.iterator(); it.hasNext();) {
			Object element = it.next();
			entries[offsetIndex] = Diffs.createListDiffEntry(index
					+ offsetIndex, true, element);
			offsetIndex++;
		}
		ListDiff diff = Diffs.createListDiff(entries);

		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setList(source, list, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return true;
	}

	public boolean removeAll(Collection c) {
		checkRealm();

		if (c.isEmpty())
			return false;

		List list = getList();
		if (list.isEmpty())
			return false;

		list = new ArrayList(list);
		List entries = new ArrayList();
		ListDiff diff;

		boolean wasUpdating = updating;
		updating = true;
		try {
			for (ListIterator it = list.listIterator(); it.hasNext();) {
				Object element = it.next();
				int index = it.previousIndex();
				if (c.contains(element)) {
					it.remove();
					entries.add(Diffs
							.createListDiffEntry(index, false, element));
				}
			}
			if (entries.isEmpty())
				return false;

			diff = Diffs.createListDiff((ListDiffEntry[]) entries
					.toArray(new ListDiffEntry[entries.size()]));
			property.setList(source, list, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return !diff.isEmpty();
	}

	public boolean retainAll(Collection c) {
		checkRealm();

		List list = getList();
		if (list.isEmpty())
			return false;

		if (c.isEmpty()) {
			clear();
			return true;
		}

		list = new ArrayList(list);
		List entries = new ArrayList();
		ListDiff diff;

		boolean wasUpdating = updating;
		updating = true;
		try {
			for (ListIterator it = list.listIterator(); it.hasNext();) {
				Object element = it.next();
				int index = it.previousIndex();
				if (!c.contains(element)) {
					it.remove();
					entries.add(Diffs
							.createListDiffEntry(index, false, element));
				}
			}
			if (entries.isEmpty())
				return false;

			diff = Diffs.createListDiff((ListDiffEntry[]) entries
					.toArray(new ListDiffEntry[entries.size()]));
			property.setList(source, list, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);

		return !diff.isEmpty();
	}

	public void clear() {
		checkRealm();

		List list = getList();
		if (list.isEmpty())
			return;

		List entries = new ArrayList();
		for (Iterator it = list.iterator(); it.hasNext();) {
			// always report 0 as the remove index
			entries.add(Diffs.createListDiffEntry(0, false, it.next()));
		}

		ListDiff diff = Diffs.createListDiff((ListDiffEntry[]) entries
				.toArray(new ListDiffEntry[entries.size()]));
		boolean wasUpdating = updating;
		updating = true;
		try {
			property.setList(source, Collections.EMPTY_LIST, diff);
			modCount++;
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(null);
	}

	private void notifyIfChanged(ListDiff diff) {
		if (hasListeners()) {
			List oldList = cachedList;
			List newList = cachedList = new ArrayList(getList());
			if (diff == null)
				diff = Diffs.computeListDiff(oldList, newList);
			if (!diff.isEmpty() || stale) {
				stale = false;
				fireListChange(diff);
			}
		}
	}

	public boolean isStale() {
		getterCalled();
		return stale;
	}

	public boolean equals(Object o) {
		getterCalled();
		return getList().equals(o);
	}

	public int hashCode() {
		getterCalled();
		return getList().hashCode();
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
				listener.removeFrom(source);
			property = null;
			source = null;
			listener = null;
			stale = false;
		}
		super.dispose();
	}
}
