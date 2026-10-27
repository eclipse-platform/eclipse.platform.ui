/*******************************************************************************
 * Copyright (c) 2000, 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.Comparator;
import org.eclipse.core.runtime.Assert;

/**
 * Stable sort to sort key-value pairs. The keys and values are specified in
 * separate arrays. Elements with equal keys keep their relative order.
 *
 * @since 2.0
 */
/* package */class TwoArrayQuickSorter {

	private final Comparator fComparator;

	/**
	 * Default comparator.
	 */
	public static final class StringComparator implements Comparator<String> {
		private final boolean fIgnoreCase;

		StringComparator(boolean ignoreCase) {
			fIgnoreCase = ignoreCase;
		}

		@Override
		public int compare(String left, String right) {
			return fIgnoreCase ? left.compareToIgnoreCase(right)
					: left.compareTo(right);
		}
	}

	/**
	 * Creates a sorter with default string comparator. The keys are assumed to be
	 * strings.
	 *
	 * @param ignoreCase specifies whether sorting is case sensitive or not.
	 */
	public TwoArrayQuickSorter(boolean ignoreCase) {
		fComparator = new StringComparator(ignoreCase);
	}

	/**
	 * Creates a sorter with a comparator.
	 *
	 * @param comparator the comparator to order the elements. The comparator must
	 *                   not be <code>null</code>.
	 */
	public TwoArrayQuickSorter(Comparator comparator) {
		fComparator = comparator;
	}

	/**
	 * Sorts keys and values in parallel. The sort is stable: values whose keys
	 * compare as equal keep their relative order.
	 *
	 * @param keys   the keys to use for sorting.
	 * @param values the values associated with the keys.
	 */
	public void sort(Object[] keys, Object[] values) {
		if ((keys == null) || (values == null)) {
			Assert.isTrue(false, "Either keys or values in null"); //$NON-NLS-1$
			return;
		}

		if (keys.length <= 1) {
			return;
		}

		mergeSort(keys, values, new Object[keys.length / 2], new Object[keys.length / 2], 0, keys.length);
	}

	/*
	 * Sorts keys[low, high) and values[low, high). The buffers must hold at least
	 * half of the elements; only the left run is copied into them when merging.
	 */
	private void mergeSort(Object[] keys, Object[] values, Object[] keyBuffer, Object[] valueBuffer, int low,
			int high) {
		if (high - low < 2) {
			return;
		}

		int mid = (low + high) >>> 1;
		mergeSort(keys, values, keyBuffer, valueBuffer, low, mid);
		mergeSort(keys, values, keyBuffer, valueBuffer, mid, high);

		if (fComparator.compare(keys[mid - 1], keys[mid]) <= 0) {
			return; // already in order
		}

		int leftCount = mid - low;
		System.arraycopy(keys, low, keyBuffer, 0, leftCount);
		System.arraycopy(values, low, valueBuffer, 0, leftCount);
		int left = 0;
		int right = mid;
		int target = low;
		while (left < leftCount && right < high) {
			// take from the right run only if strictly smaller, to keep the sort stable
			if (fComparator.compare(keys[right], keyBuffer[left]) < 0) {
				keys[target] = keys[right];
				values[target++] = values[right++];
			} else {
				keys[target] = keyBuffer[left];
				values[target++] = valueBuffer[left++];
			}
		}
		// what is left of the right run is already in its place
		while (left < leftCount) {
			keys[target] = keyBuffer[left];
			values[target++] = valueBuffer[left++];
		}
	}
}
