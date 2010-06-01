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

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.property.value.ValueProperty;

/**
 * @since 1.2
 * 
 */
public class ValuePropertyDetailValue extends ValueProperty implements
		IValueProperty {
	private IValueProperty masterProperty;
	private IValueProperty detailProperty;

	/**
	 * @param masterProperty
	 * @param detailProperty
	 */
	public ValuePropertyDetailValue(IValueProperty masterProperty,
			IValueProperty detailProperty) {
		this.masterProperty = masterProperty;
		this.detailProperty = detailProperty;
	}

	public Object getValueType() {
		return detailProperty.getValueType();
	}

	protected Object doGetValue(Object source) {
		Object masterValue = masterProperty.getValue(source);
		return detailProperty.getValue(masterValue);
	}

	protected void doSetValue(Object source, Object value) {
		Object masterValue = masterProperty.getValue(source);
		detailProperty.setValue(masterValue, value);
	}

	public IObservableValue observe(Realm realm, Object source) {
		IObservableValue masterValue;

		ObservableTracker.setIgnore(true);
		try {
			masterValue = masterProperty.observe(realm, source);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableValue detailValue = detailProperty
				.observeDetail(masterValue);
		PropertyObservableUtil.cascadeDispose(detailValue, masterValue);
		return detailValue;
	}

	public IObservableValue observeDetail(IObservableValue master) {
		IObservableValue masterValue;

		ObservableTracker.setIgnore(true);
		try {
			masterValue = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableValue detailValue = detailProperty
				.observeDetail(masterValue);
		PropertyObservableUtil.cascadeDispose(detailValue, masterValue);
		return detailValue;
	}

	public IObservableList observeDetail(IObservableList master) {
		IObservableList masterList;

		ObservableTracker.setIgnore(true);
		try {
			masterList = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableList detailList = detailProperty.observeDetail(masterList);
		PropertyObservableUtil.cascadeDispose(detailList, masterList);
		return detailList;
	}

	public IObservableMap observeDetail(IObservableSet master) {
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

	public IObservableMap observeDetail(IObservableMap master) {
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
