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

package org.eclipse.core.databinding.observable.map;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetDiff;

/**
 * Maps objects to one of their attributes. Tracks changes to the underlying
 * observable set of objects (keys), as well as changes to attribute values.
 */
public abstract class ComputedObservableMap extends AbstractObservableMap {

	private final IObservableSet keySet;

	private ISetChangeListener setChangeListener = new ISetChangeListener() {
		public void handleSetChange(IObservableSet source, SetDiff diff) {
			Set addedKeys = new HashSet(diff.getAdditions());
			Set removedKeys = new HashSet(diff.getRemovals());
			Map oldValues = new HashMap();
			Map newValues = new HashMap();
			for (Iterator it = removedKeys.iterator(); it.hasNext();) {
				Object removedKey = it.next();
				Object oldValue = doGet(removedKey);
				unhookListener(removedKey);
				if (oldValue != null) {
					oldValues.put(removedKey, oldValue);
				}
			}
			for (Iterator it = addedKeys.iterator(); it.hasNext();) {
				Object addedKey = it.next();
				hookListener(addedKey);
				Object newValue = doGet(addedKey);
				newValues.put(addedKey, newValue);
			}
			fireMapChange(Diffs.createMapDiff(addedKeys, removedKeys,
					Collections.EMPTY_SET, oldValues, newValues));
		}
	};

	private Set entrySet = new EntrySet();

	private class EntrySet extends AbstractSet {

		public Iterator iterator() {
			final Iterator keyIterator = keySet.iterator();
			return new Iterator() {

				public boolean hasNext() {
					return keyIterator.hasNext();
				}

				public Object next() {
					final Object key = keyIterator.next();
					return new Map.Entry() {

						public Object getKey() {
							return key;
						}

						public Object getValue() {
							return get(getKey());
						}

						public Object setValue(Object value) {
							return put(getKey(), value);
						}
					};
				}

				public void remove() {
					keyIterator.remove();
				}
			};
		}

		public int size() {
			return keySet.size();
		}

	}

	/**
	 * @param realm
	 * @param keySet
	 * @param wrappedMap
	 */
	public ComputedObservableMap(IObservableSet keySet) {
		super(keySet.getRealm());
		this.keySet = keySet;
		this.keySet.addSetChangeListener(setChangeListener);
	}

	protected void init() {
		for (Iterator it = this.keySet.iterator(); it.hasNext();) {
			Object key = it.next();
			hookListener(key);
		}
	}

	protected final void fireSingleChange(Object key, Object oldValue,
			Object newValue) {
		fireMapChange(Diffs.createMapDiffSingleChange(key, oldValue, newValue));
	}

	public Set entrySet() {
		return entrySet;
	}
	
	public Set keySet() {
		return keySet;
	}

	final public Object get(Object key) {
		return doGet(key);
	}

	final public Object put(Object key, Object value) {
		return doPut(key, value);
	}

	/**
	 * @param removedKey
	 */
	protected abstract void unhookListener(Object removedKey);

	/**
	 * @param addedKey
	 */
	protected abstract void hookListener(Object addedKey);

	/**
	 * @param key
	 * @return the value for the given key
	 */
	protected abstract Object doGet(Object key);

	/**
	 * @param key
	 * @param value
	 * @return the old value for the given key
	 */
	protected abstract Object doPut(Object key, Object value);
}
