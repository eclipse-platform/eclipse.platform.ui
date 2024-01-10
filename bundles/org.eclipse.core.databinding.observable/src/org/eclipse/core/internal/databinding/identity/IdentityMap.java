/*******************************************************************************
 * Copyright (c) 2008, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 *     Matthew Hall - bug 228125
 *         (through ViewerElementMap.java)
 *     Matthew Hall - bugs 262269, 303847
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 ******************************************************************************/

package org.eclipse.core.internal.databinding.identity;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

/**
 * A {@link Map} whose keys are added, removed and compared by identity. The
 * keys in the map are compared using <code>==</code> instead of
 * {@link #equals(Object)}.
 * <p>
 * This class is <i>not</i> a strict implementation the {@link Map} interface.
 * It intentionally violates the {@link Map} contract, which requires the use of
 * {@link #equals(Object)} when comparing keys.
 *
 * @param <K>
 *            the type of the keys in the map
 * @param <V>
 *            the type of the values in the map
 * @since 1.2
 */
public class IdentityMap<K, V> implements Map<K, V> {
	private Map<IdentityWrapper<K>, V> wrappedMap;

	/**
	 * Constructs an IdentityMap.
	 */
	public IdentityMap() {
		this.wrappedMap = new HashMap<>();
	}

	/**
	 * Constructs an IdentityMap containing all the entries in the specified
	 * map.
	 *
	 * @param map
	 *            the map whose entries are to be added to this map.
	 */
	public IdentityMap(Map<? extends K, ? extends V> map) {
		this();
		Assert.isNotNull(map);
		putAll(map);
	}

	@Override
	public void clear() {
		wrappedMap.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return wrappedMap.containsKey(IdentityWrapper.wrap(key));
	}

