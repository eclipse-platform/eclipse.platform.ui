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
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.viewers;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * A {@link Map} whose keys are elements in a {@link StructuredViewer}. The keys
 * in the map are compared using an {@link IElementComparer} instead of
 * {@link #equals(Object)}.
 * <p>
 * This class is <i>not</i> a strict implementation the {@link Map} interface.
 * It intentionally violates the {@link Map} contract, which requires the use of
 * {@link #equals(Object)} when comparing keys. This class is designed for use
 * with {@link StructuredViewer} which uses {@link IElementComparer} for element
 * comparisons.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @since 1.2
 */
public class ViewerElementMap<K, V> implements Map<K, V> {
	private Map<ViewerElementWrapper<K>, V> wrappedMap;
	private IElementComparer comparer;

	/**
	 * Constructs a ViewerElementMap using the given {@link IElementComparer}.
	 *
	 * @param comparer
	 *            the {@link IElementComparer} used for comparing keys.
	 */
	public ViewerElementMap(IElementComparer comparer) {
		Assert.isNotNull(comparer);
		this.wrappedMap = new HashMap<>();
		this.comparer = comparer;
	}

	/**
	 * Constructs a ViewerElementMap containing all the entries in the specified
	 * map.
	 *
	 * @param map
	 *            the map whose entries are to be added to this map.
	 * @param comparer
	 *            the {@link IElementComparer} used for comparing keys.
	 */
	public ViewerElementMap(Map<? extends K, ? extends V> map, IElementComparer comparer) {
		this(comparer);
		Assert.isNotNull(map);
		putAll(map);
	}

	@Override
	public void clear() {
		wrappedMap.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return wrappedMap.containsKey(new ViewerElementWrapper<>(key, comparer));
	}

	@Override
	public boolean containsValue(Object value) {
		return wrappedMap.containsValue(value);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		final Set<Entry<ViewerElementWrapper<K>, V>> wrappedEntrySet = wrappedMap.entrySet();
		return new Set<Entry<K, V>>() {
			@Override
			public boolean add(Entry<K, V> o) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean addAll(Collection<? extends Entry<K, V>> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				wrappedEntrySet.clear();
			}

			@Override
			public boolean contains(Object o) {
				for (Object element : this)
					if (element.equals(o))
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
			public Iterator<Entry<K, V>> iterator() {
				final Iterator<Entry<ViewerElementWrapper<K>, V>> wrappedIterator = wrappedEntrySet.iterator();
				return new Iterator<Entry<K, V>>() {
					@Override
					public boolean hasNext() {
						return wrappedIterator.hasNext();
					}

					@Override
					public Entry<K, V> next() {
						final Entry<ViewerElementWrapper<K>, V> wrappedEntry = wrappedIterator.next();
						return new Entry<K, V>() {
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
								if (obj == null || !(obj instanceof Map.Entry))
									return false;
								Entry<?, ?> that = (Entry<?, ?>) obj;
								return comparer.equals(this.getKey(), that.getKey())
										&& Objects.equals(this.getValue(), that.getValue());
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
				final Entry<?, ?> unwrappedEntry = (Entry<?, ?>) o;
				final ViewerElementWrapper<?> wrappedKey = new ViewerElementWrapper<>(unwrappedEntry.getKey(),
						comparer);
				Entry<Object, Object> wrappedEntry = new Entry<Object, Object>() {
					@Override
					public Object getKey() {
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
						Entry<?, ?> that = (Entry<?, ?>) obj;
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
				outer: for (Iterator<?> iterator = iterator(); iterator.hasNext();) {
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
					a[i] = (T) entry;
					i++;
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
		return wrappedMap.get(new ViewerElementWrapper<>(key, comparer));
	}

	@Override
	public boolean isEmpty() {
		return wrappedMap.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		final Set<ViewerElementWrapper<K>> wrappedKeySet = wrappedMap.keySet();
		return new Set<K>() {
			@Override
			public boolean add(Object o) {
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
				return wrappedKeySet.contains(new ViewerElementWrapper<>(o, comparer));
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				for (Object element : c)
					if (!wrappedKeySet.contains(new ViewerElementWrapper<>(element, comparer)))
						return false;
				return true;
			}

			@Override
			public boolean isEmpty() {
				return wrappedKeySet.isEmpty();
			}

			@Override
			public Iterator<K> iterator() {
				final Iterator<ViewerElementWrapper<K>> wrappedIterator = wrappedKeySet.iterator();
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
				return wrappedKeySet.remove(new ViewerElementWrapper<>(o, comparer));
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				boolean changed = false;
				for (Object element : c)
					changed |= wrappedKeySet.remove(new ViewerElementWrapper<>(element, comparer));
				return changed;
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				boolean changed = false;
				Object[] toRetains = c.toArray();
				outer: for (Object element : this) {
					for (Object toRetain : toRetains) {
						if (comparer.equals(element, toRetain)) {
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
				ViewerElementWrapper<T>[] wrappedArray = wrappedKeySet.toArray(new ViewerElementWrapper[size]);
				T[] result = a;
				if (a.length < size) {
					result = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
				}
				for (int i = 0; i < size; i++)
					result[i] = wrappedArray[i].unwrap();
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
		return wrappedMap.put(new ViewerElementWrapper<>(key, comparer), value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> other) {
		for (Entry<? extends K, ? extends V> entry : other.entrySet()) {
			wrappedMap.put(new ViewerElementWrapper<>(entry.getKey(), comparer), entry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		return wrappedMap.remove(new ViewerElementWrapper<>(key, comparer));
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
		if (!(obj instanceof Map))
			return false;
		Map<?, ?> that = (Map<?, ?>) obj;
		return this.entrySet().equals(that.entrySet());
	}

	@Override
	public int hashCode() {
		return wrappedMap.hashCode();
	}

	/**
	 * Returns a Map for mapping viewer elements as keys to values, using the
	 * given {@link IElementComparer} for key comparisons.
	 *
	 * @param comparer
	 *            the element comparer to use in key comparisons. If null, the
	 *            returned map will compare keys according to the standard
	 *            contract for {@link Map} interface contract.
	 * @return a Map for mapping viewer elements as keys to values, using the
	 *         given {@link IElementComparer} for key comparisons.
	 */
	public static <K, V> Map<K, V> withComparer(IElementComparer comparer) {
		if (comparer == null)
			return new HashMap<>();
		return new ViewerElementMap<>(comparer);
	}
}
