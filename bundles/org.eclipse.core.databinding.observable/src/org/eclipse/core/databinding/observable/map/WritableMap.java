/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;

/**
 * 
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * @since 1.0
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

	/**
	 * Associates the provided <code>value</code> with the <code>key</code>.  Must be invoked from the current realm.
	 */
	public Object put(Object key, Object value) {
		checkRealm();
		Object result = wrappedMap.put(key, value);
		if (result==null) {
			fireMapChange(Diffs.createMapDiffSingleAdd(key, value));
		} else {
			fireMapChange(Diffs.createMapDiffSingleChange(key, result, value));
		}
		return result;
	}

	/**
	 * Removes the value with the provide <code>key</code>.  Must be invoked from the current realm.
	 */
	public Object remove(Object key) {
		checkRealm();
		Object result = wrappedMap.remove(key);
		if (result!=null) {
			fireMapChange(Diffs.createMapDiffSingleRemove(key, result));
		}
		return result;
	}

	/**
	 * Clears the map.  Must be invoked from the current realm.
	 */
	public void clear() {
		checkRealm();
		Map copy = new HashMap(wrappedMap.size());
		copy.putAll(wrappedMap);
		wrappedMap.clear();
		fireMapChange(Diffs.createMapDiffRemoveAll(copy));
	}

	/**
	 * Adds the provided <code>map</code>'s contents to this map.  Must be invoked from the current realm.
	 */
	public void putAll(Map map) {
		checkRealm();
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
