/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 278550
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property;

import java.util.Iterator;
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
 * @since 3.3
 * 
 */
public class MapPropertyDetailValuesMap extends MapProperty {
	private final IMapProperty masterProperty;
	private final IValueProperty detailProperty;

	/**
	 * @param masterProperty
	 * @param detailProperty
	 */
	public MapPropertyDetailValuesMap(IMapProperty masterProperty,
			IValueProperty detailProperty) {
		this.masterProperty = masterProperty;
		this.detailProperty = detailProperty;
	}

	public Object getKeyType() {
		return masterProperty.getKeyType();
	}

	public Object getValueType() {
		return detailProperty.getValueType();
	}

	protected Map doGetMap(Object source) {
		Map masterMap = masterProperty.getMap(source);
		Map detailMap = new IdentityMap();
		for (Iterator it = masterMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			detailMap.put(entry.getKey(), detailProperty.getValue(entry
					.getValue()));
		}
		return detailMap;
	}

	protected void doUpdateMap(Object source, MapDiff diff) {
		if (!diff.getAddedKeys().isEmpty())
			throw new UnsupportedOperationException(toString()
					+ " does not support entry additions"); //$NON-NLS-1$
		if (!diff.getRemovedKeys().isEmpty())
			throw new UnsupportedOperationException(toString()
					+ " does not support entry removals"); //$NON-NLS-1$
		Map masterMap = masterProperty.getMap(source);
		for (Iterator it = diff.getChangedKeys().iterator(); it.hasNext();) {
			Object key = it.next();
			Object masterValue = masterMap.get(key);
			detailProperty.setValue(masterValue, diff.getNewValue(key));
		}
	}

	public IObservableMap observe(Realm realm, Object source) {
		IObservableMap masterMap;

		ObservableTracker.setIgnore(true);
		try {
			masterMap = masterProperty.observe(realm, source);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableMap detailMap = detailProperty.observeDetail(masterMap);
		PropertyObservableUtil.cascadeDispose(detailMap, masterMap);
		return detailMap;
	}

	public IObservableMap observeDetail(IObservableValue master) {
		IObservableMap masterMap;

		ObservableTracker.setIgnore(true);
		try {
			masterMap = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableMap detailMap = detailProperty.observeDetail(masterMap);
		PropertyObservableUtil.cascadeDispose(detailMap, masterMap);
		return detailMap;
	}

	public String toString() {
		return masterProperty + " => " + detailProperty; //$NON-NLS-1$
	}
}
