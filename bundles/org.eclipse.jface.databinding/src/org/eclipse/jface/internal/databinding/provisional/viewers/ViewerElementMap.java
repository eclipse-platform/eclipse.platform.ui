/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.provisional.viewers;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * A {@link Map} whose keys are elements in a {@link StructuredViewer}. The
 * keys in the map are compared using an {@link IElementComparer} instead of
 * {@link #equals(Object)}.
 * <p>
 * This class is <i>not</i> a strict implementation the {@link Map} interface.
 * It intentionally violates the {@link Map} contract, which requires the use of
 * {@link #equals(Object)} when comparing keys. This class is designed for use
 * with {@link StructuredViewer} which uses {@link IElementComparer} for element
 * comparisons.
 * 
 * @since 1.2
 */
public class ViewerElementMap implements Map {
	private Map wrappedMap;
	private IElementComparer comparer;

	/**
	 * Constructs a ViewerElementMap using the given {@link IElementComparer}.
	 * 
	 * @param comparer
	 *            the {@link IElementComparer} used for comparing keys.
	 */
	public ViewerElementMap(IElementComparer comparer) {
		Assert.isNotNull(comparer);
		this.wrappedMap = new HashMap();
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
	public ViewerElementMap(Map map, IElementComparer comparer) {
		this(comparer);
		Assert.isNotNull(map);
		putAll(map);
	}

	public void clear() {
		wrappedMap.clear();
	}

	public boolean containsKey(Object key) {
		return wrappedMap.containsKey(new Wrapper(key));
	}

	public boolean containsValue(Object value) {
		return wrappedMap.containsValue(value);
	}

	public Set entrySet() {
		final Set wrappedEntrySet = wrappedMap.entrySet();
		return new Set() {
			public boolean add(Object o) {
				throw new UnsupportedOperationException();
			}

			public boolean addAll(Collection c) {
				throw new UnsupportedOperationException();
			}

			public void clear() {
				wrappedEntrySet.clear();
			}

			public boolean contains(Object o) {
				for (Iterator iterator = iterator(); iterator.hasNext();)
					if (iterator.next().equals(o))
						return true;
				return false;
			}

			public boolean containsAll(Collection c) {
				for (Iterator iterator = c.iterator(); iterator.hasNext();)
					if (!contains(iterator.next()))
						return false;
				return true;
			}

			public boolean isEmpty() {
				return wrappedEntrySet.isEmpty();
			}

			public Iterator iterator() {
				final Iterator wrappedIterator = wrappedEntrySet.iterator();
				return new Iterator() {
					public boolean hasNext() {
						return wrappedIterator.hasNext();
					}

					public Object next() {
						final Map.Entry wrappedEntry = (Map.Entry) wrappedIterator
								.next();
						return new Map.Entry() {
							public Object getKey() {
								return ((Wrapper) wrappedEntry.getKey())
										.unwrap();
							}

							public Object getValue() {
								return wrappedEntry.getValue();
							}

							public Object setValue(Object value) {
								return wrappedEntry.setValue(value);
							}

							public boolean equals(Object obj) {
								if (obj == this)
									return true;
								if (obj == null || !(obj instanceof Map.Entry))
									return false;
								Map.Entry that = (Map.Entry) obj;
								return comparer.equals(this.getKey(), that
										.getKey())
										&& Util.equals(this.getValue(), that
												.getValue());
							}

							public int hashCode() {
								return wrappedEntry.hashCode();
							}
						};
					}

					public void remove() {
						wrappedIterator.remove();
					}
				};
			}

			public boolean remove(Object o) {
				final Map.Entry unwrappedEntry = (Map.Entry) o;
				return wrappedEntrySet.remove(new Map.Entry() {
					public Object getKey() {
						return new Wrapper(unwrappedEntry.getKey());
					}

					public Object getValue() {
						return unwrappedEntry.getValue();
					}

					public Object setValue(Object value) {
						throw new UnsupportedOperationException();
					}

					public boolean equals(Object obj) {
						throw new UnsupportedOperationException();
					}

					public int hashCode() {
						throw new UnsupportedOperationException();
					}
				});
			}

			public boolean removeAll(Collection c) {
				boolean changed = false;
				for (Iterator iterator = c.iterator(); iterator.hasNext();)
					changed |= remove(iterator.next());
				return changed;
			}

			public boolean retainAll(Collection c) {
				boolean changed = false;
				Object[] toRetain = c.toArray();
				outer: for (Iterator iterator = iterator(); iterator.hasNext();) {
					Object entry = iterator.next();
					for (int i = 0; i < toRetain.length; i++)
						if (entry.equals(toRetain[i]))
							continue outer;
					iterator.remove();
					changed = true;
				}
				return changed;
			}

			public int size() {
				return wrappedEntrySet.size();
			}

			public Object[] toArray() {
				return toArray(new Object[size()]);
			}

			public Object[] toArray(Object[] a) {
				int size = size();
				if (a.length < size) {
					a = (Object[]) Array.newInstance(a.getClass()
							.getComponentType(), size);
				}
				int i = 0;
				for (Iterator iterator = iterator(); iterator.hasNext();) {
					a[i] = iterator.next();
					i++;
				}
				return a;
			}

			public boolean equals(Object obj) {
				if (obj == this)
					return true;
				if (obj == null || !(obj instanceof Set))
					return false;
				Set that = (Set) obj;
				return this.size() == that.size() && containsAll(that);
			}

			public int hashCode() {
				return wrappedEntrySet.hashCode();
			}
		};
	}

	public Object get(Object key) {
		return wrappedMap.get(new Wrapper(key));
	}

	public boolean isEmpty() {
		return wrappedMap.isEmpty();
	}

	public Set keySet() {
		final Set wrappedKeySet = wrappedMap.keySet();
		return new Set() {
			public boolean add(Object o) {
				throw new UnsupportedOperationException();
			}

			public boolean addAll(Collection c) {
				throw new UnsupportedOperationException();
			}

			public void clear() {
				wrappedKeySet.clear();
			}

			public boolean contains(Object o) {
				return wrappedKeySet.contains(new Wrapper(o));
			}

			public boolean containsAll(Collection c) {
				for (Iterator iterator = c.iterator(); iterator.hasNext();)
					if (!wrappedKeySet.contains(new Wrapper(iterator.next())))
						return false;
				return true;
			}

			public boolean isEmpty() {
				return wrappedKeySet.isEmpty();
			}

			public Iterator iterator() {
				final Iterator wrappedIterator = wrappedKeySet.iterator();
				return new Iterator() {
					public boolean hasNext() {
						return wrappedIterator.hasNext();
					}

					public Object next() {
						return ((Wrapper) wrappedIterator.next()).unwrap();
					}

					public void remove() {
						wrappedIterator.remove();
					}
				};
			}

			public boolean remove(Object o) {
				return wrappedKeySet.remove(new Wrapper(o));
			}

			public boolean removeAll(Collection c) {
				boolean changed = false;
				for (Iterator iterator = c.iterator(); iterator.hasNext();)
					changed |= wrappedKeySet
							.remove(new Wrapper(iterator.next()));
				return changed;
			}

			public boolean retainAll(Collection c) {
				boolean changed = false;
				Object[] toRetain = c.toArray();
				outer: for (Iterator iterator = iterator(); iterator.hasNext();) {
					Object element = iterator.next();
					for (int i = 0; i < toRetain.length; i++)
						if (comparer.equals(element, toRetain[i]))
							continue outer;
					// element not contained in collection, remove.
					remove(element);
					changed = true;
				}
				return changed;
			}

			public int size() {
				return wrappedKeySet.size();
			}

			public Object[] toArray() {
				return toArray(new Object[wrappedKeySet.size()]);
			}

			public Object[] toArray(Object[] a) {
				int size = wrappedKeySet.size();
				Wrapper[] wrappedArray = (Wrapper[]) wrappedKeySet
						.toArray(new Wrapper[size]);
				Object[] result = a;
				if (a.length < size) {
					result = (Object[]) Array.newInstance(a.getClass()
							.getComponentType(), size);
				}
				for (int i = 0; i < size; i++)
					result[i] = wrappedArray[i].unwrap();
				return result;
			}

			public boolean equals(Object obj) {
				if (obj == this)
					return true;
				if (obj == null || !(obj instanceof Set))
					return false;
				Set that = (Set) obj;
				return this.size() == that.size() && containsAll(that);
			}

			public int hashCode() {
				return wrappedKeySet.hashCode();
			}
		};
	}

	public Object put(Object key, Object value) {
		return wrappedMap.put(new Wrapper(key), value);
	}

	public void putAll(Map other) {
		for (Iterator iterator = other.entrySet().iterator(); iterator
				.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			wrappedMap.put(new Wrapper(entry.getKey()), entry.getValue());
		}
	}

	public Object remove(Object key) {
		return wrappedMap.remove(new Wrapper(key));
	}

	public int size() {
		return wrappedMap.size();
	}

	public Collection values() {
		return wrappedMap.values();
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || !(obj instanceof Map))
			return false;
		Map that = (Map) obj;
		return this.entrySet().equals(that.entrySet());
	}

	public int hashCode() {
		return wrappedMap.hashCode();
	}

	class Wrapper {
		private final Object element;

		Wrapper(Object element) {
			this.element = element;
		}

		public boolean equals(Object obj) {
			return comparer.equals(element, ((Wrapper) obj).element);
		}

		public int hashCode() {
			return comparer.hashCode(element);
		}

		public Object unwrap() {
			return element;
		}
	}
}
