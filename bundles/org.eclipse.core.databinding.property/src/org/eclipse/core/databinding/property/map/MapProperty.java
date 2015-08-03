/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bug 195222
 *     Ovidio Mallo - bug 331348
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property.map;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
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
 * @param <S>
 *            type of the source object
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 * @since 1.2
 */
public abstract class MapProperty<S, K, V> implements IMapProperty<S, K, V> {

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
	@Override
	public Map<K, V> getMap(S source) {
		if (source == null) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(doGetMap(source));
	}

	/**
	 * Returns a Map with the current contents of the source's map property
	 *
	 * @param source
	 *            the property source
	 * @return a Map with the current contents of the source's map property
	 * @since 1.6
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected Map<K, V> doGetMap(S source) {
		IObservableMap<K, V> observable = observe(source);
		try {
			return new IdentityMap<>(observable);
		} finally {
			observable.dispose();
		}
	}

	/**
	 * @since 1.3
	 */
	@Override
	public final void setMap(S source, Map<K, V> map) {
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
	 * @since 1.6
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected void doSetMap(S source, Map<K, V> map) {
		MapDiff<K, V> diff = Diffs.computeMapDiff(doGetMap(source), map);
		doUpdateMap(source, diff);
	}

	/**
	 * @since 1.3
	 */
	@Override
	public final void updateMap(S source, MapDiff<K, V> diff) {
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
	 * @since 1.6
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected void doUpdateMap(S source, MapDiff<K, V> diff) {
		IObservableMap<K, V> observable = observe(source);
		try {
			diff.applyTo(observable);
		} finally {
			observable.dispose();
		}
	}

	@Override
	public IObservableMap<K, V> observe(S source) {
		return observe(Realm.getDefault(), source);
	}

	@Override
	public IObservableFactory<S, IObservableMap<K, V>> mapFactory() {
		return new IObservableFactory<S, IObservableMap<K, V>>() {
			@Override
			public IObservableMap<K, V> createObservable(S target) {
				return observe(target);
			}
		};
	}

	@Override
	public IObservableFactory<S, IObservableMap<K, V>> mapFactory(final Realm realm) {
		return new IObservableFactory<S, IObservableMap<K, V>>() {
			@Override
			public IObservableMap<K, V> createObservable(S target) {
				return observe(realm, target);
			}
		};
	}

	@Override
	public <U extends S> IObservableMap<K, V> observeDetail(IObservableValue<U> master) {
		return MasterDetailObservables.detailMap(master, mapFactory(master.getRealm()), getKeyType(), getValueType());
	}

	@Override
	public final <T> IMapProperty<S, K, T> values(IValueProperty<? super V, T> detailValues) {
		return new MapPropertyDetailValuesMap<S, K, V, T>(this, detailValues);
	}
}