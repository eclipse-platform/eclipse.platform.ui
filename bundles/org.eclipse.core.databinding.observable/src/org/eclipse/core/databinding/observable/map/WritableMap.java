/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bugs 184830, 233306, 226289, 190881
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
import org.eclipse.core.internal.databinding.observable.Util;

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
	private final Object keyType;
	private final Object valueType;

	/**
	 * Constructs a new WritableMap on the default realm.
	 */
	public WritableMap() {
		this(Realm.getDefault(), null, null);
	}
	
	/**
	 * Constructs a new WritableMap on the given realm.
	 * 
	 * @param realm
	 *            the realm
	 */
	public WritableMap(Realm realm) {
		this(realm, null, null);
	}

	/**
	 * Constructs a new WritableMap on the default realm with the specified key
	 * and value types.
	 * 
	 * @param keyType
	 * @param valueType
	 * @since 1.2
	 */
	public WritableMap(Object keyType, Object valueType) {
		this(Realm.getDefault(), keyType, valueType);
	}

	/**
	 * Constructs a new WritableMap on the given realm with the specified key
	 * and value types.
	 * 
	 * @param realm
	 * @param keyType
	 * @param valueType
	 * @since 1.2
	 */
	public WritableMap(Realm realm, Object keyType, Object valueType) {
		super(realm, new HashMap());
		this.keyType = keyType;
		this.valueType = valueType;
	}

	/**
	 * @since 1.2
	 */
	public Object getKeyType() {
		return keyType;
	}

	/**
	 * @since 1.2
	 */
	public Object getValueType() {
		return valueType;
	}

	/**
	 * Associates the provided <code>value</code> with the <code>key</code>.  Must be invoked from the current realm.
	 */
	public Object put(Object key, Object value) {
		checkRealm();

		boolean containedKeyBefore = wrappedMap.containsKey(key);
		Object result = wrappedMap.put(key, value);
		boolean containedKeyAfter = wrappedMap.containsKey(key);

		if (containedKeyBefore != containedKeyAfter
				|| !Util.equals(result, value)) {
			MapDiff diff;
			if (containedKeyBefore) {
				if (containedKeyAfter) {
					diff = Diffs
							.createMapDiffSingleChange(key, result, value);
				} else {
					diff = Diffs.createMapDiffSingleRemove(key, result);
				}
			} else {
				diff = Diffs.createMapDiffSingleAdd(key, value);
			}
			fireMapChange(diff);
		}
		return result;
	}

	/**
	 * Removes the value with the provide <code>key</code>.  Must be invoked from the current realm.
	 */
	public Object remove(Object key) {
		checkRealm();
		if (wrappedMap.containsKey(key)) {
			Object result = wrappedMap.remove(key);
			fireMapChange(Diffs.createMapDiffSingleRemove(key, result));
			return result;
		}
		return null;
	}

	/**
	 * Clears the map.  Must be invoked from the current realm.
	 */
	public void clear() {
		checkRealm();
		if (!isEmpty()) {
			Map copy = new HashMap(wrappedMap);
			wrappedMap.clear();
			fireMapChange(Diffs.createMapDiffRemoveAll(copy));
		}
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
			boolean add = !wrappedMap.containsKey(entry.getKey());
			Object previousValue = wrappedMap.put(entry.getKey(), entry
					.getValue());
			if (add) {
				addedKeys.add(entry.getKey());
			} else {
				changes.put(entry.getKey(), previousValue);
			}
		}
		if (!addedKeys.isEmpty() || !changes.isEmpty()) {
			fireMapChange(Diffs.createMapDiff(addedKeys, Collections.EMPTY_SET,
					changes.keySet(), changes, wrappedMap));
		}
	}

}
