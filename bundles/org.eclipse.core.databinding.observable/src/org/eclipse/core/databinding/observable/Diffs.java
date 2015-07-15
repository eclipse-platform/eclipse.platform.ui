/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 226216
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.internal.databinding.observable.Util;

/**
 * @since 1.0
 *
 */
public class Diffs {

	/**
	 * Returns a {@link ListDiff} describing the change between the specified
	 * old and new list states.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @param oldList
	 *            the old list state
	 * @param newList
	 *            the new list state
	 * @return the differences between oldList and newList
	 */
	public static <E> ListDiff<E> computeListDiff(List<? extends E> oldList, List<? extends E> newList) {
		List<ListDiffEntry<E>> diffEntries = new ArrayList<>();
		createListDiffs(new ArrayList<E>(oldList), newList, diffEntries);
		ListDiff<E> listDiff = createListDiff(diffEntries);
		return listDiff;
	}

	/**
	 * Returns a lazily computed {@link ListDiff} describing the change between
	 * the specified old and new list states.
	 *
	 * @param <E>
	 *            the list element type
	 *
	 * @param oldList
	 *            the old list state
	 * @param newList
	 *            the new list state
	 * @return a lazily computed {@link ListDiff} describing the change between
	 *         the specified old and new list states.
	 * @since 1.3
	 */
	public static <E> ListDiff<E> computeLazyListDiff(final List<? extends E> oldList,
			final List<? extends E> newList) {
		return new ListDiff<E>() {
			ListDiff<E> lazyDiff;

			@Override
			public ListDiffEntry<E>[] getDifferences() {
				if (lazyDiff == null) {
					lazyDiff = Diffs.computeListDiff(oldList, newList);
				}
				return lazyDiff.getDifferences();
			}
		};
	}

	/**
	 * adapted from EMF's ListDifferenceAnalyzer
	 */
	private static <E> void createListDiffs(List<E> oldList, List<? extends E> newList,
			List<ListDiffEntry<E>> listDiffs) {
		int index = 0;
		for (Iterator<? extends E> it = newList.iterator(); it.hasNext();) {
			E newValue = it.next();
			if (oldList.size() <= index) {
				// append newValue to newList
				listDiffs.add(createListDiffEntry(index, true, newValue));
			} else {
				boolean done;
				do {
					done = true;
					E oldValue = oldList.get(index);
					if (oldValue == null ? newValue != null : !oldValue
							.equals(newValue)) {
						int oldIndexOfNewValue = listIndexOf(oldList, newValue,
								index);
						if (oldIndexOfNewValue != -1) {
							int newIndexOfOldValue = listIndexOf(newList,
									oldValue, index);
							if (newIndexOfOldValue == -1) {
								// removing oldValue from list[index]
								listDiffs.add(createListDiffEntry(index, false,
										oldValue));
								oldList.remove(index);
								done = false;
							} else if (newIndexOfOldValue > oldIndexOfNewValue) {
								// moving oldValue from list[index] to
								// [newIndexOfOldValue]
								if (oldList.size() <= newIndexOfOldValue) {
									// The element cannot be moved to the
									// correct index
									// now, however later iterations will insert
									// elements
									// in front of it, eventually moving it into
									// the
									// correct spot.
									newIndexOfOldValue = oldList.size() - 1;
								}
								listDiffs.add(createListDiffEntry(index, false,
										oldValue));
								oldList.remove(index);
								listDiffs.add(createListDiffEntry(
										newIndexOfOldValue, true, oldValue));
								oldList.add(newIndexOfOldValue, oldValue);
								done = false;
							} else {
								// move newValue from list[oldIndexOfNewValue]
								// to [index]
								listDiffs.add(createListDiffEntry(
										oldIndexOfNewValue, false, newValue));
								oldList.remove(oldIndexOfNewValue);
								listDiffs.add(createListDiffEntry(index, true,
										newValue));
								oldList.add(index, newValue);
							}
						} else {
							// add newValue at list[index]
							oldList.add(index, newValue);
							listDiffs.add(createListDiffEntry(index, true,
									newValue));
						}
					}
				} while (!done);
			}
			++index;
		}
		for (int i = oldList.size(); i > index;) {
			// remove excess trailing elements not present in newList
			listDiffs.add(createListDiffEntry(--i, false, oldList.get(i)));
		}
	}

