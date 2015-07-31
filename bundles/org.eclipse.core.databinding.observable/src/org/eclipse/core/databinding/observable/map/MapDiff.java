/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 251884, 194734, 301774
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *     Stefan Xenos <sxenos@gmail.com> - Bug 474065
 *******************************************************************************/

package org.eclipse.core.databinding.observable.map;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.internal.databinding.observable.Util;

/**
 * @param <K>
 *            the type of keys maintained by this map
 * @param <V>
 *            the type of mapped values
 * @since 1.1
 *
 */
public abstract class MapDiff<K, V> implements IDiff {
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
	public void applyTo(Map<K, V> map) {
		for (Iterator<? extends K> it = getAddedKeys().iterator(); it.hasNext();) {
			K key = it.next();
			map.put(key, getNewValue(key));
		}
		for (Iterator<? extends K> it = getChangedKeys().iterator(); it.hasNext();) {
			K key = it.next();
			map.put(key, getNewValue(key));
		}
		for (Iterator<? extends K> it = getRemovedKeys().iterator(); it.hasNext();) {
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
	public Map<K, V> simulateOn(Map<K, V> map) {
		return new DeltaMap<K, V>(map, this);
	}

	private static class DeltaMap<K, V> extends AbstractMap<K, V> {

		private final Map<K, V> map;
		private final MapDiff<K, V> diff;

		private Set<Entry<K, V>> entrySet;

		public DeltaMap(Map<K, V> map, MapDiff<K, V> diff) {
			this.map = map;
			this.diff = diff;

		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsKey(Object key) {
			return diff.getAddedKeys().contains(key)
					|| (map.containsKey(key) && !diff.getRemovedKeys()
							.contains(key));
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			if (entrySet == null) {
				entrySet = new DeltaMapEntrySet<K, V>(map, diff);
			}
			return entrySet;
		}

		@Override
		public V get(Object key) {
			if (diff.getAddedKeys().contains(key))
				return diff.getNewValue(key);
			if (diff.getChangedKeys().contains(key))
				return diff.getNewValue(key);
			if (diff.getRemovedKeys().contains(key))
				return null;
			return map.get(key);
		}

		@Override
		public V put(Object arg0, Object arg1) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public V remove(Object key) {
			throw new UnsupportedOperationException();
		}

	}

	private static class DeltaMapEntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {

		private final Map<K, V> map;
		private final MapDiff<K, V> diff;

		public DeltaMapEntrySet(Map<K, V> map, MapDiff<K, V> diff) {
			this.map = map;
			this.diff = diff;
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new Iterator<Map.Entry<K, V>>() {
				Iterator<Map.Entry<K, V>> origEntries = map.entrySet().iterator();
				Iterator<? extends K> addedKeys = diff.getAddedKeys().iterator();

				boolean haveNext = false;
				Map.Entry<K, V> next;

				@Override
				public boolean hasNext() {
					return findNext();
				}

				@Override
				public Map.Entry<K, V> next() {
					if (!findNext())
						throw new NoSuchElementException();

					Map.Entry<K, V> myNext = next;
					haveNext = false;
					next = null;
					return myNext;
				}

				private boolean findNext() {
					if (haveNext)
						return true;
					while (true) {
						K candidateKey;
						Map.Entry<K, V> candidateEntry;
						if (origEntries.hasNext()) {
							candidateEntry = origEntries.next();
							candidateKey = candidateEntry.getKey();

							if (diff.getRemovedKeys().contains(candidateKey)) {
								continue;
							} else if (diff.getChangedKeys().contains(candidateKey)) {
								candidateEntry = new DeltaMapEntry<K, V>(candidateKey, diff);
							} else {
								candidateEntry = new MapEntryWrapper<K, V>(candidateEntry);
							}
						} else if (addedKeys.hasNext()) {
							candidateKey = addedKeys.next();
							candidateEntry = new DeltaMapEntry<K, V>(candidateKey, diff);
						} else {
							return false;
						}

						haveNext = true;
						next = candidateEntry;
						return true;
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}

		@Override
		public int size() {
			return map.size() + diff.getAddedKeys().size() - diff.getRemovedKeys().size();
		}

	}

	private abstract static class AbstractMapEntry<K, V> implements Map.Entry<K, V> {
		@Override
		public V setValue(Object arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Map.Entry))
				return false;
			Map.Entry<?, ?> that = (Map.Entry<?, ?>) obj;
			return Util.equals(this.getKey(), that.getKey()) && Util.equals(this.getValue(), that.getValue());
		}

		@Override
		public int hashCode() {
			Object key = getKey();
			Object value = getValue();
			return hash(key) ^ hash(value);
		}

		private int hash(Object key) {
			return key == null ? 0 : key.hashCode();
		}
	}

	private static class MapEntryWrapper<K, V> extends AbstractMapEntry<K, V> {
		private final Entry<K, V> entry;

		public MapEntryWrapper(Map.Entry<K, V> entry) {
			this.entry = entry;
		}

		@Override
		public K getKey() {
			return entry.getKey();
		}

		@Override
		public V getValue() {
			return entry.getValue();
		}

	}

	private static class DeltaMapEntry<K, V> extends AbstractMapEntry<K, V> {
		private final K key;
		private final MapDiff<K, V> diff;

		public DeltaMapEntry(K key, MapDiff<K, V> diff) {
			this.key = key;
			this.diff = diff;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return diff.getNewValue(key);
		}

	}

	/**
	 * @return the set of keys which were added
	 */
	public abstract Set<K> getAddedKeys();

	/**
	 * @return the set of keys which were removed
	 */
	public abstract Set<K> getRemovedKeys();

	/**
	 * @return the set of keys for which the value has changed
	 */
	public abstract Set<K> getChangedKeys();

	/**
	 * Returns the old value for the given key, which must be an element of
	 * {@link #getRemovedKeys()} or {@link #getChangedKeys()}.
	 *
	 * @param key
	 * @return the old value for the given key.
	 */
	public abstract V getOldValue(Object key);

	/**
	 * Returns the new value for the given key, which must be an element of
	 * {@link #getChangedKeys()} or {@link #getAddedKeys()}.
	 *
	 * @param key
	 * @return the new value for the given key.
	 */
	public abstract V getNewValue(Object key);
}
