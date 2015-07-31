/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 222289)
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 ******************************************************************************/

package org.eclipse.core.databinding.observable.list;

import java.util.ArrayList;
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
 * @param <E>
 *            the type of the elements in the list
 *
 * @since 1.2
 */
public class MultiList<E> extends AbstractObservableList<E> {
	private List<IObservableList<E>> lists;
	private Object elementType;

	private IListChangeListener<E> listChangeListener;
	private IStaleListener staleListener;
	private Boolean stale;

	/**
	 * Constructs a MultiList in the default realm, and backed by the given
	 * observable lists.
	 *
	 * @param lists
	 *            the array of observable lists backing this MultiList.
	 * @deprecated use MultiList(List<IObservableList<E>>) instead
	 */
	@Deprecated
	public MultiList(IObservableList<E>[] lists) {
		this(Realm.getDefault(), lists, null);
	}

	/**
	 * Constructs a MultiList in the default realm, and backed by the given
	 * observable lists.
	 *
	 * @param lists
	 *            the array of observable lists backing this MultiList.
	 * @since 1.6
	 */
	public MultiList(List<IObservableList<E>> lists) {
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
	 * @deprecated use MultiList(List<IObservableList<E>>, Object) instead
	 */
	@Deprecated
	public MultiList(IObservableList<E>[] lists, Object elementType) {
		this(Realm.getDefault(), lists, elementType);
	}

	/**
	 * Constructs a MultiList in the default realm backed by the given
	 * observable lists.
	 *
	 * @param lists
	 *            the array of observable lists backing this MultiList.
	 * @param elementType
	 *            element type of the constructed list.
	 * @since 1.6
	 */
	public MultiList(List<IObservableList<E>> lists, Object elementType) {
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
	public MultiList(Realm realm, IObservableList<E>[] lists) {
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
	 * @deprecated use MultiList(realm, List<IObservableList<E>>, Object)
	 *             instead
	 */
	@Deprecated
	public MultiList(Realm realm, IObservableList<E>[] lists, Object elementType) {
		super(realm);
		this.lists = new ArrayList<IObservableList<E>>(lists.length);
		for (IObservableList<E> list : lists) {
			this.lists.add(list);
		}
		this.elementType = elementType;

		for (int i = 0; i < lists.length; i++) {
			Assert.isTrue(realm.equals(lists[i].getRealm()),
					"All source lists in a MultiList must belong to the same realm"); //$NON-NLS-1$
		}
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
	 * @since 1.6
	 */
	public MultiList(Realm realm, List<IObservableList<E>> lists, Object elementType) {
		super(realm);
		this.lists = lists;
		this.elementType = elementType;

		for (IObservableList<E> list : lists) {
			Assert.isTrue(realm.equals(list.getRealm()),
					"All source lists in a MultiList must belong to the same realm"); //$NON-NLS-1$
		}
	}

	@Override
	protected void firstListenerAdded() {
		if (listChangeListener == null) {
			listChangeListener = new IListChangeListener<E>() {
				@Override
				public void handleListChange(final ListChangeEvent<? extends E> event) {
					getRealm().exec(new Runnable() {
						@Override
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
				@Override
				public void handleStale(StaleEvent staleEvent) {
					getRealm().exec(new Runnable() {
						@Override
						public void run() {
							makeStale();
						}
					});
				}
			};
		}

		for (IObservableList<E> list : lists) {
			list.addListChangeListener(listChangeListener);
			list.addStaleListener(staleListener);

			// Determining staleness at this time prevents firing redundant
			// stale events if MultiList happens to be stale now, and a sublist
			// fires a stale event later.
			this.stale = computeStaleness() ? Boolean.TRUE : Boolean.FALSE;
		}
	}

	@Override
	protected void lastListenerRemoved() {
		if (listChangeListener != null) {
			for (IObservableList<E> list : lists) {
				list.removeListChangeListener(listChangeListener);
			}
			listChangeListener = null;
		}
		if (staleListener != null) {
			for (IObservableList<E> list : lists) {
				list.removeStaleListener(staleListener);
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

	private void listChanged(ListChangeEvent<? extends E> event) {
		IObservableList<? extends E> source = event.getObservableList();
		int offset = 0;
		for (IObservableList<E> list : lists) {
			if (source == list) {
				fireListChange(offsetListDiff(offset, event.diff));
				return;
			}
			offset += list.size();
		}
		Assert.isLegal(
				false,
				"MultiList received a ListChangeEvent from an observable list that is not one of its sources."); //$NON-NLS-1$
	}

	private ListDiff<E> offsetListDiff(int offset, ListDiff<? extends E> diff) {
		List<ListDiffEntry<E>> differences = offsetListDiffEntries(offset, diff.getDifferences());
		return Diffs.createListDiff(differences);
	}

	private List<ListDiffEntry<E>> offsetListDiffEntries(int offset,
			ListDiffEntry<? extends E>[] entries) {
		List<ListDiffEntry<E>> offsetEntries = new ArrayList<>(entries.length);
		for (ListDiffEntry<? extends E> entry : entries) {
			offsetEntries.add(offsetListDiffEntry(offset, entry));
		}
		return offsetEntries;
	}

	private ListDiffEntry<E> offsetListDiffEntry(int offset, ListDiffEntry<? extends E> entry) {
		return Diffs.<E>createListDiffEntry(offset + entry.getPosition(), entry.isAddition(), entry.getElement());
	}

	@Override
	protected int doGetSize() {
		int size = 0;
		for (IObservableList<E> list : lists) {
			size += list.size();
		}
		return size;
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		checkRealm();
		for (IObservableList<E> list : lists) {
			list.clear();
		}
	}

	@Override
	public E get(int index) {
		getterCalled();
		int offset = 0;
		for (IObservableList<E> list : lists) {
			if (index - offset < list.size())
				return list.get(index - offset);
			offset += list.size();
		}
		throw new IndexOutOfBoundsException("index: " + index + ", size: " //$NON-NLS-1$ //$NON-NLS-2$
				+ offset);
	}

	@Override
	public boolean contains(Object o) {
		getterCalled();
		for (IObservableList<E> list : lists) {
			if (list.contains(o))
				return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		getterCalled();
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof List))
			return false;
		List<?> that = (List<?>) o;
		if (doGetSize() != that.size())
			return false;

		int subListIndex = 0;
		for (IObservableList<E> list : lists) {
			List<?> subList = that.subList(subListIndex, subListIndex + list.size());
			if (!list.equals(subList)) {
				return false;
			}
			subListIndex += list.size();
		}
		return true;
	}

	@Override
	public int hashCode() {
		getterCalled();
		int result = 1;
		for (IObservableList<E> list : lists) {
			result = result * 31 + list.hashCode();
		}
		return result;
	}

	@Override
	public int indexOf(Object o) {
		getterCalled();
		int offset = 0;
		for (IObservableList<E> list : lists) {
			int index = list.indexOf(o);
			if (index != -1)
				return offset + index;
			offset += list.size();
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		for (IObservableList<E> list : lists) {
			if (!list.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Iterator<E> iterator() {
		getterCalled();
		return new MultiListItr();
	}

	@Override
	public int lastIndexOf(Object o) {
		getterCalled();
		int offset = size();
		for (IObservableList<E> list : lists) {
			offset -= list.size();
			int index = list.indexOf(o);
			if (index != -1)
				return offset + index;
		}
		return -1;
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		getterCalled();
		return new MultiListListItr(index);
	}

	@Override
	public E move(int oldIndex, int newIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		checkRealm();
		int i = indexOf(o);
		if (i != -1) {
			remove(i);
			return true;
		}
		return false;
	}

	@Override
	public E remove(int index) {
		int offset = 0;
		for (IObservableList<E> list : lists) {
			if (index - offset < list.size()) {
				return list.remove(index - offset);
			}
			offset += list.size();
		}
		throw new IndexOutOfBoundsException("index: " + index + ", size: " //$NON-NLS-1$ //$NON-NLS-2$
				+ offset);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for (IObservableList<E> list : lists) {
			changed = changed | list.removeAll(c);
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		for (IObservableList<E> list : lists) {
			changed = changed | list.retainAll(c);
		}
		return changed;
	}

	@Override
	public E set(int index, E o) {
		int offset = 0;
		for (IObservableList<E> list : lists) {
			if (index - offset < list.size()) {
				return list.set(index - offset, o);
			}
			offset += list.size();
		}
		throw new IndexOutOfBoundsException("index: " + index + ", size: " //$NON-NLS-1$ //$NON-NLS-2$
				+ offset);
	}

	@Override
	public Object[] toArray() {
		getterCalled();
		return toArray(new Object[doGetSize()]);
	}

	@Override
	public <T> T[] toArray(T[] a) {
		getterCalled();
		return super.toArray(a);
	}

	@Override
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
		for (IObservableList<E> list : lists) {
			if (list.isStale()) {
				stale = true;
				break;
			}
		}
		return stale;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	public synchronized void dispose() {
		if (lists != null) {
			if (listChangeListener != null) {
				for (IObservableList<E> list : lists) {
					list.removeListChangeListener(listChangeListener);
				}
			}
			if (staleListener != null) {
				for (IObservableList<E> list : lists) {
					list.removeStaleListener(staleListener);
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

	private final class MultiListItr implements Iterator<E> {
		List<Iterator<E>> iters;
		int iterIndex = 0;

		MultiListItr() {
			iters = new ArrayList<Iterator<E>>(lists.size());
			for (IObservableList<E> list : lists) {
				iters.add(list.iterator());
			}
		}

		@Override
		public boolean hasNext() {
			for (int i = iterIndex; i < iters.size(); i++) {
				if (iters.get(i).hasNext())
					return true;
			}
			return false;
		}

		@Override
		public E next() {
			while (iterIndex < iters.size() && !iters.get(iterIndex).hasNext())
				iterIndex++;
			return iters.get(iterIndex).next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class MultiListListItr implements ListIterator<E> {
		List<ListIterator<E>> iters;
		int iterIndex;

		private MultiListListItr(int initialIndex) {
			iters = new ArrayList<ListIterator<E>>(lists.size());
			int offset = 0;
			for (int i = 0; i < lists.size(); i++) {
				IObservableList<E> list = lists.get(i);
				if (offset <= initialIndex) {
					if (offset + list.size() > initialIndex) {
						// current list contains initial index
						iters.add(list.listIterator(initialIndex - offset));
						iterIndex = i;
					} else {
						// current list ends before initial index
						iters.add(list.listIterator(list.size()));
					}
				} else {
					// current list begins after initial index
					iters.add(list.listIterator());
				}
				offset += list.size();
			}
		}

		@Override
		public void add(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean hasNext() {
			for (int i = iterIndex; i < iters.size(); i++) {
				if (iters.get(i).hasNext())
					return true;
			}
			return false;
		}

		@Override
		public boolean hasPrevious() {
			for (int i = iterIndex; i >= 0; i--) {
				if (iters.get(i).hasPrevious())
					return true;
			}
			return false;
		}

		@Override
		public E next() {
			while (iterIndex < iters.size() && !iters.get(iterIndex).hasNext())
				iterIndex++;
			return iters.get(iterIndex).next();
		}

		@Override
		public int nextIndex() {
			int offset = 0;
			for (int i = 0; i < iterIndex; i++)
				offset += iters.get(i).nextIndex();
			return offset + iters.get(iterIndex).nextIndex();
		}

		@Override
		public E previous() {
			while (iterIndex >= 0 && !iters.get(iterIndex).hasPrevious())
				iterIndex--;
			return iters.get(iterIndex).previous();
		}

		@Override
		public int previousIndex() {
			int offset = 0;
			for (int i = 0; i < iterIndex; i++)
				offset += iters.get(i).nextIndex();
			return offset + iters.get(iterIndex).previousIndex();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(E o) {
			iters.get(iterIndex).set(o);
		}
	}
}
