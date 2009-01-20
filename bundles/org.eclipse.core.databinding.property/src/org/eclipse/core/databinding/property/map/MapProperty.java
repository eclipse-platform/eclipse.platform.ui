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

package org.eclipse.core.databinding.property.map;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.property.MapPropertyDetailValuesMap;

/**
 * Abstract implementation of IMapProperty
 * 
 * @since 1.2
 */
public abstract class MapProperty implements IMapProperty {
	public IObservableMap observe(Object source) {
		return observe(Realm.getDefault(), source);
	}

	public IObservableFactory mapFactory() {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observe(target);
			}
		};
	}

	public IObservableFactory mapFactory(final Realm realm) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observe(realm, target);
			}
		};
	}

	public IObservableMap observeDetail(IObservableValue master) {
		return MasterDetailObservables.detailMap(master, mapFactory(master
				.getRealm()), getKeyType(), getValueType());
	}

	public final IMapProperty values(IValueProperty detailValues) {
		return new MapPropertyDetailValuesMap(this, detailValues);
	}
}