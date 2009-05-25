/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 194734)
 *     Matthew Hall - bugs 195222, 263868, 264954
 ******************************************************************************/

package org.eclipse.core.databinding.property;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.property.map.IMapProperty;
import org.eclipse.core.databinding.property.set.ISetProperty;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.core.internal.databinding.property.list.SelfListProperty;
import org.eclipse.core.internal.databinding.property.map.SelfMapProperty;
import org.eclipse.core.internal.databinding.property.set.SelfSetProperty;
import org.eclipse.core.internal.databinding.property.value.ObservableValueProperty;
import org.eclipse.core.internal.databinding.property.value.SelfValueProperty;

/**
 * Contains static methods to operate on or return IProperty objects.
 * 
 * @since 1.2
 */
public class Properties {
	/**
	 * Returns an array of observable maps where each map observes the
	 * corresponding value property on all elements in the given domain set, for
	 * each property in the given array.
	 * 
	 * @param domainSet
	 *            the set of elements whose properties will be observed
	 * @param properties
	 *            array of value properties to observe on each element in the
	 *            domain set.
	 * @return an array of observable maps where each map observes the
	 *         corresponding value property of the given domain set.
	 */
	public static IObservableMap[] observeEach(IObservableSet domainSet,
			IValueProperty[] properties) {
		IObservableMap[] maps = new IObservableMap[properties.length];
		for (int i = 0; i < maps.length; i++)
			maps[i] = properties[i].observeDetail(domainSet);
		return maps;
	}

	/**
	 * Returns an array of observable maps where each maps observes the
	 * corresponding value property on all elements in the given domain map's
	 * {@link Map#values() values} collection, for each property in the given
	 * array.
	 * 
	 * @param domainMap
	 *            the map of elements whose properties will be observed
	 * @param properties
	 *            array of value properties to observe on each element in the
	 *            domain map's {@link Map#values() values} collection.
	 * @return an array of observable maps where each maps observes the
	 *         corresponding value property on all elements in the given domain
	 *         map's {@link Map#values() values} collection, for each property
	 *         in the given array.
	 */
	public static IObservableMap[] observeEach(IObservableMap domainMap,
			IValueProperty[] properties) {
		IObservableMap[] maps = new IObservableMap[properties.length];
		for (int i = 0; i < maps.length; i++)
			maps[i] = properties[i].observeDetail(domainMap);
		return maps;
	}

	/**
	 * Returns a value property which takes the source object itself as the
	 * property value. This property may be used to wrap an object in an
	 * unmodifiable {@link IObservableValue}.
	 * 
	 * @param valueType
	 *            the value type of the property
	 * @return a value property which takes the source object itself as the
	 *         property value.
	 */
	public static IValueProperty selfValue(Object valueType) {
		return new SelfValueProperty(valueType);
	}

	/**
	 * Returns a list property which takes the source object (a {@link List}) as
	 * the property list. This property may be used to wrap an arbitrary List
	 * instance in an {@link IObservableList}.
	 * 
	 * @param elementType
	 *            the element type of the property
	 * @return a list property which takes the source object (a {@link List}) as
	 *         the property list.
	 */
	public static IListProperty selfList(Object elementType) {
		return new SelfListProperty(elementType);
	}

	/**
	 * Returns a set property which takes the source object (a {@link Set}) as
	 * the property set. This property may be used to wrap an arbitrary Set
	 * instance in an {@link IObservableSet}.
	 * 
	 * @param elementType
	 *            the element type of the property
	 * @return a set property which takes the source object (a {@link Set}) as
	 *         the property set.
	 */
	public static ISetProperty selfSet(Object elementType) {
		return new SelfSetProperty(elementType);
	}

	/**
	 * Returns a map property which takes the source object (a {@link Map}) as
	 * the property map. This property may be used to wrap an arbitrary Map
	 * instance in an {@link IObservableMap}.
	 * 
	 * @param keyType
	 *            the key type of the property
	 * @param valueType
	 *            the value type of the property
	 * @return a map property which takes the source object (a {@link Map} as
	 *         the property map.
	 */
	public static IMapProperty selfMap(Object keyType, Object valueType) {
		return new SelfMapProperty(keyType, valueType);
	}

	/**
	 * Returns a value property which observes the value of an
	 * {@link IObservableValue}. This property may be used e.g. for observing
	 * the respective values of an {@link IObservableList} &lt;
	 * {@link IObservableValue} &gt;.
	 * <p>
	 * Calls to {@link IValueProperty#observe(Object)} or
	 * {@link IValueProperty#observe(Realm, Object)} just cast the argument to
	 * {@link IObservableValue} and return it (the realm argument is ignored).
	 * 
	 * @param valueType
	 *            the value type of the property
	 * @return a value property which observes the value of an
	 *         {@link IObservableValue}.
	 */
	public static IValueProperty observableValue(Object valueType) {
		return new ObservableValueProperty(valueType);
	}
}
