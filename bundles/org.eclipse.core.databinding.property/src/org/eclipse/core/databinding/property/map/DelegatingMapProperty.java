/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 247997)
 *     Matthew Hall - bug 264306
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property.map;

import java.util.Collections;
import java.util.Map;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;

/**
 * @param <S>
 *            type of the source object
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 * @since 1.2
 *
 */
public abstract class DelegatingMapProperty<S, K, V> extends MapProperty<S, K, V> {
	private final Object keyType;
	private final Object valueType;
	private final IMapProperty<S, K, V> nullProperty = new NullMapProperty();

	protected DelegatingMapProperty() {
		this(null, null);
	}

	protected DelegatingMapProperty(Object keyType, Object valueType) {
		this.keyType = keyType;
		this.valueType = valueType;
	}

	/**
	 * Returns the property to delegate to for the specified source object.
	 * Repeated calls to this method with the same source object returns the
	 * same delegate instance.
	 *
	 * @param source
	 *            the property source (may be null)
	 * @return the property to delegate to for the specified source object.
	 */
	public final IMapProperty<S, K, V> getDelegate(S source) {
		if (source == null)
			return nullProperty;
		IMapProperty<S, K, V> delegate = doGetDelegate(source);
		if (delegate == null)
			delegate = nullProperty;
		return delegate;
	}

	/**
	 * Returns the property to delegate to for the specified source object.
	 * Implementers must ensure that repeated calls to this method with the same
	 * source object returns the same delegate instance.
	 *
	 * @param source
	 *            the property source
	 * @return the property to delegate to for the specified source object.
	 */
	protected abstract IMapProperty<S, K, V> doGetDelegate(S source);

	@Override
	public Object getKeyType() {
		return keyType;
	}

	@Override
	public Object getValueType() {
		return valueType;
	}

	@Override
	protected Map<K, V> doGetMap(S source) {
		return getDelegate(source).getMap(source);
	}

	@Override
	protected void doSetMap(S source, Map<K, V> map) {
		getDelegate(source).setMap(source, map);
	}

	@Override
	protected void doUpdateMap(S source, MapDiff<K, V> diff) {
		getDelegate(source).updateMap(source, diff);
	}

	@Override
	public IObservableMap<K, V> observe(S source) {
		return getDelegate(source).observe(source);
	}

	@Override
	public IObservableMap<K, V> observe(Realm realm, S source) {
		return getDelegate(source).observe(realm, source);
	}

	private class NullMapProperty extends SimpleMapProperty<S, K, V> {
		@Override
		protected Map<K, V> doGetMap(Object source) {
			return Collections.emptyMap();
		}

		@Override
		protected void doSetMap(S source, Map<K, V> map, MapDiff<K, V> diff) {
		}

		@Override
		protected void doSetMap(S source, Map<K, V> map) {
		}

		@Override
		protected void doUpdateMap(S source, MapDiff<K, V> diff) {
		}

		@Override
		public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, MapDiff<K, V>> listener) {
			return null;
		}

		@Override
		public Object getKeyType() {
			return keyType;
		}

		@Override
		public Object getValueType() {
			return valueType;
		}
	}
}
