/*******************************************************************************
 * Copyright (c) 2010, 2015 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 305367)
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.masterdetail;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObservable;
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
 * @param <M>
 *            type of the master observables in the master list
 * @param <E>
 *            type of the detail elements
 * @since 1.4
 */
public class ListDetailValueObservableList<M, E> extends
		AbstractObservableList<E> implements IObserving, RandomAccess {

	private IObservableList<M> masterList;

	private IObservableFactory<? super M, IObservableValue<E>> detailFactory;

	private Object detailType;

	// The list of detail observables.
	private ArrayList<IObservableValue<E>> detailList;

	// Maps every master to a DetailEntry containing the detail observable. This
	// map is used to avoid that multiple detail observables are created for the
	// same master.
	private IdentityMap<M, DetailEntry<E>> masterDetailMap = new IdentityMap<>();

	private IdentitySet<IObservable> staleDetailObservables = new IdentitySet<>();

	private IListChangeListener<M> masterListListener = new IListChangeListener<M>() {
		@Override
		public void handleListChange(ListChangeEvent<? extends M> event) {
			handleMasterListChange(event.diff);
		}
	};

	private IValueChangeListener<E> detailValueListener = new IValueChangeListener<E>() {
		@Override
		public void handleValueChange(ValueChangeEvent<? extends E> event) {
			if (!event.getObservable().isStale()) {
				staleDetailObservables.remove(event.getObservable());
			}
			handleDetailValueChange(event);
		}
	};

	private IStaleListener masterStaleListener = new IStaleListener() {
		@Override
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	private IStaleListener detailStaleListener = new IStaleListener() {
		@Override
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
	public ListDetailValueObservableList(IObservableList<M> masterList,
			IObservableFactory<? super M, IObservableValue<E>> detailFactory,
			Object detailType) {
		super(masterList.getRealm());
		this.masterList = masterList;
		this.detailFactory = detailFactory;
		this.detailType = detailType;
		this.detailList = new ArrayList<>();

		// Add change/stale/dispose listeners on the master list.
		masterList.addListChangeListener(masterListListener);
		masterList.addStaleListener(masterStaleListener);
		masterList.addDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent event) {
				ListDetailValueObservableList.this.dispose();
			}
		});

		List<M> emptyList = Collections.emptyList();
		ListDiff<M> initMasterDiff = Diffs.computeListDiff(emptyList, masterList);
		handleMasterListChange(initMasterDiff);
	}

	@Override
	protected synchronized void firstListenerAdded() {
		for (int i = 0; i < detailList.size(); i++) {
			IObservableValue<E> detail = detailList.get(i);
			detail.addValueChangeListener(detailValueListener);
			detail.addStaleListener(detailStaleListener);
			if (detail.isStale()) {
				staleDetailObservables.add(detail);
			}
		}
	}

	@Override
	protected synchronized void lastListenerRemoved() {
		if (isDisposed()) {
			return;
		}

		for (int i = 0; i < detailList.size(); i++) {
			IObservableValue<E> detail = detailList.get(i);
			detail.removeValueChangeListener(detailValueListener);
			detail.removeStaleListener(detailStaleListener);
		}
		staleDetailObservables.clear();
	}

	private void handleMasterListChange(ListDiff<? extends M> masterListDiff) {
		boolean wasStale = isStale();

		boolean hasListeners = hasListeners();
		ListDiffEntry<? extends M>[] masterEntries = masterListDiff.getDifferences();
		List<ListDiffEntry<E>> detailEntries = new ArrayList<>(masterEntries.length);
		for (int i = 0; i < masterEntries.length; i++) {
			ListDiffEntry<? extends M> masterEntry = masterEntries[i];
			int index = masterEntry.getPosition();

			M masterElement = masterEntry.getElement();
			E detailValue;
			if (masterEntry.isAddition()) {
				detailValue = addDetailObservable(masterElement, index);
			} else {
				detailValue = removeDetailObservable(masterElement, index);
			}

			if (hasListeners) {
				// Create the corresponding diff for the detail list.
				detailEntries.add(Diffs.createListDiffEntry(index,
						masterEntry.isAddition(), detailValue));
			} else {
				detailEntries.add(null);
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

	private E addDetailObservable(M masterElement, int index) {
		DetailEntry<E> detailEntry = masterDetailMap.get(masterElement);
		if (detailEntry != null) {
			// If we already have a detail observable for the given
			// masterElement, we increment the reference count.
			detailEntry.masterReferenceCount++;
			detailList.add(index, detailEntry.detailObservable);
			return detailEntry.detailObservable.getValue();
		}

		IObservableValue<E> detail = createDetailObservable(masterElement);
		masterDetailMap.put(masterElement, new DetailEntry<>(detail));

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

	private E removeDetailObservable(Object masterElement, int index) {
		IObservableValue<E> detail = detailList.remove(index);
		E detailValue = detail.getValue();

		DetailEntry<E> detailEntry = masterDetailMap.get(masterElement);

		// We may only dispose the detail observable when there are no more
		// masters referencing it.
		detailEntry.masterReferenceCount--;
		if (detailEntry.masterReferenceCount == 0) {
			masterDetailMap.remove(masterElement);
			staleDetailObservables.remove(detail);
			detail.dispose();
		}

		return detailValue;
	}

	private void handleDetailValueChange(ValueChangeEvent<? extends E> event) {
		IObservableValue<? extends E> detail = event.getObservableValue();

		// When we get a change event on a detail observable, we must find its
		// position while there may also be duplicate entries.
		BitSet detailIndexes = new BitSet();
		for (int i = 0; i < detailList.size(); i++) {
			if (detailList.get(i) == detail) {
				detailIndexes.set(i);
			}
		}

		// Create the diff for every found position.
		E oldValue = event.diff.getOldValue();
		E newValue = event.diff.getNewValue();
		List<ListDiffEntry<E>> diffEntries = new ArrayList<>(2 * detailIndexes.cardinality());
		for (int b = detailIndexes.nextSetBit(0); b != -1; b = detailIndexes.nextSetBit(b + 1)) {
			diffEntries.add(Diffs.createListDiffEntry(b, false, oldValue));
			diffEntries.add(Diffs.createListDiffEntry(b, true, newValue));
		}
		fireListChange(Diffs.createListDiff(diffEntries));
	}

	private IObservableValue<E> createDetailObservable(M masterElement) {
		ObservableTracker.setIgnore(true);
		try {
			return detailFactory.createObservable(masterElement);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	protected int doGetSize() {
		return detailList.size();
	}

	@Override
	public E get(int index) {
		ObservableTracker.getterCalled(this);
		return detailList.get(index).getValue();
	}

	@Override
	public E set(int index, E element) {
		IObservableValue<E> detail = detailList.get(index);
		E oldElement = detail.getValue();
		detail.setValue(element);
		return oldElement;
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
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getElementType() {
		return detailType;
	}

	@Override
	public boolean isStale() {
		return super.isStale()
				|| (masterList != null && masterList.isStale())
				|| (staleDetailObservables != null && !staleDetailObservables
						.isEmpty());
	}

	@Override
	public Object getObserved() {
		return masterList;
	}

	@Override
	public synchronized void dispose() {
		if (masterList != null) {
			masterList.removeListChangeListener(masterListListener);
			masterList.removeStaleListener(masterStaleListener);
		}

		if (detailList != null) {
			for (IObservableValue<E> detailValue : detailList) {
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

	private static final class DetailEntry<E> {

		private final IObservableValue<E> detailObservable;

		private int masterReferenceCount = 1;

		public DetailEntry(IObservableValue<E> detailObservable) {
			this.detailObservable = detailObservable;
		}
	}
}
