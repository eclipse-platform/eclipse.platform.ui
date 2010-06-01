/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 241585, 247394, 226289, 194734, 190881, 266754,
 *                    268688
 *     Ovidio Mallo - bug 303847
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.internal.databinding.identity.IdentitySet;

/**
 * Maps objects to one of their attributes. Tracks changes to the underlying
 * observable set of objects (keys), as well as changes to attribute values.
 */
public abstract class ComputedObservableMap extends AbstractObservableMap {

	private IObservableSet keySet;

	private Set knownKeys;

	private Object valueType;

	private ISetChangeListener setChangeListener = new ISetChangeListener() {
		public void handleSetChange(SetChangeEvent event) {
			Set addedKeys = new HashSet(event.diff.getAdditions());
			Set removedKeys = new HashSet(event.diff.getRemovals());
			Map oldValues = new HashMap();
			Map newValues = new HashMap();
			for (Iterator it = removedKeys.iterator(); it.hasNext();) {
				Object removedKey = it.next();
				Object oldValue = null;
				if (removedKey != null) {
					oldValue = doGet(removedKey);
					unhookListener(removedKey);
					knownKeys.remove(removedKey);
				}
				oldValues.put(removedKey, oldValue);
			}
			for (Iterator it = addedKeys.iterator(); it.hasNext();) {
				Object addedKey = it.next();
				Object newValue = null;
				if (addedKey != null) {
					newValue = doGet(addedKey);
					hookListener(addedKey);
					knownKeys.add(addedKey);
				}
				newValues.put(addedKey, newValue);
			}
			fireMapChange(Diffs.createMapDiff(addedKeys, removedKeys,
					Collections.EMPTY_SET, oldValues, newValues));
		}
	};

	private IStaleListener staleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
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
							getterCalled();
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
	 * @param keySet
	 */
	public ComputedObservableMap(IObservableSet keySet) {
		this(keySet, null);
	}

	/**
	 * @param keySet
	 * @param valueType
	 * @since 1.2
	 */
	public ComputedObservableMap(IObservableSet keySet, Object valueType) {
		super(keySet.getRealm());
		this.keySet = keySet;
		this.valueType = valueType;

		keySet.addDisposeListener(new IDisposeListener() {
			public void handleDispose(DisposeEvent staleEvent) {
				ComputedObservableMap.this.dispose();
			}
		});
	}

	/**
	 * @deprecated Subclasses are no longer required to call this method.
	 */
	protected void init() {
	}

	protected void firstListenerAdded() {
		getRealm().exec(new Runnable() {
			public void run() {
				hookListeners();
			}
		});
	}

	protected void lastListenerRemoved() {
		unhookListeners();
	}

	private void hookListeners() {
		if (keySet != null) {
			knownKeys = new IdentitySet();
			keySet.addSetChangeListener(setChangeListener);
			keySet.addStaleListener(staleListener);
			for (Iterator it = this.keySet.iterator(); it.hasNext();) {
				Object key = it.next();
				hookListener(key);
				knownKeys.add(key);
			}
		}
	}

	private void unhookListeners() {
		if (keySet != null) {
			keySet.removeSetChangeListener(setChangeListener);
			keySet.removeStaleListener(staleListener);
		}
		if (knownKeys != null) {
			Object[] keys = knownKeys.toArray();
			for (int i = 0; i < keys.length; i++) {
				unhookListener(keys[i]);
			}
			knownKeys.clear();
			knownKeys = null;
		}
	}

	protected final void fireSingleChange(Object key, Object oldValue,
			Object newValue) {
		fireMapChange(Diffs.createMapDiffSingleChange(key, oldValue, newValue));
	}

	/**
	 * @since 1.2
	 */
	public Object getKeyType() {
		return keySet.getElementType();
	}

	/**
	 * @since 1.2
	 */
	public Object getValueType() {
		return valueType;
	}

	/**
	 * @since 1.3
	 */
	public Object remove(Object key) {
		checkRealm();

		Object oldValue = get(key);
		keySet().remove(key);

		return oldValue;
	}

	/**
	 * @since 1.3
	 */
	public boolean containsKey(Object key) {
		getterCalled();
		return keySet().contains(key);
	}

	public Set entrySet() {
		return entrySet;
	}

	public Set keySet() {
		return keySet;
	}

	final public Object get(Object key) {
		getterCalled();
		if (!keySet.contains(key))
			return null;
		return doGet(key);
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	final public Object put(Object key, Object value) {
		checkRealm();
		if (!keySet.contains(key))
			return null;
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

	public boolean isStale() {
		return super.isStale() || keySet.isStale();
	}

	public synchronized void dispose() {
		unhookListeners();
		entrySet = null;
		keySet = null;
		setChangeListener = null;
		super.dispose();
	}
}
