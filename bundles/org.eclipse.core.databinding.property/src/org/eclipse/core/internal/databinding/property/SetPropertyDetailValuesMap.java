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
import java.util.Set;

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.map.MapProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;

/**
 * @since 3.3
 * 
 */
public class SetPropertyDetailValuesMap extends MapProperty {
	private final ISetProperty masterProperty;
	private final IValueProperty detailProperty;

	/**
	 * @param masterProperty
	 * @param detailProperty
	 */
	public SetPropertyDetailValuesMap(ISetProperty masterProperty,
			IValueProperty detailProperty) {
		this.masterProperty = masterProperty;
		this.detailProperty = detailProperty;
	}

	public Object getKeyType() {
		return masterProperty.getElementType();
	}

	public Object getValueType() {
		return detailProperty.getValueType();
	}

	protected Map doGetMap(Object source) {
		Set set = masterProperty.getSet(source);
		Map map = new IdentityMap();
		for (Iterator it = set.iterator(); it.hasNext();) {
			Object key = it.next();
			map.put(key, detailProperty.getValue(key));
		}
		return map;
	}

	protected void doUpdateMap(Object source, MapDiff diff) {
		if (!diff.getAddedKeys().isEmpty())
			throw new UnsupportedOperationException(toString()
					+ " does not support entry additions"); //$NON-NLS-1$
		if (!diff.getRemovedKeys().isEmpty())
			throw new UnsupportedOperationException(toString()
					+ " does not support entry removals"); //$NON-NLS-1$
		for (Iterator it = diff.getChangedKeys().iterator(); it.hasNext();) {
			Object key = it.next();
			Object newValue = diff.getNewValue(key);
			detailProperty.setValue(key, newValue);
		}
	}

	public IObservableMap observe(Realm realm, Object source) {
		IObservableSet masterSet;

		ObservableTracker.setIgnore(true);
		try {
			masterSet = masterProperty.observe(realm, source);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableMap detailMap = detailProperty.observeDetail(masterSet);
		PropertyObservableUtil.cascadeDispose(detailMap, masterSet);
		return detailMap;
	}

	public IObservableMap observeDetail(IObservableValue master) {
		IObservableSet masterSet;

		ObservableTracker.setIgnore(true);
		try {
			masterSet = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableMap detailMap = detailProperty.observeDetail(masterSet);
		PropertyObservableUtil.cascadeDispose(detailMap, masterSet);
		return detailMap;
	}

	public String toString() {
		return masterProperty + " => " + detailProperty; //$NON-NLS-1$
	}
}