	@Override
	public boolean containsValue(Object value) {
		return wrappedMap.containsValue(value);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		final Set<Map.Entry<IdentityWrapper<K>, V>> wrappedEntrySet = wrappedMap
				.entrySet();
		return new Set<Map.Entry<K, V>>() {
			@Override
			public boolean add(Map.Entry<K, V> o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				wrappedEntrySet.clear();
			}

			@Override
			public boolean contains(Object o) {
				for (Entry<K, V> entry : this)
					if (entry.equals(o))
						return true;
				return false;
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				for (Object element : c)
					if (!contains(element))
						return false;
				return true;
			}

			@Override
			public boolean isEmpty() {
				return wrappedEntrySet.isEmpty();
			}

			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				final Iterator<Map.Entry<IdentityWrapper<K>, V>> wrappedIterator = wrappedEntrySet.iterator();
				return new Iterator<Map.Entry<K, V>>() {
					@Override
					public boolean hasNext() {
						return wrappedIterator.hasNext();
					}

					@Override
					public Map.Entry<K, V> next() {
						final Map.Entry<IdentityWrapper<K>, V> wrappedEntry = wrappedIterator
								.next();
						return new Map.Entry<K, V>() {
							@Override
							public K getKey() {
								return wrappedEntry.getKey().unwrap();
							}

							@Override
							public V getValue() {
								return wrappedEntry.getValue();
							}

							@Override
							public V setValue(V value) {
								return wrappedEntry.setValue(value);
							}

							@Override
							public boolean equals(Object obj) {
								if (obj == this)
									return true;
								if (!(obj instanceof Map.Entry))
									return false;
								Map.Entry<?, ?> that = (Map.Entry<?, ?>) obj;
								return this.getKey() == that.getKey()
										&& Objects.equals(this.getValue(),
												that.getValue());
							}

							@Override
							public int hashCode() {
								return wrappedEntry.hashCode();
							}
						};
					}

					@Override
					public void remove() {
						wrappedIterator.remove();
					}
				};
			}

			@Override
			public boolean remove(Object o) {
				final Map.Entry<?, ?> unwrappedEntry = (Map.Entry<?, ?>) o;
				Object key = unwrappedEntry.getKey();
				final IdentityWrapper<Object> wrappedKey = IdentityWrapper
						.wrap(key);
				Map.Entry<IdentityWrapper<Object>, Object> wrappedEntry = new Map.Entry<>() {
					@Override
					public IdentityWrapper<Object> getKey() {
						return wrappedKey;
					}

					@Override
					public Object getValue() {
						return unwrappedEntry.getValue();
					}

					@Override
					public Object setValue(Object value) {
						throw new UnsupportedOperationException();
					}

					@Override
					public boolean equals(Object obj) {
						if (obj == this)
							return true;
						if (obj == null || !(obj instanceof Map.Entry))
							return false;
						Map.Entry<?, ?> that = (Map.Entry<?, ?>) obj;
						return Objects.equals(wrappedKey, that.getKey())
								&& Objects.equals(this.getValue(), that.getValue());
					}

					@Override
					public int hashCode() {
						return wrappedKey.hashCode() ^ Objects.hashCode(getValue());
					}
				};
				return wrappedEntrySet.remove(wrappedEntry);
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				boolean changed = false;
				for (Object element : c)
					changed |= remove(element);
				return changed;
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				boolean changed = false;
				Object[] toRetains = c.toArray();
				outer: for (Iterator<?> iterator = iterator(); iterator
						.hasNext();) {
					Object entry = iterator.next();
					for (Object toRetain : toRetains) {
						if (entry.equals(toRetain)) {
							continue outer;
						}
					}
					iterator.remove();
					changed = true;
				}
				return changed;
			}

			@Override
			public int size() {
				return wrappedEntrySet.size();
			}

			@Override
			public Object[] toArray() {
				return toArray(new Object[size()]);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T[] toArray(T[] a) {
				int size = size();
				if (a.length < size) {
					a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
				}
				int i = 0;
				for (Entry<K, V> entry : this) {
					a[i++] = (T) entry;
				}
				return a;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == this)
					return true;
				if (obj == null || !(obj instanceof Set))
					return false;
				Set<?> that = (Set<?>) obj;
				return this.size() == that.size() && containsAll(that);
			}

			@Override
			public int hashCode() {
				return wrappedEntrySet.hashCode();
			}
		};
	}

	@Override
	public V get(Object key) {
		return wrappedMap.get(IdentityWrapper.wrap(key));
	}

	@Override
	public boolean isEmpty() {
		return wrappedMap.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		final Set<IdentityWrapper<K>> wrappedKeySet = wrappedMap.keySet();
		return new Set<K>() {
			@Override
			public boolean add(K o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean addAll(Collection<? extends K> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				wrappedKeySet.clear();
			}

			@Override
			public boolean contains(Object o) {
				return wrappedKeySet.contains(IdentityWrapper.wrap(o));
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				for (Object element : c)
					if (!wrappedKeySet.contains(IdentityWrapper.wrap(element)))
						return false;
				return true;
			}

			@Override
			public boolean isEmpty() {
				return wrappedKeySet.isEmpty();
			}

			@Override
			public Iterator<K> iterator() {
				final Iterator<IdentityWrapper<K>> wrappedIterator = wrappedKeySet
						.iterator();
				return new Iterator<K>() {
					@Override
					public boolean hasNext() {
						return wrappedIterator.hasNext();
					}

					@Override
					public K next() {
						return wrappedIterator.next().unwrap();
					}

					@Override
					public void remove() {
						wrappedIterator.remove();
					}
				};
			}

			@Override
			public boolean remove(Object o) {
				return wrappedKeySet.remove(IdentityWrapper.wrap(o));
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				boolean changed = false;
				for (Object element : c)
					changed |= wrappedKeySet.remove(IdentityWrapper
							.wrap(element));
				return changed;
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				boolean changed = false;
				Object[] toRetains = c.toArray();
				outer: for (Object element : this) {
					for (Object toRetain : toRetains) {
						if (element == toRetain) {
							continue outer;
						}
					}
					// element not contained in collection, remove.
					remove(element);
					changed = true;
				}
				return changed;
			}

			@Override
			public int size() {
				return wrappedKeySet.size();
			}

			@Override
			public Object[] toArray() {
				return toArray(new Object[wrappedKeySet.size()]);
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T[] toArray(T[] a) {
				int size = wrappedKeySet.size();
				T[] result = a;
				if (a.length < size) {
					result = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
				}
				int i = 0;
				for (IdentityWrapper<K> wrapper : wrappedKeySet) {
					result[i++] = (T) wrapper.unwrap();
				}
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj == this)
					return true;
				if (obj == null || !(obj instanceof Set))
					return false;
				Set<?> that = (Set<?>) obj;
				return this.size() == that.size() && containsAll(that);
			}

			@Override
			public int hashCode() {
				return wrappedKeySet.hashCode();
			}
		};
	}

	@Override
	public V put(K key, V value) {
		return wrappedMap.put(IdentityWrapper.wrap(key), value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> other) {
		for (Map.Entry<? extends K, ? extends V> entry : other.entrySet()) {
			K key = entry.getKey();
			V value = entry.getValue();
			wrappedMap.put(IdentityWrapper.wrap(key), value);
		}
	}

	@Override
	public V remove(Object key) {
		return wrappedMap.remove(IdentityWrapper.wrap(key));
	}

	@Override
	public int size() {
		return wrappedMap.size();
	}

	@Override
	public Collection<V> values() {
		return wrappedMap.values();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof Map))
			return false;
		Map<?, ?> that = (Map<?, ?>) obj;
		return this.entrySet().equals(that.entrySet());
	}

	@Override
	public int hashCode() {
		return wrappedMap.hashCode();
	}
}
