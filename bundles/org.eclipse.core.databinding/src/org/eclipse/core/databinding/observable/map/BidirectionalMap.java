/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.databinding.observable.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @since 3.3
 * 
 */
public class BidirectionalMap extends ObservableMap {

	private Map valueToElements = new HashMap();

	private IMapChangeListener mapListener = new IMapChangeListener() {

		public void handleMapChange(MapChangeEvent event) {
			MapDiff diff = event.diff;
			for (Iterator it = diff.getAddedKeys().iterator(); it.hasNext();) {
				Object addedKey = it.next();
				addMapping(addedKey, diff.getNewValue(addedKey));
			}
			for (Iterator it = diff.getChangedKeys().iterator(); it.hasNext();) {
				Object changedKey = it.next();
				removeMapping(changedKey, diff.getOldValue(changedKey));
				addMapping(changedKey, diff.getNewValue(changedKey));
			}
			for (Iterator it = diff.getRemovedKeys().iterator(); it.hasNext();) {
				Object removedKey = it.next();
				removeMapping(removedKey, diff.getOldValue(removedKey));
			}
			fireMapChange(diff);
		}
	};

	/**
	 * @param wrappedMap
	 */
	public BidirectionalMap(IObservableMap wrappedMap) {
		super(wrappedMap.getRealm(), wrappedMap);
		wrappedMap.addMapChangeListener(mapListener);
		for (Iterator it = wrappedMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Entry) it.next();
			addMapping(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @param key
	 * @param value
	 */
	private void addMapping(Object key, Object value) {
		Object elementOrSet = valueToElements.get(value);
		if (elementOrSet == null) {
			valueToElements.put(value, key);
			return;
		}
		if (!(elementOrSet instanceof Set)) {
			elementOrSet = new HashSet(Collections.singleton(elementOrSet));
			valueToElements.put(value, elementOrSet);
		}
		Set set = (Set) elementOrSet;
		set.add(key);
	}

	/**
	 * @param functionValue
	 * @param element
	 */
	private void removeMapping(Object functionValue, Object element) {
		Object elementOrSet = valueToElements.get(functionValue);
		if (elementOrSet instanceof Set) {
			Set set = (Set) elementOrSet;
			set.remove(element);
			if (set.size() == 0) {
				valueToElements.remove(functionValue);
			}
		} else {
			valueToElements.remove(functionValue);
		}
	}

}
