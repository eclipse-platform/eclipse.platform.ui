/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 ******************************************************************************/

package org.eclipse.core.databinding.property.value;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.internal.databinding.property.ValuePropertyDetailList;
import org.eclipse.core.internal.databinding.property.ValuePropertyDetailMap;
import org.eclipse.core.internal.databinding.property.ValuePropertyDetailSet;
import org.eclipse.core.internal.databinding.property.ValuePropertyDetailValue;

/**
 * Abstract implementation of IValueProperty
 * 
 * @since 1.2
 */
public abstract class ValueProperty implements IValueProperty {
	public IObservableValue observe(Object source) {
		return observe(Realm.getDefault(), source);
	}

	public IObservableFactory valueFactory() {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observe(target);
			}
		};
	}

	public IObservableFactory valueFactory(final Realm realm) {
		return new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return observe(realm, target);
			}
		};
	}

	public IObservableValue observeDetail(IObservableValue master) {
		return MasterDetailObservables.detailValue(master, valueFactory(master
				.getRealm()), getValueType());
	}

	public final IValueProperty value(IValueProperty detailValue) {
		return new ValuePropertyDetailValue(this, detailValue);
	}

	public final IListProperty list(IListProperty detailList) {
		return new ValuePropertyDetailList(this, detailList);
	}

	public final ISetProperty set(ISetProperty detailSet) {
		return new ValuePropertyDetailSet(this, detailSet);
	}

	public final IMapProperty map(IMapProperty detailMap) {
		return new ValuePropertyDetailMap(this, detailMap);
	}
}
