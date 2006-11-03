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

package org.eclipse.jface.databinding.observable.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.ObservableTracker;
import org.eclipse.jface.databinding.observable.Realm;

/**
 * @since 3.3
 *
 */
public class WritableMap extends ObservableMap {

	/**
	 * 
	 */
	public WritableMap() {
		this(Realm.getDefault());
	}
	
	/**
	 * @param realm
	 */
	public WritableMap(Realm realm) {
		super(realm, new HashMap());
	}

	public Object put(Object key, Object value) {
		ObservableTracker.getterCalled(this);
		Object result = wrappedMap.put(key, value);
		if (result==null) {
			fireMapChange(Diffs.createMapDiffSingleAdd(key, value));
		} else {
			fireMapChange(Diffs.createMapDiffSingleChange(key, value, result));
		}
		return result;
	}

	public Object remove(Object key) {
		ObservableTracker.getterCalled(this);
		Object result = wrappedMap.remove(key);
		if (result!=null) {
			fireMapChange(Diffs.createMapDiffSingleRemove(key, result));
		}
		return result;
	}

	public void clear() {
		Map copy = new HashMap(wrappedMap.size());
		copy.putAll(wrappedMap);
		wrappedMap.clear();
		fireMapChange(Diffs.createMapDiffRemoveAll(copy));
	}

	public void putAll(Map map) {
		Set addedKeys = new HashSet(map.size());
		Map changes = new HashMap(map.size());
		for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Entry) it.next();
			Object previousValue = wrappedMap.put(entry.getKey(), entry.getValue());
			if (previousValue==null) {
				addedKeys.add(entry.getKey());
			} else {
				changes.put(entry.getKey(), previousValue);
			}
		}
		fireMapChange(Diffs.createMapDiff(addedKeys, Collections.EMPTY_SET, changes.keySet(), changes, wrappedMap));
	}

}
