/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 262269, 266754, 265561, 262287, 268688
 *     Ovidio Mallo - bug 299619
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property.value;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.IPropertyObservable;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.SimplePropertyEvent;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.identity.IdentitySet;
import org.eclipse.core.internal.databinding.property.Util;

/**
 * @param <S>
 *            type of the source object
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 * @since 1.2
 */
public class SetSimpleValueObservableMap<S, K extends S, V> extends ComputedObservableMap<K, V>
		implements IPropertyObservable<SimpleValueProperty<S, V>> {
	private SimpleValueProperty<S, V> detailProperty;

	private INativePropertyListener<S> listener;

	private Map<K, V> cachedValues;
	private Set<K> staleKeys;

	private boolean updating;

	/**
	 * @param keySet
	 * @param valueProperty
	 */
	public SetSimpleValueObservableMap(IObservableSet<K> keySet,
			SimpleValueProperty<S, V> valueProperty) {
		super(keySet, valueProperty.getValueType());
		this.detailProperty = valueProperty;
	}

	@Override
	protected void firstListenerAdded() {
		if (listener == null) {
			listener = detailProperty.adaptListener(new ISimplePropertyListener<S, ValueDiff<? extends V>>() {
				@Override
				public void handleEvent(final SimplePropertyEvent<S, ValueDiff<? extends V>> event) {
					if (!isDisposed() && !updating) {
						getRealm().exec(new Runnable() {
							@Override
							public void run() {
								@SuppressWarnings("unchecked")
								K source = (K) event.getSource();
								if (event.type == SimplePropertyEvent.CHANGE) {
									notifyIfChanged(source);
								} else if (event.type == SimplePropertyEvent.STALE) {
									boolean wasStale = !staleKeys.isEmpty();
									staleKeys.add(source);
									if (!wasStale)
										fireStale();
								}
							}
						});
					}
				}
			});
		}
		cachedValues = new IdentityMap<>();
		staleKeys = new IdentitySet<>();
		super.firstListenerAdded();
	}

	@Override
	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		cachedValues.clear();
		cachedValues = null;
		staleKeys.clear();
		staleKeys = null;
	}

	@Override
	protected void hookListener(K addedKey) {
		if (cachedValues != null) {
			cachedValues.put(addedKey, detailProperty.getValue(addedKey));
			if (listener != null)
				listener.addTo(addedKey);
		}
	}

	@Override
	protected void unhookListener(K removedKey) {
		if (cachedValues != null) {
			if (listener != null)
				listener.removeFrom(removedKey);
			cachedValues.remove(removedKey);
			staleKeys.remove(removedKey);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected V doGet(Object key) {
		// NOTE/TODO: This is unsafe and will cause ClassCastExceptions
		// if this map is queried with keys that are not of type S
		return detailProperty.getValue((S) key);
	}

	@Override
	protected V doPut(K key, V value) {
		V oldValue = detailProperty.getValue(key);

		updating = true;
		try {
			detailProperty.setValue(key, value);
		} finally {
			updating = false;
		}

		notifyIfChanged(key);

		return oldValue;
	}

	private void notifyIfChanged(K key) {
		if (cachedValues != null) {
			V oldValue = cachedValues.get(key);
			V newValue = detailProperty.getValue(key);
			if (!Util.equals(oldValue, newValue) || staleKeys.contains(key)) {
				cachedValues.put(key, newValue);
				staleKeys.remove(key);
				fireMapChange(Diffs.createMapDiffSingleChange(key, oldValue,
						newValue));
			}
		}
	}

	@Override
	public Object getObserved() {
		return keySet();
	}

	@Override
	public SimpleValueProperty<S, V> getProperty() {
		return detailProperty;
	}

	@Override
	public boolean isStale() {
		return super.isStale() || staleKeys != null && !staleKeys.isEmpty();
	}

	@Override
	public synchronized void dispose() {
		if (cachedValues != null) {
			cachedValues.clear();
			cachedValues = null;
		}

		listener = null;
		detailProperty = null;
		cachedValues = null;
		staleKeys = null;

		super.dispose();
	}
}
