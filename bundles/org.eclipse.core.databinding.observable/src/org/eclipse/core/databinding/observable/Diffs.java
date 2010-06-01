/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 226216
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
	 * @param oldList
	 *            the old list state
	 * @param newList
	 *            the new list state
	 * @return the differences between oldList and newList
	 */
	public static ListDiff computeListDiff(List oldList, List newList) {
		List diffEntries = new ArrayList();
		createListDiffs(new ArrayList(oldList), newList, diffEntries);
		ListDiff listDiff = createListDiff((ListDiffEntry[]) diffEntries
				.toArray(new ListDiffEntry[diffEntries.size()]));
		return listDiff;
	}

	/**
	 * Returns a lazily computed {@link ListDiff} describing the change between
	 * the specified old and new list states.
	 * 
	 * @param oldList
	 *            the old list state
	 * @param newList
	 *            the new list state
	 * @return a lazily computed {@link ListDiff} describing the change between
	 *         the specified old and new list states.
	 * @since 1.3
	 */
	public static ListDiff computeLazyListDiff(final List oldList,
			final List newList) {
		return new ListDiff() {
			ListDiff lazyDiff;

			public ListDiffEntry[] getDifferences() {
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
	private static void createListDiffs(List oldList, List newList,
			List listDiffs) {
		int index = 0;
		for (Iterator it = newList.iterator(); it.hasNext();) {
			Object newValue = it.next();
			if (oldList.size() <= index) {
				// append newValue to newList
				listDiffs.add(createListDiffEntry(index, true, newValue));
			} else {
				boolean done;
				do {
					done = true;
					Object oldValue = oldList.get(index);
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
	private static int listIndexOf(List list, Object object, int index) {
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
	 * @param oldSet
	 *            the old set state
	 * @param newSet
	 *            the new set state
	 * @return a {@link SetDiff} describing the change between the specified old
	 *         and new set states.
	 */
	public static SetDiff computeSetDiff(Set oldSet, Set newSet) {
		Set additions = new HashSet(newSet);
		additions.removeAll(oldSet);
		Set removals = new HashSet(oldSet);
		removals.removeAll(newSet);
		return createSetDiff(additions, removals);
	}

	/**
	 * Returns a lazily computed {@link SetDiff} describing the change between
	 * the specified old and new set states.
	 * 
	 * @param oldSet
	 *            the old set state
	 * @param newSet
	 *            the new set state
	 * @return a lazily computed {@link SetDiff} describing the change between
	 *         the specified old and new set states.
	 * @since 1.3
	 */
	public static SetDiff computeLazySetDiff(final Set oldSet, final Set newSet) {
		return new SetDiff() {

			private SetDiff lazyDiff;

			private SetDiff getLazyDiff() {
				if (lazyDiff == null) {
					lazyDiff = computeSetDiff(oldSet, newSet);
				}
				return lazyDiff;
			}

			public Set getAdditions() {
				return getLazyDiff().getAdditions();
			}

			public Set getRemovals() {
				return getLazyDiff().getRemovals();
			}

		};
	}

	/**
	 * Returns a {@link MapDiff} describing the change between the specified old
	 * and new map states.
	 * 
	 * @param oldMap
	 *            the old map state
	 * @param newMap
	 *            the new map state
	 * @return a {@link MapDiff} describing the change between the specified old
	 *         and new map states.
	 */
	public static MapDiff computeMapDiff(Map oldMap, Map newMap) {
		// starts out with all keys from the new map, we will remove keys from
		// the old map as we go
		final Set addedKeys = new HashSet(newMap.keySet());
		final Set removedKeys = new HashSet();
		final Set changedKeys = new HashSet();
		final Map oldValues = new HashMap();
		final Map newValues = new HashMap();
		for (Iterator it = oldMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry oldEntry = (Entry) it.next();
			Object oldKey = oldEntry.getKey();
			if (addedKeys.remove(oldKey)) {
				// potentially changed key since it is in oldMap and newMap
				Object oldValue = oldEntry.getValue();
				Object newValue = newMap.get(oldKey);
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
		for (Iterator it = addedKeys.iterator(); it.hasNext();) {
			Object newKey = it.next();
			newValues.put(newKey, newMap.get(newKey));
		}
		return new MapDiff() {
			public Set getAddedKeys() {
				return addedKeys;
			}

			public Set getChangedKeys() {
				return changedKeys;
			}

			public Set getRemovedKeys() {
				return removedKeys;
			}

			public Object getNewValue(Object key) {
				return newValues.get(key);
			}

			public Object getOldValue(Object key) {
				return oldValues.get(key);
			}
		};
	}

	/**
	 * Returns a lazily computed {@link MapDiff} describing the change between
	 * the specified old and new map states.
	 * 
	 * @param oldMap
	 *            the old map state
	 * @param newMap
	 *            the new map state
	 * @return a lazily computed {@link MapDiff} describing the change between
	 *         the specified old and new map states.
	 * @since 1.3
	 */
	public static MapDiff computeLazyMapDiff(final Map oldMap, final Map newMap) {
		return new MapDiff() {

			private MapDiff lazyDiff;

			private MapDiff getLazyDiff() {
				if (lazyDiff == null) {
					lazyDiff = computeMapDiff(oldMap, newMap);
				}
				return lazyDiff;
			}

			public Set getAddedKeys() {
				return getLazyDiff().getAddedKeys();
			}

			public Set getRemovedKeys() {
				return getLazyDiff().getRemovedKeys();
			}

			public Set getChangedKeys() {
				return getLazyDiff().getChangedKeys();
			}

			public Object getOldValue(Object key) {
				return getLazyDiff().getOldValue(key);
			}

			public Object getNewValue(Object key) {
				return getLazyDiff().getNewValue(key);
			}

		};
	}

	/**
	 * @param oldValue
	 * @param newValue
	 * @return a value diff
	 */
	public static ValueDiff createValueDiff(final Object oldValue,
			final Object newValue) {
		return new ValueDiff() {

			public Object getOldValue() {
				return oldValue;
			}

			public Object getNewValue() {
				return newValue;
			}
		};
	}

	/**
	 * @param additions
	 * @param removals
	 * @return a set diff
	 */
	public static SetDiff createSetDiff(Set additions, Set removals) {
		final Set unmodifiableAdditions = Collections
				.unmodifiableSet(additions);
		final Set unmodifiableRemovals = Collections.unmodifiableSet(removals);
		return new SetDiff() {

			public Set getAdditions() {
				return unmodifiableAdditions;
			}

			public Set getRemovals() {
				return unmodifiableRemovals;
			}
		};
	}

	/**
	 * @param difference
	 * @return a list diff with one differing entry
	 */
	public static ListDiff createListDiff(ListDiffEntry difference) {
		return createListDiff(new ListDiffEntry[] { difference });
	}

	/**
	 * @param difference1
	 * @param difference2
	 * @return a list diff with two differing entries
	 */
	public static ListDiff createListDiff(ListDiffEntry difference1,
			ListDiffEntry difference2) {
		return createListDiff(new ListDiffEntry[] { difference1, difference2 });
	}

	/**
	 * @param differences
	 * @return a list diff with the given entries
	 */
	public static ListDiff createListDiff(final ListDiffEntry[] differences) {
		return new ListDiff() {
			public ListDiffEntry[] getDifferences() {
				return differences;
			}
		};
	}

	/**
	 * @param position
	 * @param isAddition
	 * @param element
	 * @return a list diff entry
	 */
	public static ListDiffEntry createListDiffEntry(final int position,
			final boolean isAddition, final Object element) {
		return new ListDiffEntry() {

			public int getPosition() {
				return position;
			}

			public boolean isAddition() {
				return isAddition;
			}

			public Object getElement() {
				return element;
			}
		};
	}

	/**
	 * @param addedKey
	 * @param newValue
	 * @return a map diff
	 */
	public static MapDiff createMapDiffSingleAdd(final Object addedKey,
			final Object newValue) {
		return new MapDiff() {

			public Set getAddedKeys() {
				return Collections.singleton(addedKey);
			}

			public Set getChangedKeys() {
				return Collections.EMPTY_SET;
			}

			public Object getNewValue(Object key) {
				return newValue;
			}

			public Object getOldValue(Object key) {
				return null;
			}

			public Set getRemovedKeys() {
				return Collections.EMPTY_SET;
			}
		};
	}

	/**
	 * @param existingKey
	 * @param oldValue
	 * @param newValue
	 * @return a map diff
	 */
	public static MapDiff createMapDiffSingleChange(final Object existingKey,
			final Object oldValue, final Object newValue) {
		return new MapDiff() {

			public Set getAddedKeys() {
				return Collections.EMPTY_SET;
			}

			public Set getChangedKeys() {
				return Collections.singleton(existingKey);
			}

			public Object getNewValue(Object key) {
				return newValue;
			}

			public Object getOldValue(Object key) {
				return oldValue;
			}

			public Set getRemovedKeys() {
				return Collections.EMPTY_SET;
			}
		};
	}

	/**
	 * @param removedKey
	 * @param oldValue
	 * @return a map diff
	 */
	public static MapDiff createMapDiffSingleRemove(final Object removedKey,
			final Object oldValue) {
		return new MapDiff() {

			public Set getAddedKeys() {
				return Collections.EMPTY_SET;
			}

			public Set getChangedKeys() {
				return Collections.EMPTY_SET;
			}

			public Object getNewValue(Object key) {
				return null;
			}

			public Object getOldValue(Object key) {
				return oldValue;
			}

			public Set getRemovedKeys() {
				return Collections.singleton(removedKey);
			}
		};
	}

	/**
	 * @param copyOfOldMap
	 * @return a map diff
	 */
	public static MapDiff createMapDiffRemoveAll(final Map copyOfOldMap) {
		return new MapDiff() {

			public Set getAddedKeys() {
				return Collections.EMPTY_SET;
			}

			public Set getChangedKeys() {
				return Collections.EMPTY_SET;
			}

			public Object getNewValue(Object key) {
				return null;
			}

			public Object getOldValue(Object key) {
				return copyOfOldMap.get(key);
			}

			public Set getRemovedKeys() {
				return copyOfOldMap.keySet();
			}
		};
	}

	/**
	 * @param addedKeys
	 * @param removedKeys
	 * @param changedKeys
	 * @param oldValues
	 * @param newValues
	 * @return a map diff
	 */
	public static MapDiff createMapDiff(final Set addedKeys,
			final Set removedKeys, final Set changedKeys, final Map oldValues,
			final Map newValues) {
		return new MapDiff() {

			public Set getAddedKeys() {
				return addedKeys;
			}

			public Set getChangedKeys() {
				return changedKeys;
			}

			public Object getNewValue(Object key) {
				return newValues.get(key);
			}

			public Object getOldValue(Object key) {
				return oldValues.get(key);
			}

			public Set getRemovedKeys() {
				return removedKeys;
			}
		};
	}
}
