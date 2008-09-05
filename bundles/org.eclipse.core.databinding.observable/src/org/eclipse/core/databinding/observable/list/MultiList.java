/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 222289)
 ******************************************************************************/

package org.eclipse.core.databinding.observable.list;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.runtime.Assert;

/**
 * An observable list backed by an array of observable lists. This class
 * supports all removal methods (including {@link #clear()}), as well as the
 * {@link #set(int, Object)} method. All other mutator methods (addition methods
 * and {@link #move(int, int)}) throw an {@link UnsupportedOperationException}.
 * 
 * @since 1.2
 */
public class MultiList extends AbstractObservableList {
	private IObservableList[] lists;
	private Object elementType;

	private IListChangeListener listChangeListener;
	private IStaleListener staleListener;
	private Boolean stale;

	/**
	 * Constructs a MultiList in the default realm, and backed by the given
	 * observable lists.
	 * 
	 * @param lists
	 *            the array of observable lists backing this MultiList.
	 */
	public MultiList(IObservableList[] lists) {
		this(Realm.getDefault(), lists, null);
	}

	/**
	 * Constructs a MultiList in the default realm backed by the given
	 * observable lists.
	 * 
	 * @param lists
	 *            the array of observable lists backing this MultiList.
	 * @param elementType
	 *            element type of the constructed list.
	 */
	public MultiList(IObservableList[] lists, Object elementType) {
		this(Realm.getDefault(), lists, elementType);
	}

	/**
	 * Constructs a MultiList belonging to the given realm, and backed by the
	 * given observable lists.
	 * 
	 * @param realm
	 *            the observable's realm
	 * @param lists
	 *            the array of observable lists backing this MultiList
	 */
	public MultiList(Realm realm, IObservableList[] lists) {
		this(realm, lists, null);
	}

	/**
	 * Constructs a MultiList belonging to the given realm, and backed by the
	 * given observable lists.
	 * 
	 * @param realm
	 *            the observable's realm
	 * @param lists
	 *            the array of observable lists backing this MultiList
	 * @param elementType
	 *            element type of the constructed list.
	 */
	public MultiList(Realm realm, IObservableList[] lists, Object elementType) {
		super(realm);
		this.lists = lists;
		this.elementType = elementType;

		for (int i = 0; i < lists.length; i++) {
			Assert
					.isTrue(realm.equals(lists[i].getRealm()),
							"All source lists in a MultiList must belong to the same realm"); //$NON-NLS-1$
		}
	}

	protected void firstListenerAdded() {
		if (listChangeListener == null) {
			listChangeListener = new IListChangeListener() {
				public void handleListChange(final ListChangeEvent event) {
					getRealm().exec(new Runnable() {
						public void run() {
							stale = null;
							listChanged(event);
							if (isStale())
								fireStale();
						}
					});
				}
			};
		}
		if (staleListener == null) {
			staleListener = new IStaleListener() {
				public void handleStale(StaleEvent staleEvent) {
					getRealm().exec(new Runnable() {
						public void run() {
							makeStale();
						}
					});
				}
			};
		}

		for (int i = 0; i < lists.length; i++) {
			lists[i].addListChangeListener(listChangeListener);
			lists[i].addStaleListener(staleListener);

			// Determining staleness at this time prevents firing redundant
			// stale events if MultiList happens to be stale now, and a sublist
			// fires a stale event later.
			this.stale = computeStaleness() ? Boolean.TRUE : Boolean.FALSE;
		}
	}

	protected void lastListenerRemoved() {
		if (listChangeListener != null) {
			for (int i = 0; i < lists.length; i++) {
				lists[i].removeListChangeListener(listChangeListener);
			}
			listChangeListener = null;
		}
		if (staleListener != null) {
			for (int i = 0; i < lists.length; i++) {
				lists[i].removeStaleListener(staleListener);
			}
			staleListener = null;
		}
		stale = null;
	}

	private void makeStale() {
		if (stale == null || stale.booleanValue() == false) {
			stale = Boolean.TRUE;
			fireStale();
		}
	}

	private void listChanged(ListChangeEvent event) {
		IObservableList source = event.getObservableList();
		int offset = 0;
		for (int i = 0; i < lists.length; i++) {
			if (source == lists[i]) {
				fireListChange(offsetListDiff(offset, event.diff));
				return;
			}
			offset += lists[i].size();
		}
		Assert
				.isLegal(
						false,
						"MultiList received a ListChangeEvent from an observable list that is not one of its sources."); //$NON-NLS-1$
	}

