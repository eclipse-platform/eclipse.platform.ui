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

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.databinding.property.value.ValueProperty;

/**
 * @param <S>
 *            type of the source object
 * @param <M>
 *            type of the property of the source object this type being the type
 *            that has the detail property as a property
 * @param <T>
 *            type of this value property, being the same as the type of the
 *            value of the detail property
 * @since 1.2
 *
 */
public class ValuePropertyDetailValue<S, M, T> extends ValueProperty<S, T>implements IValueProperty<S, T> {
	private IValueProperty<S, M> masterProperty;
	private IValueProperty<? super M, T> detailProperty;

	/**
	 * @param masterProperty
	 * @param detailProperty
	 */
	public ValuePropertyDetailValue(IValueProperty<S, M> masterProperty, IValueProperty<? super M, T> detailProperty) {
		this.masterProperty = masterProperty;
		this.detailProperty = detailProperty;
	}

	@Override
	public Object getValueType() {
		return detailProperty.getValueType();
	}

	@Override
	protected T doGetValue(S source) {
		M masterValue = masterProperty.getValue(source);
		return detailProperty.getValue(masterValue);
	}

	@Override
	protected void doSetValue(S source, T value) {
		M masterValue = masterProperty.getValue(source);
		detailProperty.setValue(masterValue, value);
	}

	@Override
	public IObservableValue<T> observe(Realm realm, S source) {
		IObservableValue<M> masterValue;

		ObservableTracker.setIgnore(true);
		try {
			masterValue = masterProperty.observe(realm, source);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableValue<T> detailValue = detailProperty.observeDetail(masterValue);
		PropertyObservableUtil.cascadeDispose(detailValue, masterValue);
		return detailValue;
	}

	@Override
	public <V extends S> IObservableValue<T> observeDetail(IObservableValue<V> master) {
		IObservableValue<M> masterValue;

		ObservableTracker.setIgnore(true);
		try {
			masterValue = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableValue<T> detailValue = detailProperty.observeDetail(masterValue);
		PropertyObservableUtil.cascadeDispose(detailValue, masterValue);
		return detailValue;
	}

	@Override
	public <V extends S> IObservableList<T> observeDetail(IObservableList<V> master) {
		IObservableList<M> masterList;

		ObservableTracker.setIgnore(true);
		try {
			masterList = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableList<T> detailList = detailProperty.observeDetail(masterList);
		PropertyObservableUtil.cascadeDispose(detailList, masterList);
		return detailList;
	}

	@Override
	public <V extends S> IObservableMap<V, T> observeDetail(IObservableSet<V> master) {
		IObservableMap<V, M> masterMap;

		ObservableTracker.setIgnore(true);
		try {
			masterMap = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableMap<V, T> detailMap = detailProperty.observeDetail(masterMap);
		PropertyObservableUtil.cascadeDispose(detailMap, masterMap);
		return detailMap;
	}

	@Override
	public <K, V extends S> IObservableMap<K, T> observeDetail(IObservableMap<K, V> master) {
		IObservableMap<K, M> masterMap;

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
