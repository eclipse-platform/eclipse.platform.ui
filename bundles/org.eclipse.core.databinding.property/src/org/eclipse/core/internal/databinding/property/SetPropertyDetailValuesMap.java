/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 ******************************************************************************/

package org.eclipse.core.internal.databinding.property;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.map.MapProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;

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

	public IObservableMap observe(Realm realm, Object source) {
		IObservableSet masterSet = masterProperty.observe(realm, source);
		IObservableMap detailMap = detailProperty.observeDetail(masterSet);
		PropertyObservableUtil.cascadeDispose(detailMap, masterSet);
		return detailMap;
	}

	public IObservableMap observeDetail(IObservableValue master) {
		IObservableSet masterSet = masterProperty.observeDetail(master);
		IObservableMap detailMap = detailProperty.observeDetail(masterSet);
		PropertyObservableUtil.cascadeDispose(detailMap, masterSet);
		return detailMap;
	}

	public String toString() {
		return masterProperty + " => " + detailProperty; //$NON-NLS-1$
	}
}
