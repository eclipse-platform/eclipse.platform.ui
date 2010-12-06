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
 *     Ovidio Mallo - bug 331348
 ******************************************************************************/

package org.eclipse.core.databinding.property.map;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.identity.IdentityMap;
import org.eclipse.core.internal.databinding.property.MapPropertyDetailValuesMap;

/**
 * Abstract implementation of IMapProperty
 * 
 * @since 1.2
 */
public abstract class MapProperty implements IMapProperty {

	/**
	 * By default, this method returns <code>Collections.EMPTY_MAP</code> in
	 * case the source object is <code>null</code>. Otherwise, this method
	 * delegates to {@link #doGetMap(Object)}.
	 * 
	 * <p>
	 * Clients may override this method if they e.g. want to return a specific
	 * default map in case the source object is <code>null</code>.
	 * </p>
	 * 
	 * @see #doGetMap(Object)
	 * 
	 * @since 1.3
	 */
	public Map getMap(Object source) {
		if (source == null) {
			return Collections.EMPTY_MAP;
		}
		return Collections.unmodifiableMap(doGetMap(source));
	}

	/**
	 * Returns a Map with the current contents of the source's map property
	 * 
	 * @param source
	 *            the property source
	 * @return a Map with the current contents of the source's map property
	 * @since 1.3
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected Map doGetMap(Object source) {
		IObservableMap observable = observe(source);
		try {
			return new IdentityMap(observable);
		} finally {
			observable.dispose();
		}
	}

	/**
	 * @since 1.3
	 */
	public final void setMap(Object source, Map map) {
		if (source != null) {
			doSetMap(source, map);
		}
	}

	/**
	 * Updates the property on the source with the specified change.
	 * 
	 * @param source
	 *            the property source
	 * @param map
	 *            the new map
	 * @since 1.3
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected void doSetMap(Object source, Map map) {
		MapDiff diff = Diffs.computeMapDiff(doGetMap(source), map);
		doUpdateMap(source, diff);
	}

	/**
	 * @since 1.3
	 */
	public final void updateMap(Object source, MapDiff diff) {
		if (source != null) {
			doUpdateMap(source, diff);
		}
	}

	/**
	 * Updates the property on the source with the specified change.
	 * 
	 * @param source
	 *            the property source
	 * @param diff
	 *            a diff describing the change
	 * @since 1.3
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected void doUpdateMap(Object source, MapDiff diff) {
		IObservableMap observable = observe(source);
		try {
			diff.applyTo(observable);
		} finally {
			observable.dispose();
		}
	}

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
		return MasterDetailObservables.detailMap(master,
				mapFactory(master.getRealm()), getKeyType(), getValueType());
	}

	public final IMapProperty values(IValueProperty detailValues) {
		return new MapPropertyDetailValuesMap(this, detailValues);
	}
}