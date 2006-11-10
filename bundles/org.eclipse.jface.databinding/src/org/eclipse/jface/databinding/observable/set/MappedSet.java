/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.observable.set;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.map.IMapChangeListener;
import org.eclipse.jface.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.observable.map.MapDiff;

/**
 * @since 1.0
 * 
 */
public class MappedSet extends ObservableSet {

	private final IObservableMap wrappedMap;

	/*
	 * Map from values (range elements) to Integer ref counts
	 */
	private Map valueCounts = new HashMap();

	private ISetChangeListener domainListener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, SetDiff diff) {
			Set additions = new HashSet();
			for (Iterator it = diff.getAdditions().iterator(); it.hasNext();) {
				Object added = it.next();
				Object mapValue = wrappedMap.get(added);
				if (handleAddition(mapValue)) {
					additions.add(mapValue);
				}
			}
			Set removals = new HashSet();
			for (Iterator it = diff.getRemovals().iterator(); it.hasNext();) {
				Object removed = it.next();
				Object mapValue = wrappedMap.get(removed);
				if (handleRemoval(mapValue)) {
					removals.add(mapValue);
				}
			}
			fireSetChange(Diffs.createSetDiff(additions, removals));
		}
	};

	private IMapChangeListener mapChangeListener = new IMapChangeListener() {
		public void handleMapChange(IObservableMap source, MapDiff diff) {
			Set additions = new HashSet();
			Set removals = new HashSet();
			for (Iterator it = diff.getRemovedKeys().iterator(); it.hasNext();) {
				Object key = it.next();
				Object oldValue = diff.getOldValue(key);
				if (handleRemoval(oldValue)) {
					removals.add(oldValue);
				}
			}
			for (Iterator it = diff.getChangedKeys().iterator(); it.hasNext();) {
				Object key = it.next();
				Object oldValue = diff.getOldValue(key);
				Object newValue = diff.getNewValue(key);
				if (handleRemoval(oldValue)) {
					removals.add(oldValue);
				}
				if (handleAddition(newValue)) {
					additions.add(newValue);
				}
			}
			for (Iterator it = diff.getAddedKeys().iterator(); it.hasNext();) {
				Object key = it.next();
				Object newValue = diff.getNewValue(key);
				if (handleAddition(newValue)) {
					additions.add(newValue);
				}
			}
			fireSetChange(Diffs.createSetDiff(additions, removals));
		}
	};

	private IObservableSet input;

	/**
	 * @param input
	 * @param map
	 */
	public MappedSet(IObservableSet input, IObservableMap map) {
		super(input.getRealm(), Collections.EMPTY_SET, Object.class);
		setWrappedSet(valueCounts.keySet());
		this.wrappedMap = map;
		this.input = input;
		for (Iterator it = input.iterator(); it.hasNext();) {
			Object element = it.next();
			Object functionValue = wrappedMap.get(element);
			handleAddition(functionValue);
		}
		input.addSetChangeListener(domainListener);
		map.addMapChangeListener(mapChangeListener);
	}

	/**
	 * @param mapValue
	 * @return true if the given mapValue was an addition
	 */
	protected boolean handleAddition(Object mapValue) {
		Integer count = (Integer) valueCounts.get(mapValue);
		if (count == null) {
			valueCounts.put(mapValue, new Integer(1));
			return true;
		}
		valueCounts.put(mapValue, new Integer(count.intValue() + 1));
		return false;
	}

	/**
	 * @param mapValue
	 * @return true if the given mapValue has been removed
	 */
	protected boolean handleRemoval(Object mapValue) {
		Integer count = (Integer) valueCounts.get(mapValue);
		if (count.intValue() <= 1) {
			valueCounts.remove(mapValue);
			return true;
		}
		valueCounts.put(mapValue, new Integer(count.intValue() - 1));
		return false;
	}

	public void dispose() {
		wrappedMap.removeMapChangeListener(mapChangeListener);
		input.removeSetChangeListener(domainListener);
	}

}
