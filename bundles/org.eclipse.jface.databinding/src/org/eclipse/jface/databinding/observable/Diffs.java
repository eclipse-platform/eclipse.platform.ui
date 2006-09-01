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

package org.eclipse.jface.databinding.observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.databinding.observable.list.ListDiff;
import org.eclipse.jface.databinding.observable.list.ListDiffEntry;
import org.eclipse.jface.databinding.observable.set.SetDiff;
import org.eclipse.jface.databinding.observable.value.ValueDiff;

/**
 * @since 1.0
 * 
 */
public class Diffs {

	/**
	 * @param oldList
	 * @param newList
	 * @return the differences between oldList and newList
	 */
	public static ListDiff computeListDiff(List oldList, List newList) {
		List diffEntries = new ArrayList();
		for (Iterator it = oldList.iterator(); it.hasNext();) {
			Object oldElement = it.next();
			diffEntries.add(createListDiffEntry(0, false, oldElement));
		}
		int i = 0;
		for (Iterator it = newList.iterator(); it.hasNext();) {
			Object newElement = it.next();
			diffEntries.add(createListDiffEntry(i++, true, newElement));
		}
		ListDiff listDiff = createListDiff((ListDiffEntry[]) diffEntries
				.toArray(new ListDiffEntry[diffEntries.size()]));
		return listDiff;
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
	 * @param oldSet
	 * @param newSet
	 * @return a set diff
	 */
	public static SetDiff computeSetDiff(Set oldSet, Set newSet) {
		Set additions = new HashSet(newSet);
		additions.removeAll(oldSet);
		Set removals = new HashSet(oldSet);
		removals.removeAll(newSet);
		return createSetDiff(additions, removals);
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

}
