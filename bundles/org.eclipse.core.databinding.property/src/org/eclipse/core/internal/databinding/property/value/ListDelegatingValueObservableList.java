/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 262269
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;

/**
 * @param <S>
 *            type of the source object
 * @param <T>
 *            type of the value of the property
 * @param <E>
 *            type of the elements in the list
 * @since 1.2
 */
public class ListDelegatingValueObservableList<S, T extends S, E> extends AbstractObservableList<E>
		implements IPropertyObservable<DelegatingValueProperty<S, E>> {
	private IObservableList<T> masterList;
	private DelegatingValueProperty<S, E> detailProperty;
	private DelegatingCache<S, T, E> cache;

	private IListChangeListener<T> masterListener = new IListChangeListener<T>() {
		@Override
		public void handleListChange(ListChangeEvent<? extends T> event) {
			if (isDisposed())
				return;

			cache.addAll(masterList);

			// Need both obsolete and new elements to convert diff
			ListDiff<E> diff = convertDiff(event.diff);

			cache.retainAll(masterList);

			fireListChange(diff);
		}

		private ListDiff<E> convertDiff(ListDiff<? extends T> diff) {
			// Convert diff to detail value
			ListDiffEntry<? extends T>[] masterEntries = diff.getDifferences();
			List<ListDiffEntry<E>> detailEntries = new ArrayList<>(masterEntries.length);
			for (ListDiffEntry<? extends T> masterDifference : masterEntries) {
				int index = masterDifference.getPosition();
				boolean addition = masterDifference.isAddition();
				T masterElement = masterDifference.getElement();
				E detailValue = cache.get(masterElement);

				detailEntries.add(Diffs.createListDiffEntry(index, addition, detailValue));
			}
			return Diffs.createListDiff(detailEntries);
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		@Override
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	/**
	 * @param masterList
	 * @param valueProperty
	 */
	public ListDelegatingValueObservableList(IObservableList<T> masterList,
			DelegatingValueProperty<S, E> valueProperty) {
		super(masterList.getRealm());
		this.masterList = masterList;
		this.detailProperty = valueProperty;
		this.cache = new DelegatingCache<S, T, E>(getRealm(), valueProperty) {
			@Override
			void handleValueChange(T masterElement, E oldValue, E newValue) {
				fireListChange(indicesOf(masterElement), oldValue, newValue);
			}
		};
		cache.addAll(masterList);

		masterList.addListChangeListener(masterListener);
		masterList.addStaleListener(staleListener);
	}

	@Override
	protected int doGetSize() {
		getterCalled();
		return masterList.size();
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	@Override
	public E get(int index) {
		getterCalled();
		T masterElement = masterList.get(index);
		return cache.get(masterElement);
	}

	@Override
	public boolean add(Object o) {
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
	public boolean contains(Object o) {
		getterCalled();
		return cache.containsValue(o);
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		return masterList.isEmpty();
	}

	@Override
	public boolean isStale() {
		getterCalled();
		return masterList.isStale();
	}

	@Override
	public Iterator<E> iterator() {
		getterCalled();
		return new Iterator<E>() {
			Iterator<T> it = masterList.iterator();

			@Override
			public boolean hasNext() {
				getterCalled();
				return it.hasNext();
			}

			@Override
			public E next() {
				getterCalled();
				T masterElement = it.next();
				return cache.get(masterElement);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public E move(int oldIndex, int newIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		getterCalled();
		Object[] masterElements = masterList.toArray();
		Object[] result = new Object[masterElements.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = cache.get(masterElements[i]);
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> U[] toArray(U[] a) {
		getterCalled();
		Object[] masterElements = masterList.toArray();
		if (a.length < masterElements.length)
			a = (U[]) Array.newInstance(a.getClass().getComponentType(), masterElements.length);
		for (int i = 0; i < masterElements.length; i++) {
			a[i] = (U) cache.get(masterElements[i]);
		}
		return a;
	}

	@Override
	public void add(int index, Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		getterCalled();
		return new ListIterator<E>() {
			ListIterator<T> it = masterList.listIterator(index);
			T lastMasterElement;
			E lastElement;
			boolean haveIterated = false;

			@Override
			public void add(Object arg0) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				getterCalled();
				return it.hasNext();
			}

			@Override
			public boolean hasPrevious() {
				getterCalled();
				return it.hasPrevious();
			}

			@Override
			public E next() {
				getterCalled();
				lastMasterElement = it.next();
				lastElement = cache.get(lastMasterElement);
				haveIterated = true;
				return lastElement;
			}

			@Override
			public int nextIndex() {
				getterCalled();
				return it.nextIndex();
			}

			@Override
			public E previous() {
				getterCalled();
				lastMasterElement = it.previous();
				lastElement = cache.get(lastMasterElement);
				haveIterated = true;
				return lastElement;
			}

			@Override
			public int previousIndex() {
				getterCalled();
				return it.previousIndex();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void set(E o) {
				checkRealm();
				if (!haveIterated)
					throw new IllegalStateException();

				cache.put(lastMasterElement, o);

				lastElement = o;
			}
		};
	}

	private int[] indicesOf(Object masterElement) {
		List<Integer> indices = new ArrayList<>();

		for (ListIterator<T> it = masterList.listIterator(); it.hasNext();) {
			if (masterElement == it.next())
				indices.add(new Integer(it.previousIndex()));
		}

		int[] result = new int[indices.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = indices.get(i).intValue();
		}
		return result;
	}

	private void fireListChange(int[] indices, E oldValue, E newValue) {
		List<ListDiffEntry<E>> differences = new ArrayList<>(indices.length * 2);
		for (int i = 0; i < indices.length; i++) {
			int index = indices[i];
			differences.add(Diffs.createListDiffEntry(index, false, oldValue));
			differences.add(Diffs.createListDiffEntry(index, true, newValue));
		}
		fireListChange(Diffs.createListDiff(differences));
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E o) {
		checkRealm();
		T masterElement = masterList.get(index);
		return cache.put(masterElement, o);
	}

	@Override
	public Object getObserved() {
		return masterList;
	}

	@Override
	public DelegatingValueProperty<S, E> getProperty() {
		return detailProperty;
	}

	@Override
	public Object getElementType() {
		return detailProperty.getValueType();
	}

	@Override
	public synchronized void dispose() {
		if (masterList != null) {
			masterList.removeListChangeListener(masterListener);
			masterList.removeStaleListener(staleListener);
			masterList = null;
		}

		if (cache != null) {
			cache.dispose();
			cache = null;
		}

		masterListener = null;
		detailProperty = null;

		super.dispose();
	}
}
