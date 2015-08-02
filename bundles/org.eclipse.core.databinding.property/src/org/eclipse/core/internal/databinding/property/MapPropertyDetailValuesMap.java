/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 278550
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property;

import java.util.Map;

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.map.MapProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;

/**
 * @param <S>
 *            type of the source object
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 * @param <T>
 *            type of the elements in the list, being the type of the value of
 *            the detail property
 * @since 3.3
 *
 */
public class MapPropertyDetailValuesMap<S, K, V, T> extends MapProperty<S, K, T> {
	private final IMapProperty<S, K, V> masterProperty;
	private final IValueProperty<? super V, T> detailProperty;

	/**
	 * @param masterProperty
	 * @param detailProperty
	 */
	public MapPropertyDetailValuesMap(IMapProperty<S, K, V> masterProperty,
			IValueProperty<? super V, T> detailProperty) {
		this.masterProperty = masterProperty;
		this.detailProperty = detailProperty;
	}

	@Override
	public Object getKeyType() {
		return masterProperty.getKeyType();
	}

	@Override
	public Object getValueType() {
		return detailProperty.getValueType();
	}

	@Override
	protected Map<K, T> doGetMap(S source) {
		Map<K, V> masterMap = masterProperty.getMap(source);
		Map<K, T> detailMap = new IdentityMap<K, T>();
		for (Map.Entry<K, V> entry : masterMap.entrySet()) {
			detailMap.put(entry.getKey(), detailProperty.getValue(entry.getValue()));
		}
		return detailMap;
	}

	@Override
	protected void doUpdateMap(S source, MapDiff<K, T> diff) {
		if (!diff.getAddedKeys().isEmpty())
			throw new UnsupportedOperationException(toString() + " does not support entry additions"); //$NON-NLS-1$
		if (!diff.getRemovedKeys().isEmpty())
			throw new UnsupportedOperationException(toString() + " does not support entry removals"); //$NON-NLS-1$
		Map<K, V> masterMap = masterProperty.getMap(source);
		for (K key : diff.getChangedKeys()) {
			V masterValue = masterMap.get(key);
			detailProperty.setValue(masterValue, diff.getNewValue(key));
		}
	}

	@Override
	public IObservableMap<K, T> observe(Realm realm, S source) {
		IObservableMap<K, V> masterMap;

		ObservableTracker.setIgnore(true);
		try {
			masterMap = masterProperty.observe(realm, source);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableMap<K, T> detailMap = detailProperty.observeDetail(masterMap);
		PropertyObservableUtil.cascadeDispose(detailMap, masterMap);
		return detailMap;
	}

	@Override
	public <U extends S> IObservableMap<K, T> observeDetail(IObservableValue<U> master) {
		IObservableMap<K, V> masterMap;

		ObservableTracker.setIgnore(true);
		try {
			masterMap = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableMap<K, T> detailMap = detailProperty.observeDetail(masterMap);
		PropertyObservableUtil.cascadeDispose(detailMap, masterMap);
		return detailMap;
	}

	@Override
	public String toString() {
		return masterProperty + " => " + detailProperty; //$NON-NLS-1$
	}
}