	private ListDiff offsetListDiff(int offset, ListDiff diff) {
		return Diffs.createListDiff(offsetListDiffEntries(offset, diff
				.getDifferences()));
	}

	private ListDiffEntry[] offsetListDiffEntries(int offset,
			ListDiffEntry[] entries) {
		ListDiffEntry[] offsetEntries = new ListDiffEntry[entries.length];
		for (int i = 0; i < entries.length; i++) {
			offsetEntries[i] = offsetListDiffEntry(offset, entries[i]);
		}
		return offsetEntries;
	}

	private ListDiffEntry offsetListDiffEntry(int offset, ListDiffEntry entry) {
		return Diffs.createListDiffEntry(offset + entry.getPosition(), entry
				.isAddition(), entry.getElement());
	}

	protected int doGetSize() {
		int size = 0;
		for (int i = 0; i < lists.length; i++)
			size += lists[i].size();
		return size;
	}

	public Object getElementType() {
		return elementType;
	}

	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	public void add(int index, Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean addAll(int index, Collection c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		checkRealm();
		for (int i = 0; i < lists.length; i++)
			lists[i].clear();
	}

	public Object get(int index) {
		getterCalled();
		int offset = 0;
		for (int i = 0; i < lists.length; i++) {
			if (index - offset < lists[i].size())
				return lists[i].get(index - offset);
			offset += lists[i].size();
		}
		throw new IndexOutOfBoundsException("index: " + index + ", size: " //$NON-NLS-1$ //$NON-NLS-2$
				+ offset);
	}

	public boolean contains(Object o) {
		getterCalled();
		for (int i = 0; i < lists.length; i++) {
			if (lists[i].contains(o))
				return true;
		}
		return false;
	}

	public boolean equals(Object o) {
		getterCalled();
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof List))
			return false;
		List that = (List) o;
		if (doGetSize() != that.size())
			return false;

