/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerGroupingEntry;

/**
 * @since 3.5
 * 
 * @author Hitesh
 */
public class MarkerSortUtil {
	
	/**
	 * Sorts [from,first+k-1] in the array of [from,to] using a variant of
	 * modified heapsort, such that
	 * array[from]<array[from+1]<...<array[from+k-1] and
	 * array[from+k-1]<arra[from+k||from+k+1||from+k+2|| ....to]
	 * 
	 * Note: if k is greater than a number,the sorting happens in batches of
	 * that number, this for performance reasons.
	 * 
	 * @param entries
	 * @param comparator
	 * @param from
	 * @param to
	 * @param k
	 */
	public static void sortStatingKElement(MarkerEntry[] entries,
			Comparator comparator, int from, int to, int k) {
		// check range valid
		int fromK = from + k - 1;
		if (entries.length == 0 || from < 0 || from >= to || fromK < from
				|| fromK > to || to > entries.length - 1 || to < 0)
			return;
		// use arrays sort
		Arrays.sort(entries, from, to, comparator);
			// clear cache for first to middle since we are done with sort
			for (int i = from; i <= to; i++) {
				entries[i].clearCache();
			}
		}
		
	/**
	 * Sorts [0,k-1] in the array of [0,entries.length-1] using a variant of
	 * modified heapsort, such that
	 * array[0]<array[1]<...<array[k-1] and
	 * array[k-1]<arra[k||k+1||k+2|| ....entries.length-1]
	 * 
	 * Note: if k is greater than a number,the sorting happens in batches of
	 * that number, this for performance reasons.
	 * 
	 * @param entries
	 * @param comparator
	 * @param k
	 */
	public static void sortStatingKElement(MarkerEntry[] entries,
			Comparator comparator, int k) {
		sortStatingKElement(entries, comparator, 0, entries.length - 1, k);
	}

	/**
	 * Sorts/groups the markers in O(N) comparisons and returns the boundary
	 * indices in the map. The O(N) complexity requires the use of a few data
	 * structures. But the speed benefit is tremendous at a very small price of
	 * few extra references.
	 * 
	 * @param entries
	 * @param group
	 * @param k
	 * @return {@link Map}
	 * 
	 */
	public static Map groupMarkerEnteries(MarkerEntry[] entries,
			MarkerGroup group, int k) {
		TreeMap map = new TreeMap(group.getEntriesComparator());
		for (int i = 0; i <= k; i++) {
			IMarker marker = entries[i].getMarker();
			if(marker == null || !marker.exists()) {
				continue;//skip stale markers
			}
			try {
				MarkerGroupingEntry groupingEntry = group.findGroupValue(marker
						.getType(), marker);
				List list = (List) map.get(groupingEntry);
				if (list == null) {
					list = new ArrayList();
					map.put(groupingEntry, list);
				}
				list.add(entries[i]);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		Iterator keys = map.keySet().iterator();
		int i = 0;
		while (keys.hasNext()) {
			Object key = keys.next();
			List list = (List) map.get(key);
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				MarkerEntry entry = (MarkerEntry) iterator.next();
				entries[i++] = entry;
			}
			map.put(key, new Integer(i - 1));
		}
		return map;
	}

}
