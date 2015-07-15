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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.internal.databinding.identity.IdentitySet;

/**
 * @param <M>
 *            type of the master observables in the master set
 * @param <E>
 *            type of the detail elements
 * @since 1.4
 */
public class SetDetailValueObservableMap<M, E> extends
		ComputedObservableMap<M, E> implements IObserving {

	private IObservableFactory<? super M, IObservableValue<E>> observableValueFactory;

	private Map<M, IObservableValue<E>> detailObservableValueMap = new HashMap<>();

	private IdentitySet<IObservableValue<?>> staleDetailObservables = new IdentitySet<>();

	private IStaleListener detailStaleListener = new IStaleListener() {
		@Override
		public void handleStale(StaleEvent staleEvent) {
			addStaleDetailObservable((IObservableValue<?>) staleEvent
					.getObservable());
		}
	};

	/**
	 * @param masterKeySet
	 * @param observableValueFactory
	 * @param detailValueType
	 */
	public SetDetailValueObservableMap(
			IObservableSet<M> masterKeySet,
			IObservableFactory<? super M, IObservableValue<E>> observableValueFactory,
			Object detailValueType) {
		super(masterKeySet, detailValueType);
		this.observableValueFactory = observableValueFactory;
	}

	@Override
	protected void hookListener(final M addedKey) {
		final IObservableValue<E> detailValue = getDetailObservableValue(addedKey);

		detailValue.addValueChangeListener(new IValueChangeListener<E>() {
			@Override
			public void handleValueChange(ValueChangeEvent<? extends E> event) {
				if (!event.getObservableValue().isStale()) {
					staleDetailObservables.remove(detailValue);
				}

				fireSingleChange(addedKey, event.diff.getOldValue(),
						event.diff.getNewValue());
			}
		});

		detailValue.addStaleListener(detailStaleListener);
	}

	@Override
	protected void unhookListener(Object removedKey) {
		if (isDisposed()) {
			return;
		}

		IObservableValue<E> detailValue = detailObservableValueMap.remove(removedKey);
		staleDetailObservables.remove(detailValue);
		detailValue.dispose();
	}

	private IObservableValue<E> getDetailObservableValue(M masterKey) {
		IObservableValue<E> detailValue = detailObservableValueMap.get(masterKey);

		if (detailValue == null) {
			ObservableTracker.setIgnore(true);
			try {
				detailValue = observableValueFactory.createObservable(masterKey);
			} finally {
				ObservableTracker.setIgnore(false);
			}

			detailObservableValueMap.put(masterKey, detailValue);

			if (detailValue.isStale()) {
				addStaleDetailObservable(detailValue);
			}
		}

		return detailValue;
	}

	private void addStaleDetailObservable(IObservableValue<?> detailObservable) {
		boolean wasStale = isStale();
		staleDetailObservables.add(detailObservable);
		if (!wasStale) {
			fireStale();
		}
	}

	@Override
	protected E doGet(M key) {
		IObservableValue<E> detailValue = getDetailObservableValue(key);
		return detailValue.getValue();
	}

	@Override
	protected E doPut(M key, E value) {
		IObservableValue<E> detailValue = getDetailObservableValue(key);
		E oldValue = detailValue.getValue();
		detailValue.setValue(value);
		return oldValue;
	}

	@Override
	public boolean containsKey(Object key) {
		getterCalled();

		return keySet().contains(key);
	}

	@Override
	public E remove(Object key) {
		checkRealm();

		if (!containsKey(key)) {
			return null;
		}

		@SuppressWarnings("unchecked")
		IObservableValue<E> detailValue = getDetailObservableValue((M) key);
		E oldValue = detailValue.getValue();

		keySet().remove(key);

		return oldValue;
	}

	@Override
	public int size() {
		getterCalled();

		return keySet().size();
	}

	@Override
	public boolean isStale() {
		return super.isStale() || staleDetailObservables != null
				&& !staleDetailObservables.isEmpty();
	}

	@Override
	public Object getObserved() {
		return keySet();
	}

	@Override
	public synchronized void dispose() {
		super.dispose();

		observableValueFactory = null;
		detailObservableValueMap = null;
		detailStaleListener = null;
		staleDetailObservables = null;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}
}