		int subListIndex = 0;
		for (int i = 0; i < lists.length; i++) {
			List subList = that.subList(subListIndex, subListIndex
					+ lists[i].size());
			if (!lists[i].equals(subList)) {
				return false;
			}
			subListIndex += lists[i].size();
		}
		return true;
	}

	public int hashCode() {
		getterCalled();
		int result = 1;
		for (int i = 0; i < lists.length; i++) {
			result = result * 31 + lists[i].hashCode();
		}
		return result;
	}

	public int indexOf(Object o) {
		getterCalled();
		int offset = 0;
		for (int i = 0; i < lists.length; i++) {
			int index = lists[i].indexOf(o);
			if (index != -1)
				return offset + index;
			offset += lists[i].size();
		}
		return -1;
	}

	public boolean isEmpty() {
		getterCalled();
		for (int i = 0; i < lists.length; i++) {
			if (!lists[i].isEmpty())
				return false;
		}
		return true;
	}

	public Iterator iterator() {
		getterCalled();
		return new MultiListItr();
	}

	public int lastIndexOf(Object o) {
		getterCalled();
		int offset = size();
		for (int i = 0; i < lists.length; i++) {
			offset -= lists[i].size();
			int index = lists[i].indexOf(o);
			if (index != -1)
				return offset + index;
		}
		return -1;
	}

	public ListIterator listIterator(int index) {
		getterCalled();
		return new MultiListListItr(index);
	}

	public Object move(int oldIndex, int newIndex) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		checkRealm();
		int i = indexOf(o);
		if (i != -1) {
			remove(i);
			return true;
		}
		return false;
	}

	public Object remove(int index) {
		int offset = 0;
		for (int i = 0; i < lists.length; i++) {
			if (index - offset < lists[i].size()) {
				return lists[i].remove(index - offset);
			}
			offset += lists[i].size();
		}
		throw new IndexOutOfBoundsException("index: " + index + ", size: " //$NON-NLS-1$ //$NON-NLS-2$
				+ offset);
	}

	public boolean removeAll(Collection c) {
		boolean changed = false;
		for (int i = 0; i < lists.length; i++) {
			changed = changed | lists[i].removeAll(c);
		}
		return changed;
	}

	public boolean retainAll(Collection c) {
		boolean changed = false;
		for (int i = 0; i < lists.length; i++) {
			changed = changed | lists[i].retainAll(c);
		}
		return changed;
	}

	public Object set(int index, Object o) {
		int offset = 0;
		for (int i = 0; i < lists.length; i++) {
			if (index - offset < lists[i].size()) {
				return lists[i].set(index - offset, o);
			}
			offset += lists[i].size();
		}
		throw new IndexOutOfBoundsException("index: " + index + ", size: " //$NON-NLS-1$ //$NON-NLS-2$
				+ offset);
	}

	public Object[] toArray() {
		getterCalled();
		return toArray(new Object[doGetSize()]);
	}

	public Object[] toArray(Object[] a) {
		getterCalled();
		Object[] result = a;
		if (result.length < doGetSize()) {
			result = (Object[]) Array.newInstance(a.getClass()
					.getComponentType(), doGetSize());
		}
		int offset = 0;
		for (int i = 0; i < lists.length; i++) {
			Object[] oa = lists[i].toArray();
			System.arraycopy(oa, 0, result, offset, oa.length);
			offset += lists[i].size();
		}

		return result;
	}

	public boolean isStale() {
		getterCalled();

		if (staleListener == null || listChangeListener == null) {
			// this.stale is only updated in response to list change events or
			// stale events on the sublists. If we are not listening to sublists
			// then we must calculate staleness on every invocation.
			return computeStaleness();
		}

		if (stale == null) {
			this.stale = computeStaleness() ? Boolean.TRUE : Boolean.FALSE;
		}

		return stale.booleanValue();
	}

	private boolean computeStaleness() {
		boolean stale = false;
		for (int i = 0; i < lists.length; i++) {
			if (lists[i].isStale()) {
				stale = true;
				break;
			}
		}
		return stale;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	public synchronized void dispose() {
		if (lists != null) {
			if (listChangeListener != null) {
				for (int i = 0; i < lists.length; i++) {
					lists[i].removeListChangeListener(listChangeListener);
				}
			}
			if (staleListener != null) {
				for (int i = 0; i < lists.length; i++) {
					lists[i].removeStaleListener(staleListener);
				}
			}
		}
		listChangeListener = null;
		staleListener = null;
		lists = null;
		elementType = null;
		stale = null;
		super.dispose();
	}

	private final class MultiListItr implements Iterator {
		Iterator[] iters;
		int iterIndex = 0;

		MultiListItr() {
			iters = new Iterator[lists.length];
			for (int i = 0; i < lists.length; i++) {
				iters[i] = lists[i].iterator();
			}
		}

		public boolean hasNext() {
			for (int i = iterIndex; i < iters.length; i++) {
				if (iters[i].hasNext())
					return true;
			}
			return false;
		}

		public Object next() {
			while (iterIndex < iters.length && !iters[iterIndex].hasNext())
				iterIndex++;
			return iters[iterIndex].next();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class MultiListListItr implements ListIterator {
		ListIterator[] iters;
		int iterIndex;

		private MultiListListItr(int initialIndex) {
			iters = new ListIterator[lists.length];
			int offset = 0;
			for (int i = 0; i < lists.length; i++) {
				if (offset <= initialIndex) {
					if (offset + lists[i].size() > initialIndex) {
						// current list contains initial index
						iters[i] = lists[i].listIterator(initialIndex - offset);
						iterIndex = i;
					} else {
						// current list ends before initial index
						iters[i] = lists[i].listIterator(lists[i].size());
					}
				} else {
					// current list begins after initial index
					iters[i] = lists[i].listIterator();
				}
				offset += lists[i].size();
			}
		}

		public void add(Object o) {
			throw new UnsupportedOperationException();
		}

		public boolean hasNext() {
			for (int i = iterIndex; i < iters.length; i++) {
				if (iters[i].hasNext())
					return true;
			}
			return false;
		}

		public boolean hasPrevious() {
			for (int i = iterIndex; i >= 0; i--) {
				if (iters[i].hasPrevious())
					return true;
			}
			return false;
		}

		public Object next() {
			while (iterIndex < iters.length && !iters[iterIndex].hasNext())
				iterIndex++;
			return iters[iterIndex].next();
		}

		public int nextIndex() {
			int offset = 0;
			for (int i = 0; i < iterIndex; i++)
				offset += iters[i].nextIndex();
			return offset + iters[iterIndex].nextIndex();
		}

		public Object previous() {
			while (iterIndex >= 0 && !iters[iterIndex].hasPrevious())
				iterIndex--;
			return iters[iterIndex].previous();
		}

		public int previousIndex() {
			int offset = 0;
			for (int i = 0; i < iterIndex; i++)
				offset += iters[i].nextIndex();
			return offset + iters[iterIndex].previousIndex();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void set(Object o) {
			iters[iterIndex].set(o);
		}
	}
}
