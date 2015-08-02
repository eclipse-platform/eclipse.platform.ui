/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation
 *     Matthew Hall - bugs 195222, 247997, 265561
 *     Ovidio Mallo - bug 301774
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.databinding.property.map;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.internal.databinding.property.map.SimplePropertyObservableMap;

/**
 * Simplified abstract implementation of IMapProperty. This class takes care of
 * most of the functional requirements for an IMapProperty implementation,
 * leaving only the property-specific details to subclasses.
 * <p>
 * Subclasses must implement these methods:
 * <ul>
 * <li> {@link #getKeyType()}
 * <li> {@link #getValueType()}
 * <li> {@link #doGetMap(Object)}
 * <li> {@link #doSetMap(Object, Map, MapDiff)}
 * <li> {@link #adaptListener(ISimplePropertyListener)}
 * </ul>
 * <p>
 * In addition, we recommended overriding {@link #toString()} to return a
 * description suitable for debugging purposes.
 *
 * @param <S>
 *            type of the source object
 * @param <K>
 *            type of the keys to the map
 * @param <V>
 *            type of the values in the map
 * @since 1.2
 */
public abstract class SimpleMapProperty<S, K, V> extends MapProperty<S, K, V> {
	@Override
	public IObservableMap<K, V> observe(Realm realm, S source) {
		return new SimplePropertyObservableMap<S, K, V>(realm, source, this);
	}

	// Accessors

	@Override
	protected abstract Map<K, V> doGetMap(S source);

	// Mutators

	/**
	 * Updates the property on the source with the specified change.
	 *
	 * @param source
	 *            the property source
	 * @param map
	 *            the new map
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 1.6
	 */
	public final void setMap(S source, Map<K, V> map, MapDiff<K, V> diff) {
		if (source != null && !diff.isEmpty())
			doSetMap(source, map, diff);
	}

	/**
	 * Updates the property on the source with the specified change.
	 *
	 * @param source
	 *            the property source
	 * @param map
	 *            the new map
	 * @param diff
	 *            a diff describing the change
	 * @noreference This method is not intended to be referenced by clients.
	 */
	protected abstract void doSetMap(S source, Map<K, V> map, MapDiff<K, V> diff);

	@Override
	protected void doSetMap(S source, Map<K, V> map) {
		MapDiff<K, V> diff = Diffs.computeLazyMapDiff(doGetMap(source), map);
		doSetMap(source, map, diff);
	}

	@Override
	protected void doUpdateMap(S source, MapDiff<K, V> diff) {
		Map<K, V> map = new HashMap<>(doGetMap(source));
		diff.applyTo(map);
		doSetMap(source, map, diff);
	}

	// Listeners

	/**
	 * Returns a listener capable of adding or removing itself as a listener on
	 * a source object using the the source's "native" listener API. Events
	 * received from the source objects are parlayed to the specified listener
	 * argument.
	 * <p>
	 * This method returns null if the source object has no listener APIs for
	 * this property.
	 *
	 * @param listener
	 *            the property listener to receive events
	 * @return a native listener which parlays property change events to the
	 *         specified listener, or null if the source object has no listener
	 *         APIs for this property.
	 * @noreference This method is not intended to be referenced by clients.
	 * @since 1.5
	 */
	public abstract INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, MapDiff<K, V>> listener);
}
