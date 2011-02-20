/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 *     Ovidio Mallo - bugs 331348, 305367
 ******************************************************************************/

package org.eclipse.core.databinding.property.value;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
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

	/**
	 * By default, this method returns <code>null</code> in case the source
	 * object is itself <code>null</code>. Otherwise, this method delegates to
	 * {@link #doGetValue(Object)}.
	 * 
	 * <p>
	 * Clients may override this method if they e.g. want to return a specific
	 * default value in case the source object is <code>null</code>.
	 * </p>
	 * 
	 * @see #doGetValue(Object)
	 * 
	 * @since 1.3
	 */
	public Object getValue(Object source) {
		if (source == null) {
			return null;
		}
		return doGetValue(source);
	}

	/**
	 * Returns the value of the property on the specified source object
	 * 
	 * @param source
	 *            the property source
	 * @return the current value of the source's value property
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 1.3
	 */
	protected Object doGetValue(Object source) {
		IObservableValue observable = observe(source);
		try {
			return observable.getValue();
		} finally {
			observable.dispose();
		}
	}

	/**
	 * @since 1.3
	 */
	public final void setValue(Object source, Object value) {
		if (source != null) {
			doSetValue(source, value);
		}
	}

	/**
	 * Sets the source's value property to the specified vlaue
	 * 
	 * @param source
	 *            the property source
	 * @param value
	 *            the new value
	 * @since 1.3
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected void doSetValue(Object source, Object value) {
		IObservableValue observable = observe(source);
		try {
			observable.setValue(value);
		} finally {
			observable.dispose();
		}
	}

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
		return MasterDetailObservables.detailValue(master,
				valueFactory(master.getRealm()), getValueType());
	}

	/**
	 * @since 1.4
	 */
	public IObservableList observeDetail(IObservableList master) {
		return MasterDetailObservables.detailValues(master,
				valueFactory(master.getRealm()), getValueType());
	}

	/**
	 * @since 1.4
	 */
	public IObservableMap observeDetail(IObservableSet master) {
		return MasterDetailObservables.detailValues(master,
				valueFactory(master.getRealm()), getValueType());
	}

	/**
	 * @since 1.4
	 */
	public IObservableMap observeDetail(IObservableMap master) {
		return MasterDetailObservables.detailValues(master,
				valueFactory(master.getRealm()), getValueType());
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
