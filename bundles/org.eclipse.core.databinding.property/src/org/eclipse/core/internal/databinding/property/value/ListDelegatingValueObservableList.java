/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 262269
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
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.value.DelegatingValueProperty;

/**
 * @since 1.2
 */
public class ListDelegatingValueObservableList extends AbstractObservableList
		implements IPropertyObservable {
	private IObservableList masterList;
	private DelegatingValueProperty detailProperty;
	private DelegatingCache cache;

	private IListChangeListener masterListener = new IListChangeListener() {
		@Override
		public void handleListChange(ListChangeEvent event) {
			if (isDisposed())
				return;

			cache.addAll(masterList);

			// Need both obsolete and new elements to convert diff
			ListDiff diff = convertDiff(event.diff);

			cache.retainAll(masterList);

			fireListChange(diff);
		}

		private ListDiff convertDiff(ListDiff diff) {
			// Convert diff to detail value
			ListDiffEntry[] masterEntries = diff.getDifferences();
			ListDiffEntry[] detailEntries = new ListDiffEntry[masterEntries.length];
			for (int i = 0; i < masterEntries.length; i++) {
				ListDiffEntry masterDifference = masterEntries[i];
				int index = masterDifference.getPosition();
				boolean addition = masterDifference.isAddition();
				Object masterElement = masterDifference.getElement();
				Object detailValue = cache.get(masterElement);

				detailEntries[i] = Diffs.createListDiffEntry(index, addition,
						detailValue);
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
	public ListDelegatingValueObservableList(IObservableList masterList,
			DelegatingValueProperty valueProperty) {
		super(masterList.getRealm());
		this.masterList = masterList;
		this.detailProperty = valueProperty;
		this.cache = new DelegatingCache(getRealm(), valueProperty) {
			@Override
			void handleValueChange(Object masterElement, Object oldValue,
					Object newValue) {
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
	public Object get(int index) {
		getterCalled();
		Object masterElement = masterList.get(index);
		return cache.get(masterElement);
	}

	@Override
	public boolean add(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection c) {
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
	public Iterator iterator() {
		getterCalled();
		return new Iterator() {
			Iterator it = masterList.iterator();

			@Override
			public boolean hasNext() {
				getterCalled();
				return it.hasNext();
			}

			@Override
			public Object next() {
				getterCalled();
				Object masterElement = it.next();
				return cache.get(masterElement);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public Object move(int oldIndex, int newIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection c) {
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
	public Object[] toArray(Object[] a) {
		getterCalled();
		Object[] masterElements = masterList.toArray();
		if (a.length < masterElements.length)
			a = (Object[]) Array.newInstance(a.getClass().getComponentType(),
					masterElements.length);
		for (int i = 0; i < masterElements.length; i++) {
			a[i] = cache.get(masterElements[i]);
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
	public ListIterator listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator listIterator(final int index) {
		getterCalled();
		return new ListIterator() {
			ListIterator it = masterList.listIterator(index);
			Object lastMasterElement;
			Object lastElement;
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
			public Object next() {
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
			public Object previous() {
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
			public void set(Object o) {
				checkRealm();
				if (!haveIterated)
					throw new IllegalStateException();

				cache.put(lastMasterElement, o);

				lastElement = o;
			}
		};
	}

	private int[] indicesOf(Object masterElement) {
		List indices = new ArrayList();

		for (ListIterator it = masterList.listIterator(); it.hasNext();) {
			if (masterElement == it.next())
				indices.add(new Integer(it.previousIndex()));
		}

		int[] result = new int[indices.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = ((Integer) indices.get(i)).intValue();
		}
		return result;
	}

	private void fireListChange(int[] indices, Object oldValue, Object newValue) {
		ListDiffEntry[] differences = new ListDiffEntry[indices.length * 2];
		for (int i = 0; i < indices.length; i++) {
			int index = indices[i];
			differences[i * 2] = Diffs.createListDiffEntry(index, false,
					oldValue);
			differences[i * 2 + 1] = Diffs.createListDiffEntry(index, true,
					newValue);
		}
		fireListChange(Diffs.createListDiff(differences));
	}

	@Override
	public Object remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object set(int index, Object o) {
		checkRealm();
		Object masterElement = masterList.get(index);
		return cache.put(masterElement, o);
	}

	@Override
	public Object getObserved() {
		return masterList;
	}

	@Override
	public IProperty getProperty() {
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
