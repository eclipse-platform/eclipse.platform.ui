/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 221704)
 *     Matthew Hall - bug 223114, 226289, 247875, 246782, 249526, 268022,
 *                    251424
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.masterdetail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObserving;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.ObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;

/**
 * @param <M>
 *            type of the master observable
 * @param <K>
 *            type of the keys to the inner observable map
 * @param <V>
 *            type of the values in the inner observable map
 * @since 1.1
 *
 */
public class DetailObservableMap<M, K, V> extends ObservableMap<K, V>implements IObserving {
	private boolean updating = false;

	private IObservableValue<M> master;
	private IObservableFactory<? super M, IObservableMap<K, V>> detailFactory;

	private IObservableMap<K, V> detailMap;

	private Object detailKeyType;
	private Object detailValueType;

	private IValueChangeListener<M> masterChangeListener = new IValueChangeListener<M>() {
		@Override
		public void handleValueChange(ValueChangeEvent<? extends M> event) {
			if (isDisposed())
				return;
			ObservableTracker.setIgnore(true);
			try {
				Map<K, V> oldMap = new HashMap<K, V>(wrappedMap);
				updateDetailMap();
				fireMapChange(Diffs.computeMapDiff(oldMap, wrappedMap));
			} finally {
				ObservableTracker.setIgnore(false);
			}
		}
	};

	private IMapChangeListener<K, V> detailChangeListener = new IMapChangeListener<K, V>() {
		@Override
		public void handleMapChange(MapChangeEvent<? extends K, ? extends V> event) {
			if (!updating) {
				fireMapChange(Diffs.unmodifiableDiff(event.diff));
			}
		}
	};

	/**
	 * Constructs a new DetailObservableMap
	 *
	 * @param detailFactory
	 *            observable factory that creates IObservableMap instances given
	 *            the current value of master observable value
	 * @param master
	 * @param keyType
	 * @param valueType
	 *
	 */
	public DetailObservableMap(
			IObservableFactory<? super M, IObservableMap<K, V>> detailFactory,
			IObservableValue<M> master, Object keyType, Object valueType) {
		super(master.getRealm(), Collections.<K, V> emptyMap());
		Assert.isTrue(!master.isDisposed(), "Master observable is disposed"); //$NON-NLS-1$

		this.master = master;
		this.detailFactory = detailFactory;
		this.detailKeyType = keyType;
		this.detailValueType = valueType;

		master.addDisposeListener(new IDisposeListener() {
			@Override
			public void handleDispose(DisposeEvent staleEvent) {
				dispose();
			}
		});

		ObservableTracker.setIgnore(true);
		try {
			updateDetailMap();
		} finally {
			ObservableTracker.setIgnore(false);
		}
		master.addValueChangeListener(masterChangeListener);
	}

	private void updateDetailMap() {
		final M masterValue = master.getValue();
		if (detailMap != null) {
			detailMap.removeMapChangeListener(detailChangeListener);
			detailMap.dispose();
		}

		if (masterValue == null) {
			detailMap = null;
			wrappedMap = Collections.emptyMap();
		} else {
			ObservableTracker.setIgnore(true);
			try {
				detailMap = detailFactory.createObservable(masterValue);
			} finally {
				ObservableTracker.setIgnore(false);
			}
			DetailObservableHelper.warnIfDifferentRealms(getRealm(),
					detailMap.getRealm());
			wrappedMap = detailMap;

			if (detailKeyType != null) {
				Object innerKeyType = detailMap.getKeyType();

				Assert.isTrue(detailKeyType.equals(innerKeyType),
						"Cannot change key type in a nested observable map"); //$NON-NLS-1$
			}

			if (detailValueType != null) {
				Object innerValueType = detailMap.getValueType();

				Assert.isTrue(detailValueType.equals(innerValueType),
						"Cannot change value type in a nested observable map"); //$NON-NLS-1$
			}

			detailMap.addMapChangeListener(detailChangeListener);
		}
	}

	@Override
	public Object getKeyType() {
		return detailKeyType;
	}

	@Override
	public Object getValueType() {
		return detailValueType;
	}

	@Override
	public V put(final K key, final V value) {
		ObservableTracker.setIgnore(true);
		try {
			return detailMap.put(key, value);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> map) {
		ObservableTracker.setIgnore(true);
		try {
			detailMap.putAll(map);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public V remove(final Object key) {
		ObservableTracker.setIgnore(true);
		try {
			return detailMap.remove(key);
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public void clear() {
		ObservableTracker.setIgnore(true);
		try {
			detailMap.clear();
		} finally {
			ObservableTracker.setIgnore(false);
		}
	}

	@Override
	public synchronized void dispose() {
		if (master != null) {
			master.removeValueChangeListener(masterChangeListener);
			master = null;
			masterChangeListener = null;
		}
		detailFactory = null;
		if (detailMap != null) {
			detailMap.removeMapChangeListener(detailChangeListener);
			detailMap.dispose();
			detailMap = null;
		}
		detailChangeListener = null;
		super.dispose();
	}

	@Override
	public Object getObserved() {
		if (detailMap instanceof IObserving) {
			return ((IObserving) detailMap).getObserved();
		}
		return null;
	}
}
