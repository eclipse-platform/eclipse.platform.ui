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

import java.util.Set;

import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.set.SetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;

/**
 * @since 3.3
 * 
 */
public class ValuePropertyDetailSet extends SetProperty {
	private IValueProperty masterProperty;
	private ISetProperty detailProperty;

	/**
	 * @param masterProperty
	 * @param detailProperty
	 */
	public ValuePropertyDetailSet(IValueProperty masterProperty,
			ISetProperty detailProperty) {
		this.masterProperty = masterProperty;
		this.detailProperty = detailProperty;
	}

	public Object getElementType() {
		return detailProperty.getElementType();
	}

	protected Set doGetSet(Object source) {
		Object masterValue = masterProperty.getValue(source);
		return detailProperty.getSet(masterValue);
	}

	protected void doSetSet(Object source, Set set) {
		Object masterValue = masterProperty.getValue(source);
		detailProperty.setSet(masterValue, set);
	}

	protected void doUpdateSet(Object source, SetDiff diff) {
		Object masterValue = masterProperty.getValue(source);
		detailProperty.updateSet(masterValue, diff);
	}

	public IObservableSet observe(Realm realm, Object source) {
		IObservableValue masterValue;

		ObservableTracker.setIgnore(true);
		try {
			masterValue = masterProperty.observe(realm, source);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableSet detailSet = detailProperty.observeDetail(masterValue);
		PropertyObservableUtil.cascadeDispose(detailSet, masterValue);
		return detailSet;
	}

	public IObservableSet observeDetail(IObservableValue master) {
		IObservableValue masterValue;

		ObservableTracker.setIgnore(true);
		try {
			masterValue = masterProperty.observeDetail(master);
		} finally {
			ObservableTracker.setIgnore(false);
		}

		IObservableSet detailSet = detailProperty.observeDetail(masterValue);
		PropertyObservableUtil.cascadeDispose(detailSet, masterValue);
		return detailSet;
	}

	public String toString() {
		return masterProperty + " => " + detailProperty; //$NON-NLS-1$
	}
}