	/**
	 * @param list
	 * @param object
	 * @param index
	 * @return the index, or -1 if not found
	 */
	private static <E> int listIndexOf(List<E> list, Object object, int index) {
		int size = list.size();
		for (int i = index; i < size; i++) {
			Object candidate = list.get(i);
			if (candidate == null ? object == null : candidate.equals(object)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Checks whether the two objects are <code>null</code> -- allowing for
	 * <code>null</code>.
	 *
	 * @param left
	 *            The left object to compare; may be <code>null</code>.
	 * @param right
	 *            The right object to compare; may be <code>null</code>.
	 * @return <code>true</code> if the two objects are equivalent;
	 *         <code>false</code> otherwise.
	 */
	public static final boolean equals(final Object left, final Object right) {
		return left == null ? right == null : ((right != null) && left
				.equals(right));
	}

	/**
	 * Returns a {@link SetDiff} describing the change between the specified old
	 * and new set states.
	 *
	 * @param <E>
	 *            the set element type
	 *
	 * @param oldSet
	 *            the old set state
	 * @param newSet
	 *            the new set state
	 * @return a {@link SetDiff} describing the change between the specified old
	 *         and new set states.
	 */
	public static <E> SetDiff<E> computeSetDiff(Set<? extends E> oldSet, Set<? extends E> newSet) {
		Set<E> additions = new HashSet<E>(newSet);
		additions.removeAll(oldSet);
		Set<E> removals = new HashSet<E>(oldSet);
		removals.removeAll(newSet);
		return createSetDiff(additions, removals);
	}

	/**
	 * Returns a lazily computed {@link SetDiff} describing the change between
	 * the specified old and new set states.
	 *
	 * @param <E>
	 *            the set element type
	 *
	 * @param oldSet
	 *            the old set state
	 * @param newSet
	 *            the new set state
	 * @return a lazily computed {@link SetDiff} describing the change between
	 *         the specified old and new set states.
	 * @since 1.3
	 */
	public static <E> SetDiff<E> computeLazySetDiff(final Set<? extends E> oldSet, final Set<? extends E> newSet) {
		return new SetDiff<E>() {

			private SetDiff<E> lazyDiff;

			private SetDiff<E> getLazyDiff() {
				if (lazyDiff == null) {
					lazyDiff = computeSetDiff(oldSet, newSet);
				}
				return lazyDiff;
			}

			@Override
			public Set<E> getAdditions() {
				return getLazyDiff().getAdditions();
			}

			@Override
			public Set<E> getRemovals() {
				return getLazyDiff().getRemovals();
			}

		};
	}

	/**
	 * Returns a {@link MapDiff} describing the change between the specified old
	 * and new map states.
	 *
	 * @param <K>
	 *            the type of keys maintained by this map
	 * @param <V>
	 *            the type of mapped values
	 * @param oldMap
	 *            the old map state
	 * @param newMap
	 *            the new map state
	 * @return a {@link MapDiff} describing the change between the specified old
	 *         and new map states.
	 */
	public static <K, V> MapDiff<K, V> computeMapDiff(Map<? extends K, ? extends V> oldMap,
			Map<? extends K, ? extends V> newMap) {
		// starts out with all keys from the new map, we will remove keys from
		// the old map as we go
		final Set<K> addedKeys = new HashSet<K>(newMap.keySet());
		final Set<K> removedKeys = new HashSet<K>();
		final Set<K> changedKeys = new HashSet<K>();
		final Map<K, V> oldValues = new HashMap<K, V>();
		final Map<K, V> newValues = new HashMap<K, V>();
		for (Entry<? extends K, ? extends V> oldEntry : oldMap.entrySet()) {
			K oldKey = oldEntry.getKey();
			if (addedKeys.remove(oldKey)) {
				// potentially changed key since it is in oldMap and newMap
				V oldValue = oldEntry.getValue();
				V newValue = newMap.get(oldKey);
				if (!Util.equals(oldValue, newValue)) {
					changedKeys.add(oldKey);
					oldValues.put(oldKey, oldValue);
					newValues.put(oldKey, newValue);
				}
			} else {
				removedKeys.add(oldKey);
				oldValues.put(oldKey, oldEntry.getValue());
			}
		}
		for (Iterator<K> it = addedKeys.iterator(); it.hasNext();) {
			K newKey = it.next();
			newValues.put(newKey, newMap.get(newKey));
		}
		return new MapDiff<K, V>() {
			@Override
			public Set<K> getAddedKeys() {
				return addedKeys;
			}

			@Override
			public Set<K> getChangedKeys() {
				return changedKeys;
			}

			@Override
			public Set<K> getRemovedKeys() {
				return removedKeys;
			}

			@Override
			public V getNewValue(Object key) {
				return newValues.get(key);
			}

			@Override
			public V getOldValue(Object key) {
				return oldValues.get(key);
			}
		};
	}

	/**
	 * Returns a lazily computed {@link MapDiff} describing the change between
	 * the specified old and new map states.
	 *
	 * @param <K>
	 *            the type of keys maintained by this map
	 * @param <V>
	 *            the type of mapped values
	 * @param oldMap
	 *            the old map state
	 * @param newMap
	 *            the new map state
	 * @return a lazily computed {@link MapDiff} describing the change between
	 *         the specified old and new map states.
	 * @since 1.3
	 */
	public static <K, V> MapDiff<K, V> computeLazyMapDiff(final Map<? extends K, ? extends V> oldMap,
			final Map<? extends K, ? extends V> newMap) {
		return new MapDiff<K, V>() {

			private MapDiff<K, V> lazyDiff;

			private MapDiff<K, V> getLazyDiff() {
				if (lazyDiff == null) {
					lazyDiff = computeMapDiff(oldMap, newMap);
				}
				return lazyDiff;
			}

			@Override
			public Set<? extends K> getAddedKeys() {
				return getLazyDiff().getAddedKeys();
			}

			@Override
			public Set<? extends K> getRemovedKeys() {
				return getLazyDiff().getRemovedKeys();
			}

			@Override
			public Set<? extends K> getChangedKeys() {
				return getLazyDiff().getChangedKeys();
			}

			@Override
			public V getOldValue(Object key) {
				return getLazyDiff().getOldValue(key);
			}

			@Override
			public V getNewValue(Object key) {
				return getLazyDiff().getNewValue(key);
			}

		};
	}

	/**
	 * Creates a diff between two values
	 *
	 * @param <T>
	 *            the value type
	 * @param oldValue
	 * @param newValue
	 * @return a value diff
	 */
	public static <T> ValueDiff<T> createValueDiff(final T oldValue, final T newValue) {
		return new ValueDiff<T>() {

			@Override
			public T getOldValue() {
				return oldValue;
			}

			@Override
			public T getNewValue() {
				return newValue;
			}
		};
	}

	/**
	 * @param <E>
	 *            the set element type
	 * @param additions
	 * @param removals
	 * @return a set diff
	 */
	public static <E> SetDiff<E> createSetDiff(Set<? extends E> additions, Set<? extends E> removals) {
		final Set<E> unmodifiableAdditions = Collections
				.unmodifiableSet(additions);
		final Set<E> unmodifiableRemovals = Collections
				.unmodifiableSet(removals);
		return new SetDiff<E>() {

			@Override
			public Set<E> getAdditions() {
				return unmodifiableAdditions;
			}

			@Override
			public Set<E> getRemovals() {
				return unmodifiableRemovals;
			}
		};
	}

	/**
	 * @param <E>
	 *            the list element type
	 * @param difference
	 * @return a list diff with one differing entry
	 */
	public static <E> ListDiff<E> createListDiff(ListDiffEntry<E> difference) {
		return createListDiff(Collections.singletonList(difference));
	}

	/**
	 * @param <E>
	 *            the list element type
	 * @param difference1
	 * @param difference2
	 * @return a list diff with two differing entries
	 */
	public static <E> ListDiff<E> createListDiff(ListDiffEntry<E> difference1,
			ListDiffEntry<E> difference2) {
		List<ListDiffEntry<E>> differences = new ArrayList<>(2);
		differences.add(difference1);
		differences.add(difference2);
		return createListDiff(differences);
	}

	/**
	 * Creates a new ListDiff object given its constituent ListDiffEntry
	 * objects.
	 * <p>
	 * This form cannot be used in a type-safe manner because it is not possible
	 * to construct an array of generic types in a type-safe manner. Use the
	 * form below which takes a properly parameterized List.
	 *
	 * @param <E>
	 *            the list element type
	 * @param differences
	 * @return a list diff with the given entries
	 */
	public static <E> ListDiff<E> createListDiff(final ListDiffEntry<E>[] differences) {
		return new ListDiff<E>() {
			@Override
			public ListDiffEntry<E>[] getDifferences() {
				return differences;
			}
		};
	}

	/**
	 * Creates a new ListDiff object given its constituent ListDiffEntry
	 * objects.
	 *
	 * @param <E>
	 *            the list element type
	 * @param differences
	 * @return a list diff with the given entries
	 * @since 1.6
	 */
	public static <E> ListDiff<E> createListDiff(final List<ListDiffEntry<E>> differences) {
		final ListDiffEntry<E>[] differencesArray = differences.toArray(new ListDiffEntry[differences.size()]);
		return new ListDiff<E>() {
			@Override
			public ListDiffEntry<E>[] getDifferences() {
				return differencesArray;
			}
		};
	}

	/**
	 * @param <E>
	 *            the list element type
	 * @param position
	 * @param isAddition
	 * @param element
	 * @return a list diff entry
	 */
	public static <E> ListDiffEntry<E> createListDiffEntry(final int position,
			final boolean isAddition, final E element) {
		return new ListDiffEntry<E>() {

			@Override
			public int getPosition() {
				return position;
			}

			@Override
			public boolean isAddition() {
				return isAddition;
			}

			@Override
			public E getElement() {
				return element;
			}
		};
	}

	/**
	 * Creates a MapDiff representing the addition of a single added key
	 *
	 * @param <K>
	 *            the type of keys maintained by this map
	 * @param <V>
	 *            the type of mapped values
	 * @param addedKey
	 * @param newValue
	 * @return a map diff
	 */
	public static <K, V> MapDiff<K, V> createMapDiffSingleAdd(final K addedKey,
			final V newValue) {
		return new MapDiff<K, V>() {

			@Override
			public Set<K> getAddedKeys() {
				return Collections.singleton(addedKey);
			}

			@Override
			public Set<K> getChangedKeys() {
				return Collections.emptySet();
			}

			@Override
			public V getNewValue(Object key) {
				return newValue;
			}

			@Override
			public V getOldValue(Object key) {
				return null;
			}

			@Override
			public Set<K> getRemovedKeys() {
				return Collections.emptySet();
			}
		};
	}

	/**
	 * @param <K>
	 *            the type of keys maintained by this map
	 * @param <V>
	 *            the type of mapped values
	 * @param existingKey
	 * @param oldValue
	 * @param newValue
	 * @return a map diff
	 */
	public static <K, V> MapDiff<K, V> createMapDiffSingleChange(
			final K existingKey, final V oldValue, final V newValue) {
		return new MapDiff<K, V>() {

			@Override
			public Set<K> getAddedKeys() {
				return Collections.emptySet();
			}

			@Override
			public Set<K> getChangedKeys() {
				return Collections.singleton(existingKey);
			}

			@Override
			public V getNewValue(Object key) {
				return newValue;
			}

			@Override
			public V getOldValue(Object key) {
				return oldValue;
			}

			@Override
			public Set<K> getRemovedKeys() {
				return Collections.emptySet();
			}
		};
	}

	/**
	 * @param <K>
	 *            the type of keys maintained by this map
	 * @param <V>
	 *            the type of mapped values
	 * @param removedKey
	 * @param oldValue
	 * @return a map diff
	 */
	public static <K, V> MapDiff<K, V> createMapDiffSingleRemove(
			final K removedKey, final V oldValue) {
		return new MapDiff<K, V>() {

			@Override
			public Set<K> getAddedKeys() {
				return Collections.emptySet();
			}

			@Override
			public Set<K> getChangedKeys() {
				return Collections.emptySet();
			}

			@Override
			public V getNewValue(Object key) {
				return null;
			}

			@Override
			public V getOldValue(Object key) {
				return oldValue;
			}

			@Override
			public Set<K> getRemovedKeys() {
				return Collections.singleton(removedKey);
			}
		};
	}

	/**
	 * @param <K>
	 *            the type of keys maintained by this map
	 * @param <V>
	 *            the type of mapped values
	 * @param copyOfOldMap
	 * @return a map diff
	 */
	public static <K, V> MapDiff<K, V> createMapDiffRemoveAll(
			final Map<K, V> copyOfOldMap) {
		return new MapDiff<K, V>() {

			@Override
			public Set<K> getAddedKeys() {
				return Collections.emptySet();
			}

			@Override
			public Set<K> getChangedKeys() {
				return Collections.emptySet();
			}

			@Override
			public V getNewValue(Object key) {
				return null;
			}

			@Override
			public V getOldValue(Object key) {
				return copyOfOldMap.get(key);
			}

			@Override
			public Set<K> getRemovedKeys() {
				return copyOfOldMap.keySet();
			}
		};
	}

	/**
	 * @param <K>
	 *            the type of keys maintained by this map
	 * @param <V>
	 *            the type of mapped values
	 * @param addedKeys
	 * @param removedKeys
	 * @param changedKeys
	 * @param oldValues
	 * @param newValues
	 * @return a map diff
	 */
	public static <K, V> MapDiff<K, V> createMapDiff(final Set<? extends K> addedKeys,
			final Set<? extends K> removedKeys, final Set<? extends K> changedKeys,
			final Map<? extends K, ? extends V> oldValues, final Map<? extends K, ? extends V> newValues) {
		return new MapDiff<K, V>() {

			@Override
			public Set<? extends K> getAddedKeys() {
				return addedKeys;
			}

			@Override
			public Set<? extends K> getChangedKeys() {
				return changedKeys;
			}

			@Override
			public V getNewValue(Object key) {
				return newValues.get(key);
			}

			@Override
			public V getOldValue(Object key) {
				return oldValues.get(key);
			}

			@Override
			public Set<? extends K> getRemovedKeys() {
				return removedKeys;
			}
		};
	}
}
