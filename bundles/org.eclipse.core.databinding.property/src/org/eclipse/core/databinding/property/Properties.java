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

package org.eclipse.core.databinding.property;

import java.util.Map;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.value.IValueProperty;

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
}
