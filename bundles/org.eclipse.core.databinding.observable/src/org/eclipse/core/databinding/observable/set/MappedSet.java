/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 263693
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.MapDiff;

/**
 *
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 *
 * @since 1.0
 *
 * @deprecated This class is deprecated.
 */
@Deprecated
// OK to hide warnings on a deprecated class
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MappedSet extends ObservableSet {

	private final IObservableMap wrappedMap;

	/*
	 * Map from values (range elements) to Integer ref counts
	 */
	private Map valueCounts = new HashMap();

	private ISetChangeListener domainListener = new ISetChangeListener() {
		@Override
		public void handleSetChange(SetChangeEvent event) {
			Set additions = new HashSet();
			for (Object added : event.diff.getAdditions()) {
				Object mapValue = wrappedMap.get(added);
				if (handleAddition(mapValue)) {
					additions.add(mapValue);
				}
			}
			Set removals = new HashSet();
			for (Object removed : event.diff.getRemovals()) {
				Object mapValue = wrappedMap.get(removed);
				if (handleRemoval(mapValue)) {
					removals.add(mapValue);
				}
			}
			fireSetChange(Diffs.createSetDiff(additions, removals));
		}
	};

	private IMapChangeListener mapChangeListener = (MapChangeEvent event) -> {
		MapDiff diff = event.diff;
		Set additions = new HashSet();
		Set removals = new HashSet();
		for (Object key : diff.getRemovedKeys()) {
			Object oldValue = diff.getOldValue(key);
			if (handleRemoval(oldValue)) {
				removals.add(oldValue);
			}
		}
		for (Object key : diff.getChangedKeys()) {
			Object oldValue = diff.getOldValue(key);
			Object newValue = diff.getNewValue(key);
			if (handleRemoval(oldValue)) {
				removals.add(oldValue);
			}
			if (handleAddition(newValue)) {
				additions.add(newValue);
			}
		}
		for (Object key : diff.getAddedKeys()) {
			Object newValue = diff.getNewValue(key);
			if (handleAddition(newValue)) {
				additions.add(newValue);
			}
		}
		fireSetChange(Diffs.createSetDiff(additions, removals));
	};

	private IObservableSet input;

	/**
	 * @param input input set with keys from the map
	 * @param map   the map to map
	 */
	public MappedSet(IObservableSet input, IObservableMap map) {
		super(input.getRealm(), Collections.EMPTY_SET, Object.class);
		setWrappedSet(valueCounts.keySet());
		this.wrappedMap = map;
		this.input = input;
		for (Object element : input) {
			Object functionValue = wrappedMap.get(element);
			handleAddition(functionValue);
		}
		input.addSetChangeListener(domainListener);
		map.addMapChangeListener(mapChangeListener);
	}

	/**
	 * @param mapValue map value to add
	 * @return true if the given mapValue was an addition
	 */
	protected boolean handleAddition(Object mapValue) {
		Integer count = (Integer) valueCounts.get(mapValue);
		if (count == null) {
			valueCounts.put(mapValue, Integer.valueOf(1));
			return true;
		}
		valueCounts.put(mapValue, Integer.valueOf(count.intValue() + 1));
		return false;
	}

	/**
	 * @param mapValue map value to remove
	 * @return true if the given mapValue has been removed
	 */
	protected boolean handleRemoval(Object mapValue) {
		Integer count = (Integer) valueCounts.get(mapValue);
		if (count.intValue() <= 1) {
			valueCounts.remove(mapValue);
			return true;
		}
		valueCounts.put(mapValue, Integer.valueOf(count.intValue() - 1));
		return false;
	}

	@Override
	public synchronized void dispose() {
		wrappedMap.removeMapChangeListener(mapChangeListener);
		input.removeSetChangeListener(domainListener);
	}

}
