/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
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

	public IObservableValue observe(final Realm realm, final Object source) {
		final IObservableValue[] masterValue = new IObservableValue[1];

		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				masterValue[0] = masterProperty.observe(realm, source);
			}
		});

		IObservableValue detailValue = detailProperty
				.observeDetail(masterValue[0]);
		PropertyObservableUtil.cascadeDispose(detailValue, masterValue[0]);
		return detailValue;
	}

	public IObservableValue observeDetail(final IObservableValue master) {
		final IObservableValue[] masterValue = new IObservableValue[1];

		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				masterValue[0] = masterProperty.observeDetail(master);
			}
		});

		IObservableValue detailValue = detailProperty
				.observeDetail(masterValue[0]);
		PropertyObservableUtil.cascadeDispose(detailValue, masterValue[0]);
		return detailValue;
	}

	public IObservableList observeDetail(final IObservableList master) {
		final IObservableList[] masterList = new IObservableList[1];

		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				masterList[0] = masterProperty.observeDetail(master);
			}
		});

		IObservableList detailList = detailProperty
				.observeDetail(masterList[0]);
		PropertyObservableUtil.cascadeDispose(detailList, masterList[0]);
		return detailList;
	}

	public IObservableMap observeDetail(final IObservableSet master) {
		final IObservableMap[] masterMap = new IObservableMap[1];

		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				masterMap[0] = masterProperty.observeDetail(master);
			}
		});

		IObservableMap detailMap = detailProperty.observeDetail(masterMap[0]);
		PropertyObservableUtil.cascadeDispose(detailMap, masterMap[0]);
		return detailMap;
	}

	public IObservableMap observeDetail(final IObservableMap master) {
		final IObservableMap[] masterMap = new IObservableMap[1];

		ObservableTracker.runAndIgnore(new Runnable() {
			public void run() {
				masterMap[0] = masterProperty.observeDetail(master);
			}
		});

		IObservableMap detailMap = detailProperty.observeDetail(masterMap[0]);
		PropertyObservableUtil.cascadeDispose(detailMap, masterMap[0]);
		return detailMap;
	}

	public String toString() {
		return masterProperty + " => " + detailProperty; //$NON-NLS-1$
	}
}
