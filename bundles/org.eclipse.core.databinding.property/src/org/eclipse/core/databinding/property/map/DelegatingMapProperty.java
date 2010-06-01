/*******************************************************************************
 * Copyright (c) 2008, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 247997)
 *     Matthew Hall - bug 264306
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
 * @since 1.2
 * 
 */
public abstract class DelegatingMapProperty extends MapProperty {
	private final Object keyType;
	private final Object valueType;
	private final IMapProperty nullProperty = new NullMapProperty();

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
	public final IMapProperty getDelegate(Object source) {
		if (source == null)
			return nullProperty;
		IMapProperty delegate = doGetDelegate(source);
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
	protected abstract IMapProperty doGetDelegate(Object source);

	public Object getKeyType() {
		return keyType;
	}

	public Object getValueType() {
		return valueType;
	}

	protected Map doGetMap(Object source) {
		return getDelegate(source).getMap(source);
	}

	protected void doSetMap(Object source, Map map) {
		getDelegate(source).setMap(source, map);
	}

	protected void doUpdateMap(Object source, MapDiff diff) {
		getDelegate(source).updateMap(source, diff);
	}

	public IObservableMap observe(Object source) {
		return getDelegate(source).observe(source);
	}

	public IObservableMap observe(Realm realm, Object source) {
		return getDelegate(source).observe(realm, source);
	}

	private class NullMapProperty extends SimpleMapProperty {
		protected Map doGetMap(Object source) {
			return Collections.EMPTY_MAP;
		}

		protected void doSetMap(Object source, Map map, MapDiff diff) {
		}

		protected void doSetMap(Object source, Map map) {
		}

		protected void doUpdateMap(Object source, MapDiff diff) {
		}

		public INativePropertyListener adaptListener(
				ISimplePropertyListener listener) {
			return null;
		}

		public Object getKeyType() {
			return keyType;
		}

		public Object getValueType() {
			return valueType;
		}
	}
}
