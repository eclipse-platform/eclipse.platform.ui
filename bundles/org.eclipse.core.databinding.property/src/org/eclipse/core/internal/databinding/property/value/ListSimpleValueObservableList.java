/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 262269, 265561, 262287, 268688, 278550
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.identity.IdentityObservableSet;
import org.eclipse.core.internal.databinding.identity.IdentitySet;
import org.eclipse.core.internal.databinding.property.Util;

/**
 * @since 1.2
 */
public class ListSimpleValueObservableList extends AbstractObservableList
		implements IPropertyObservable {
	private IObservableList masterList;
	private SimpleValueProperty detailProperty;

	private IObservableSet knownMasterElements;
	private Map cachedValues;
	private Set staleElements;

	private boolean updating;

	private IListChangeListener masterListener = new IListChangeListener() {
		@Override
		public void handleListChange(ListChangeEvent event) {
			if (!isDisposed()) {
				updateKnownElements();
				fireListChange(convertDiff(event.diff));
			}
		}

		private void updateKnownElements() {
			Set identityKnownElements = new IdentitySet(masterList);
			knownMasterElements.retainAll(identityKnownElements);
			knownMasterElements.addAll(identityKnownElements);
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
				Object elementDetailValue = detailProperty
						.getValue(masterElement);
				detailEntries[i] = Diffs.createListDiffEntry(index, addition,
						elementDetailValue);
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

	private INativePropertyListener detailListener;

	/**
	 * @param masterList
	 * @param valueProperty
	 */
	public ListSimpleValueObservableList(IObservableList masterList,
			SimpleValueProperty valueProperty) {
		super(masterList.getRealm());
		this.masterList = masterList;
		this.detailProperty = valueProperty;

		ISimplePropertyListener listener = new ISimplePropertyListener() {
			@Override
			public void handleEvent(final SimplePropertyEvent event) {
				if (!isDisposed() && !updating) {
					getRealm().exec(new Runnable() {
						@Override
						public void run() {
							if (event.type == SimplePropertyEvent.CHANGE) {
								notifyIfChanged(event.getSource());
							} else if (event.type == SimplePropertyEvent.STALE) {
								boolean wasStale = !staleElements.isEmpty();
								staleElements.add(event.getSource());
								if (!wasStale)
									fireStale();
							}
						}
					});
				}
			}
		};
		this.detailListener = detailProperty.adaptListener(listener);
	}

	@Override
	protected void firstListenerAdded() {
		ObservableTracker.setIgnore(true);
		try {
			knownMasterElements = new IdentityObservableSet(getRealm(), null);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		cachedValues = new IdentityMap();
		staleElements = new IdentitySet();
		knownMasterElements.addSetChangeListener(new ISetChangeListener() {
			@Override
			public void handleSetChange(SetChangeEvent event) {
				for (Iterator it = event.diff.getRemovals().iterator(); it
						.hasNext();) {
					Object key = it.next();
					if (detailListener != null)
						detailListener.removeFrom(key);
					cachedValues.remove(key);
					staleElements.remove(key);
				}
				for (Iterator it = event.diff.getAdditions().iterator(); it
						.hasNext();) {
					Object key = it.next();
					cachedValues.put(key, detailProperty.getValue(key));
					if (detailListener != null)
						detailListener.addTo(key);
				}
			}
		});
		getRealm().exec(new Runnable() {
			@Override
			public void run() {
				knownMasterElements.addAll(masterList);

				masterList.addListChangeListener(masterListener);
				masterList.addStaleListener(staleListener);
			}
		});
	}

	@Override
	protected void lastListenerRemoved() {
		if (masterList != null) {
			masterList.removeListChangeListener(masterListener);
			masterList.removeStaleListener(staleListener);
		}
		if (knownMasterElements != null) {
			knownMasterElements.dispose();
			knownMasterElements = null;
		}
		if (cachedValues != null) {
			cachedValues.clear();
			cachedValues = null;
		}
		if (staleElements != null) {
			staleElements.clear();
			staleElements = null;
		}
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
	public Object getElementType() {
		return detailProperty.getValueType();
	}

	@Override
	public Object get(int index) {
		getterCalled();
		Object masterElement = masterList.get(index);
		return detailProperty.getValue(masterElement);
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

		for (Iterator it = masterList.iterator(); it.hasNext();) {
			if (Util.equals(detailProperty.getValue(it.next()), o))
				return true;
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		getterCalled();
		return masterList.isEmpty();
	}

	@Override
	public boolean isStale() {
		getterCalled();
		return masterList.isStale() || staleElements != null
				&& !staleElements.isEmpty();
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
				return detailProperty.getValue(masterElement);
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
			result[i] = detailProperty.getValue(masterElements[i]);
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
			a[i] = detailProperty.getValue(masterElements[i]);
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
				lastElement = detailProperty.getValue(lastMasterElement);
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
				lastElement = detailProperty.getValue(lastMasterElement);
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

				boolean wasUpdating = updating;
				updating = true;
				try {
					detailProperty.setValue(lastElement, o);
				} finally {
					updating = wasUpdating;
				}

				notifyIfChanged(lastMasterElement);

				lastElement = o;
			}
		};
	}

	private void notifyIfChanged(Object masterElement) {
		if (cachedValues != null) {
			Object oldValue = cachedValues.get(masterElement);
			Object newValue = detailProperty.getValue(masterElement);
			if (!Util.equals(oldValue, newValue)
					|| staleElements.contains(masterElement)) {
				cachedValues.put(masterElement, newValue);
				staleElements.remove(masterElement);
				fireListChange(indicesOf(masterElement), oldValue, newValue);
			}
		}
	}

	private int[] indicesOf(Object masterElement) {
		List indices = new ArrayList();

		for (ListIterator it = ListSimpleValueObservableList.this.masterList
				.listIterator(); it.hasNext();) {
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
		Object oldValue = detailProperty.getValue(masterElement);

		boolean wasUpdating = updating;
		updating = true;
		try {
			detailProperty.setValue(masterElement, o);
		} finally {
			updating = wasUpdating;
		}

		notifyIfChanged(masterElement);

		return oldValue;
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
	public synchronized void dispose() {
		if (knownMasterElements != null) {
			knownMasterElements.clear(); // detaches listeners
			knownMasterElements.dispose();
			knownMasterElements = null;
		}

		if (masterList != null) {
			masterList.removeListChangeListener(masterListener);
			masterList = null;
		}

		masterListener = null;
		detailListener = null;
		detailProperty = null;
		cachedValues = null;
		staleElements = null;

		super.dispose();
	}
}