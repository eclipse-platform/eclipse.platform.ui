/*******************************************************************************
 * Copyright (c) 2010 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 305367)
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.masterdetail;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.RandomAccess;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.identity.IdentitySet;

/**
 * @since 1.4
 */
public class ListDetailValueObservableList extends AbstractObservableList
		implements IObserving, RandomAccess {

	private IObservableList masterList;

	private IObservableFactory detailFactory;

	private Object detailType;

	// The list of detail observables.
	private ArrayList detailList;

	// Maps every master to a DetailEntry containing the detail observable. This
	// map is used to avoid that multiple detail observables are created for the
	// same master.
	private IdentityMap masterDetailMap = new IdentityMap();

	private IdentitySet staleDetailObservables = new IdentitySet();

	private IListChangeListener masterListListener = new IListChangeListener() {
		public void handleListChange(ListChangeEvent event) {
			handleMasterListChange(event.diff);
		}
	};

	private IValueChangeListener detailValueListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			if (!event.getObservable().isStale()) {
				staleDetailObservables.remove(event.getObservable());
			}
			handleDetailValueChange(event);
		}
	};

	private IStaleListener masterStaleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	private IStaleListener detailStaleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			boolean wasStale = isStale();
			staleDetailObservables.add((staleEvent.getObservable()));
			if (!wasStale) {
				fireStale();
			}
		}
	};

	/**
	 * 
	 * @param masterList
	 * @param detailFactory
	 * @param detailType
	 */
	public ListDetailValueObservableList(IObservableList masterList,
			IObservableFactory detailFactory, Object detailType) {
		super(masterList.getRealm());
		this.masterList = masterList;
		this.detailFactory = detailFactory;
		this.detailType = detailType;
		this.detailList = new ArrayList();

		// Add change/stale/dispose listeners on the master list.
		masterList.addListChangeListener(masterListListener);
		masterList.addStaleListener(masterStaleListener);
		masterList.addDisposeListener(new IDisposeListener() {
			public void handleDispose(DisposeEvent event) {
				ListDetailValueObservableList.this.dispose();
			}
		});

		ListDiff initMasterDiff = Diffs.computeListDiff(Collections.EMPTY_LIST,
				masterList);
		handleMasterListChange(initMasterDiff);
	}

	protected synchronized void firstListenerAdded() {
		for (int i = 0; i < detailList.size(); i++) {
			IObservableValue detail = (IObservableValue) detailList.get(i);
			detail.addValueChangeListener(detailValueListener);
			detail.addStaleListener(detailStaleListener);
			if (detail.isStale()) {
				staleDetailObservables.add(detail);
			}
		}
	}

	protected synchronized void lastListenerRemoved() {
		if (isDisposed()) {
			return;
		}

		for (int i = 0; i < detailList.size(); i++) {
			IObservableValue detail = (IObservableValue) detailList.get(i);
			detail.removeValueChangeListener(detailValueListener);
			detail.removeStaleListener(detailStaleListener);
		}
		staleDetailObservables.clear();
	}

	private void handleMasterListChange(ListDiff masterListDiff) {
		boolean wasStale = isStale();

		boolean hasListeners = hasListeners();
		ListDiffEntry[] masterEntries = masterListDiff.getDifferences();
		ListDiffEntry[] detailEntries = new ListDiffEntry[masterEntries.length];
		for (int i = 0; i < masterEntries.length; i++) {
			ListDiffEntry masterEntry = masterEntries[i];
			int index = masterEntry.getPosition();

			Object masterElement = masterEntry.getElement();
			Object detailValue;
			if (masterEntry.isAddition()) {
				detailValue = addDetailObservable(masterElement, index);
			} else {
				detailValue = removeDetailObservable(masterElement, index);
			}

			if (hasListeners) {
				// Create the corresponding diff for the detail list.
				detailEntries[i] = Diffs.createListDiffEntry(index,
						masterEntry.isAddition(), detailValue);
			}
		}

		if (hasListeners) {
			if (!wasStale && isStale()) {
				fireStale();
			}

			// Fire a list change event with the adapted diff.
			fireListChange(Diffs.createListDiff(detailEntries));
		}
	}

	private Object addDetailObservable(Object masterElement, int index) {
		DetailEntry detailEntry = (DetailEntry) masterDetailMap
				.get(masterElement);
		if (detailEntry != null) {
			// If we already have a detail observable for the given
			// masterElement, we increment the reference count.
			detailEntry.masterReferenceCount++;
			detailList.add(index, detailEntry.detailObservable);
			return detailEntry.detailObservable.getValue();
		}

		IObservableValue detail = createDetailObservable(masterElement);
		masterDetailMap.put(masterElement, new DetailEntry(detail));

		detailList.add(index, detail);

		if (hasListeners()) {
			detail.addValueChangeListener(detailValueListener);
			detail.addStaleListener(detailStaleListener);
			if (detail.isStale()) {
				staleDetailObservables.add(detail);
			}
		}

		return detail.getValue();
	}

	private Object removeDetailObservable(Object masterElement, int index) {
		IObservableValue detail = (IObservableValue) detailList.remove(index);
		Object detailValue = detail.getValue();

		DetailEntry detailEntry = (DetailEntry) masterDetailMap
				.get(masterElement);

		// We may only dispose the detail observable ASA there are no more
		// masters referencing it.
		detailEntry.masterReferenceCount--;
		if (detailEntry.masterReferenceCount == 0) {
			masterDetailMap.remove(masterElement);
			staleDetailObservables.remove(detail);
			detail.dispose();
		}

		return detailValue;
	}

	private void handleDetailValueChange(ValueChangeEvent event) {
		IObservableValue detail = event.getObservableValue();

		// When we get a change event on a detail observable, we must find its
		// position while there may also be duplicate entries.
		BitSet detailIndexes = new BitSet();
		for (int i = 0; i < detailList.size(); i++) {
			if (detailList.get(i) == detail) {
				detailIndexes.set(i);
			}
		}

		// Create the diff for every found position.
		Object oldValue = event.diff.getOldValue();
		Object newValue = event.diff.getNewValue();
		ListDiffEntry[] diffEntries = new ListDiffEntry[2 * detailIndexes
				.cardinality()];
		int diffIndex = 0;
		for (int b = detailIndexes.nextSetBit(0); b != -1; b = detailIndexes
				.nextSetBit(b + 1)) {
			diffEntries[diffIndex++] = Diffs.createListDiffEntry(b, false,
					oldValue);
			diffEntries[diffIndex++] = Diffs.createListDiffEntry(b, true,
					newValue);
		}
		fireListChange(Diffs.createListDiff(diffEntries));
	}

	private IObservableValue createDetailObservable(Object masterElement) {
		ObservableTracker.setIgnore(true);
		try {
			return (IObservableValue) detailFactory
					.createObservable(masterElement);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	protected int doGetSize() {
		return detailList.size();
	}

	public Object get(int index) {
		ObservableTracker.getterCalled(this);
		return ((IObservableValue) detailList.get(index)).getValue();
	}

	public Object set(int index, Object element) {
		IObservableValue detail = (IObservableValue) detailList.get(index);
		Object oldElement = detail.getValue();
		detail.setValue(element);
		return oldElement;
	}

	public Object move(int oldIndex, int newIndex) {
		throw new UnsupportedOperationException();
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public boolean removeAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public boolean retainAll(Collection c) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public Object getElementType() {
		return detailType;
	}

	public boolean isStale() {
		return super.isStale()
				|| (masterList != null && masterList.isStale())
				|| (staleDetailObservables != null && !staleDetailObservables
						.isEmpty());
	}

	public Object getObserved() {
		return masterList;
	}

	public synchronized void dispose() {
		if (masterList != null) {
			masterList.removeListChangeListener(masterListListener);
			masterList.removeStaleListener(masterStaleListener);
		}

		if (detailList != null) {
			for (Iterator iter = detailList.iterator(); iter.hasNext();) {
				IObservableValue detailValue = (IObservableValue) iter.next();
				detailValue.dispose();
			}
			detailList.clear();
		}

		masterList = null;
		detailFactory = null;
		detailType = null;
		masterListListener = null;
		detailValueListener = null;
		masterDetailMap = null;
		staleDetailObservables = null;

		super.dispose();
	}

	private static final class DetailEntry {

		private final IObservableValue detailObservable;

		private int masterReferenceCount = 1;

		public DetailEntry(IObservableValue detailObservable) {
			this.detailObservable = detailObservable;
		}
	}
}
