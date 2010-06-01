/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 251884, 194734, 301774
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.internal.databinding.observable.Util;

/**
 * @since 1.1
 * 
 */
public abstract class MapDiff implements IDiff {
	/**
	 * Returns true if the diff has no added, removed or changed entries.
	 * 
	 * @return true if the diff has no added, removed or changed entries.
	 * @since 1.2
	 */
	public boolean isEmpty() {
		return getAddedKeys().isEmpty() && getRemovedKeys().isEmpty()
				&& getChangedKeys().isEmpty();
	}

	/**
	 * Applies the changes in this diff to the given map
	 * 
	 * @param map
	 *            the map to which the diff will be applied
	 * @since 1.2
	 */
	public void applyTo(Map map) {
		for (Iterator it = getAddedKeys().iterator(); it.hasNext();) {
			Object key = it.next();
			map.put(key, getNewValue(key));
		}
		for (Iterator it = getChangedKeys().iterator(); it.hasNext();) {
			Object key = it.next();
			map.put(key, getNewValue(key));
		}
		for (Iterator it = getRemovedKeys().iterator(); it.hasNext();) {
			map.remove(it.next());
		}
	}

	/**
	 * Returns a map showing what <code>map</code> would look like if this diff
	 * were applied to it.
	 * <p>
	 * <b>Note</b>: the returned map is only valid until structural changes are
	 * made to the passed-in map.
	 * 
	 * @param map
	 *            the map over which the diff will be simulated
	 * @return an unmodifiable map showing what <code>map</code> would look like
	 *         if it were passed to the {@link #applyTo(Map)} method.
	 * @see #applyTo(Map)
	 * @since 1.3
	 */
	public Map simulateOn(Map map) {
		return new DeltaMap(map, this);
	}

	private static class DeltaMap extends AbstractMap {

		private final Map map;
		private final MapDiff diff;

		private Set entrySet;

		public DeltaMap(Map map, MapDiff diff) {
			this.map = map;
			this.diff = diff;

		}

		public void clear() {
			throw new UnsupportedOperationException();
		}

		public boolean containsKey(Object key) {
			return diff.getAddedKeys().contains(key)
					|| (map.containsKey(key) && !diff.getRemovedKeys()
							.contains(key));
		}

		public Set entrySet() {
			if (entrySet == null) {
				entrySet = new DeltaMapEntrySet(map, diff);
			}
			return entrySet;
		}

		public Object get(Object key) {
			if (diff.getAddedKeys().contains(key))
				return diff.getNewValue(key);
			if (diff.getChangedKeys().contains(key))
				return diff.getNewValue(key);
			if (diff.getRemovedKeys().contains(key))
				return null;
			return map.get(key);
		}

		public Object put(Object arg0, Object arg1) {
			throw new UnsupportedOperationException();
		}

		public void putAll(Map arg0) {
			throw new UnsupportedOperationException();
		}

		public Object remove(Object key) {
			throw new UnsupportedOperationException();
		}

	}

	private static class DeltaMapEntrySet extends AbstractSet {

		private final Map map;
		private final MapDiff diff;

		public DeltaMapEntrySet(Map map, MapDiff diff) {
			this.map = map;
			this.diff = diff;
		}

		public Iterator iterator() {
			return new Iterator() {
				Iterator origEntries = map.entrySet().iterator();
				Iterator addedKeys = diff.getAddedKeys().iterator();

				boolean haveNext = false;
				Map.Entry next;

				public boolean hasNext() {
					return findNext();
				}

				public Object next() {
					if (!findNext())
						throw new NoSuchElementException();

					Map.Entry myNext = next;
					haveNext = false;
					next = null;
					return myNext;
				}

				private boolean findNext() {
					if (haveNext)
						return true;
					while (true) {
						Object candidateKey;
						Map.Entry candidateEntry;
						if (origEntries.hasNext()) {
							candidateEntry = (Map.Entry) origEntries.next();
							candidateKey = candidateEntry.getKey();

							if (diff.getRemovedKeys().contains(candidateKey)) {
								continue;
							} else if (diff.getChangedKeys().contains(
									candidateKey)) {
								candidateEntry = new DeltaMapEntry(
										candidateKey, diff);
							} else {
								candidateEntry = new MapEntryWrapper(
										candidateEntry);
							}
						} else if (addedKeys.hasNext()) {
							candidateKey = addedKeys.next();
							candidateEntry = new DeltaMapEntry(candidateKey,
									diff);
						} else {
							return false;
						}

						haveNext = true;
						next = candidateEntry;
						return true;
					}
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}

		public int size() {
			return map.size() + diff.getAddedKeys().size()
					- diff.getRemovedKeys().size();
		}

	}

	private abstract static class AbstractMapEntry implements Map.Entry {
		public Object setValue(Object arg0) {
			throw new UnsupportedOperationException();
		}

		public boolean equals(Object obj) {
			if (!(obj instanceof Map.Entry))
				return false;
			Map.Entry that = (Map.Entry) obj;
			return Util.equals(this.getKey(), that.getKey())
					&& Util.equals(this.getValue(), that.getValue());
		}

		public int hashCode() {
			Object key = getKey();
			Object value = getValue();
			return hash(key) ^ hash(value);
		}

		private int hash(Object key) {
			return key == null ? 0 : key.hashCode();
		}
	}

	private static class MapEntryWrapper extends AbstractMapEntry {
		private final Entry entry;

		public MapEntryWrapper(Map.Entry entry) {
			this.entry = entry;
		}

		public Object getKey() {
			return entry.getKey();
		}

		public Object getValue() {
			return entry.getValue();
		}

	}

	private static class DeltaMapEntry extends AbstractMapEntry {
		private final Object key;
		private final MapDiff diff;

		public DeltaMapEntry(Object key, MapDiff diff) {
			this.key = key;
			this.diff = diff;
		}

		public Object getKey() {
			return key;
		}

		public Object getValue() {
			return diff.getNewValue(key);
		}

	}

	/**
	 * @return the set of keys which were added
	 */
	public abstract Set getAddedKeys();

	/**
	 * @return the set of keys which were removed
	 */
	public abstract Set getRemovedKeys();

	/**
	 * @return the set of keys for which the value has changed
	 */
	public abstract Set getChangedKeys();

	/**
	 * Returns the old value for the given key, which must be an element of
	 * {@link #getRemovedKeys()} or {@link #getChangedKeys()}.
	 * 
	 * @param key
	 * @return the old value for the given key.
	 */
	public abstract Object getOldValue(Object key);

	/**
	 * Returns the new value for the given key, which must be an element of
	 * {@link #getChangedKeys()} or {@link #getAddedKeys()}.
	 * 
	 * @param key
	 * @return the new value for the given key.
	 */
	public abstract Object getNewValue(Object key);
}
