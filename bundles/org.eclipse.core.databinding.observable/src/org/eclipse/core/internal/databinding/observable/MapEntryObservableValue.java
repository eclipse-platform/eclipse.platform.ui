/*******************************************************************************
 * Copyright (c) 2008, 2015 Marko Topolnik and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marko Topolnik - initial API and implementation (bug 184830)
 *     Matthew Hall - bug 184830
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * An {@link IObservableValue} that tracks the value of an entry in an
 * {@link IObservableMap}, identified by the entry's key.
 *
 * @param <K>
 *            the type of the keys in this map
 * @param <V>
 *            the type of the values in this map
 *
 * @since 1.1
 */
public class MapEntryObservableValue<K, V> extends AbstractObservableValue<V> {
	private IObservableMap<K, V> map;
	private K key;
	private Object valueType;

	private IMapChangeListener<K, V> changeListener = new IMapChangeListener<K, V>() {
		@Override
		public void handleMapChange(final MapChangeEvent<? extends K, ? extends V> event) {
			if (event.diff.getAddedKeys().contains(key)) {
				final V newValue = event.diff.getNewValue(key);
				if (newValue != null) {
					fireValueChange(Diffs.createValueDiff(null, newValue));
				}
			} else if (event.diff.getChangedKeys().contains(key)) {
				fireValueChange(Diffs.createValueDiff(
						event.diff.getOldValue(key),
						event.diff.getNewValue(key)));
			} else if (event.diff.getRemovedKeys().contains(key)) {
				final V oldValue = event.diff.getOldValue(key);
				if (oldValue != null) {
					fireValueChange(Diffs.createValueDiff(oldValue, null));
				}
			}
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		@Override
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	/**
	 * Creates a map entry observable.
	 *
	 * @param map
	 *            the observable map whose entry will be tracked
	 * @param key
	 *            the key identifying the entry whose value will be tracked
	 * @param valueType
	 *            the type of the value
	 */
	public MapEntryObservableValue(IObservableMap<K, V> map, K key,
			Object valueType) {
		super(map.getRealm());
		this.map = map;
		this.key = key;
		this.valueType = valueType;

		map.addMapChangeListener(changeListener);
		map.addStaleListener(staleListener);
	}

	@Override
	public Object getValueType() {
		return this.valueType;
	}

	@Override
	public boolean isStale() {
		ObservableTracker.getterCalled(this);
		return map.isStale();
	}

	@Override
	public synchronized void dispose() {
		if (map != null) {
			map.removeMapChangeListener(changeListener);
			map.removeStaleListener(staleListener);
			map = null;
			changeListener = null;
			staleListener = null;
		}
		super.dispose();
	}

	@Override
	protected V doGetValue() {
		return this.map.get(this.key);
	}

	@Override
	protected void doSetValue(V value) {
		this.map.put(this.key, value);
	}
}