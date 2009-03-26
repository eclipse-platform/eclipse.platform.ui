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
class MarkerSortUtil {
	
	/*
	 * Note: partial quicksort or introsort would not be of much use here as the
	 * sorting direction can be reversed easily from the UI. These would perform
	 * at quadratic complexities in these conditions. The code below is based on
	 * a variant of Modified HeapSort.Runs in O(NlogN) in worst case.
	 */

	/*
	 * Increasing BATCH_SIZE increases memory consumption but increases speed,
	 * and vice-versa.This indirectly controls the number of active caches in
	 * MarkerEntry[] array passed for sorting.
	 */
	private static int BATCH_SIZE = 10000;

	/*
	 * For n/k ratios less than this , we will use Arrays.Sort(). The heapsort
	 * performs nearly as good as mergesort for small data.We can still benefit 
	 * from the mergesort - Arrays.Sort(). When the number of elements to be sorted,
	 * are almost as much as the elements we have. 
	 */
	private static float MERGE_OR_HEAP_SWITCH=1.5f;

	/**
	 * Sorts [first,middle] in the array of [first,last] using a variant of
	 * modified heapsort, such that
	 * array[first]<array[first+1]<...<array[middle] and
	 * array[middle]<arra[middle+1||middle+2|| ....last]
	 * 
	 * @param array
	 * @param first
	 * @param middle
	 * @param last
	 * @param comparator
	 */
	private static void partiallySort(MarkerEntry[] array, int first,
			int middle, int last, Comparator comparator) {
		heapify(array, first, middle, comparator);

		/*
		 * we do not clear caches for heap elements when re-adjusting and
		 * sorting this will ensure sorting remains fast
		 */
		int current = middle;
		while (current <= last) {
			if (comparator.compare(array[current], array[first]) < 0) {
				MarkerEntry tmp = array[current];
				array[current] = array[first];
				array[first] = tmp;
				adjustHeap(array, first, first, middle, comparator);
			}
			// clear cache of the one not in heap
			array[current].clearCache();

			++current;
		}
		
		heapToSortedArray(array, first, middle, comparator);
	}

	/**
	 *  Re-adjust the elements in the heap to maintain heap-property
	 *  
	 *  Note: caches are not cleared in this method, as it would offset
	 *  to a certain extent the benefit of caching in sorting.
	 *  
	 * @param array
	 * @param first
	 * @param position
	 * @param last
	 * @param comparator
	 */
	private static void adjustHeap(MarkerEntry[] array, int first,
			int position, int last, Comparator comparator) {
		MarkerEntry tmp = array[position];
		int len = last - first;
		int holeIndex = position - first;
		int secondChild = 2 * holeIndex + 2;

		// percolate down the Heap: adjust left ,right, self nodes for heap 
		// starting from hole all the way down the heap
		while (secondChild < len) {
			if (comparator.compare(array[first + secondChild], array[first
					+ (secondChild - 1)]) < 0)
				--secondChild;
			array[first + holeIndex] = array[first + secondChild];
			holeIndex = secondChild++;
			secondChild *= 2;
		}
		if (secondChild-- == len) {
			array[first + holeIndex] = array[first + secondChild];
			holeIndex = secondChild;
		}

		// percolate up the Heap:  add the hole element back to heap, 
		// at the right position, all the way up the heap
		int parent = (holeIndex - 1) / 2;
		int topIndex = position - first;
		
		while (holeIndex != topIndex
				&& (comparator.compare(array[first + parent], tmp) < 0)) {
			array[first + holeIndex] = array[first + parent];
			holeIndex = parent;
			parent = (holeIndex - 1) / 2;
		}
		array[first + holeIndex] = tmp;
		
		
		/* 
		 * Using Binary search to locate the parent to replace.
		 * This is worse compared to linear search as most of the 
		 * holes would replace only a few parents above them.
		 * This code has been left commented for future examination.
		 * */		
	    /*
		int topIndex = position - first;
		int lowParent = 1;
		int highParent = 0;
		int fromIndx = holeIndex;
		while (fromIndx > topIndex) {
			fromIndx = (fromIndx - 1) / 2;
			highParent++;
		}
		if (fromIndx != topIndex)
			highParent--;// Between, exculding topIndex//n++;

		int parentReplaceCount = -1;
		// start at lower ranges because of heap property
		int midParent = lowParent + (highParent - lowParent) / 4;
		while (lowParent <= highParent) {
			// mth parent
			int currenParentIndex = holeIndex;
			int mth = midParent;
			while (mth > 0) {
				currenParentIndex = (currenParentIndex - 1) / 2;
				mth--;
			}
			int value = comparator.compare(array[first + currenParentIndex], tmp);
			if (value < 0) {
				parentReplaceCount = midParent;
				lowParent = midParent + 1;
			} else if (value > 0) {
				highParent = midParent - 1;
			} else {
				// we are looking for just lower
				// not exact match
				highParent = midParent - 1;
			}
			midParent = (highParent + lowParent) / 2;
		}
		int parent = (holeIndex - 1) / 2;
		for (int i = 1; i <= parentReplaceCount; i++) {
			array[first + holeIndex] = array[first + parent];
			holeIndex = parent;
			parent = (holeIndex - 1) / 2;
		}
		array[first + holeIndex] = tmp;
		*/

	}

	/**
	 * Makes a heap in the array
	 * @param array
	 * @param first
	 * @param last
	 * @param comparator
	 */
	private static void heapify(MarkerEntry[] array, int first, int last,
			Comparator comparator) {
		if (last - first < 2)
			return;
		int parent = (last - first - 2) / 2;
		do
			adjustHeap(array, first, first + parent, last, comparator);
		while (parent-- != 0);
	}

	/**
	 * Converts a heap array to sorted array
	 *
	 * @param array
	 * @param first
	 * @param last
	 * @param comparator
	 * 
	 */
	private static void heapToSortedArray(MarkerEntry[] array, int first,
			int last, Comparator comparator) {
		//TODO:Use mergesort to convert the heap to sorted array?
		
		while (last - first > 1) {
			// clear cache sorted and present at the end
			array[last].clearCache();
			// leave out the max elements at the end
			MarkerEntry tmp = array[--last];
			array[last] = array[first];
			array[first] = tmp;
			// readjust for next max
			adjustHeap(array, first, first, last, comparator);
		}
		array[first+1].clearCache();
		array[first].clearCache();
	}

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
		int n=to-from;
		if(n<=BATCH_SIZE&&(((float)n/k)<=MERGE_OR_HEAP_SWITCH)){
			//use arrays sort
			Arrays.sort(entries,from,to,comparator);
			// clear cache for first to middle since we are done with sort
			for (int i = from; i <= to; i++) {
				entries[i].clearCache();
			}
			return;
		}
		
		// do it in blocks of BATCH_SIZE so we get a chance
		// of clearing caches to keep memory usage to a minimum

		int totalBatches = k / BATCH_SIZE;
		int batchCount = 0;
		while (totalBatches > 0) {
			int fromTemp = from + batchCount * BATCH_SIZE;
			int toTemp = from + (batchCount + 1) * BATCH_SIZE - 1;
			partiallySort(entries, fromTemp, toTemp, to, comparator);
			batchCount++;
			totalBatches--;
		}

		if (fromK > from + batchCount * BATCH_SIZE) {
			//the last remaining enteries
			partiallySort(entries, from + batchCount * BATCH_SIZE, fromK, to,
					comparator);
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
	 * 
	 * Sorts [from,first+k-1] in the array of [from,entries.length-1] using a variant of
	 * modified heapsort, such that
	 * array[from]<array[from+1]<...<array[from+k-1] and
	 * array[from+k-1]<arra[from+k||from+k+1||from+k+2|| ....entries.length-1]
	 * 
	 * Note: if k is greater than a number,the sorting happens in batches of
	 * that number, this for performance reasons.
	 *
	 * @param entries
	 * @param comparator
	 * @param from
	 * @param k
	 */
	public static void sortStatingKElement(MarkerEntry[] entries,
			Comparator comparator, int from, int k) {
		sortStatingKElement(entries, comparator, from, entries.length - 1, k);
	}

	/**
	 * Sorts [from,first+k-1] in the array of [from,to] using a variant of
	 * modified heapsort, such that
	 * array[from]<array[from+1]<...<array[from+k-1] and
	 * array[from+k-1]<arra[from+k||from+k+1||from+k+2|| ....to]
	 * 
	 * Note: when inBlocks is false,sorting happens in single shot, not in blocks.
	 * 
	 * @param entries
	 * @param comparator
	 * @param from
	 * @param to
	 * @param k
	 * @param inBlocks 
	 * 
	 * @see MarkerSortUtil#sortStatingKElement(MarkerEntry[], Comparator, int, int, int)
	 */
	public static void sortStatingKElement(MarkerEntry[] entries,
			Comparator comparator, int from, int to, int k,boolean inBlocks) {
		if(inBlocks){
			sortStatingKElement(entries, comparator, from, to, k);
			return;
		}
		// check range valid
		int fromK = from + k - 1;
		if (entries.length == 0 || from < 0 || from >= to || fromK < from
				|| fromK > to || to > entries.length - 1 || to < 0)
			return;
		int n=to-from;
		if(n<=BATCH_SIZE&&(((float)n/k)<=MERGE_OR_HEAP_SWITCH)){
			//use arrays sort
			Arrays.sort(entries,from,to,comparator);
			// clear cache for first to middle since we are done with sort
			for (int i = from; i <= to; i++) {
				entries[i].clearCache();
			}
		}else{
			partiallySort(entries, from, fromK, to, comparator);
		}
	}
	
	/**
	 * Sorts [0,k-1] in the array of [0,entries.length-1] using a variant of
	 * modified heapsort, such that
	 * array[0]<array[1]<...<array[k-1] and
	 * array[k-1]<arra[k||k+1||k+2|| ....entries.length-1]
	 * 
	 * Note: when inBlocks is false,sorting happens in single shot, not in blocks.
	 * 
	 * @param entries
	 * @param comparator
	 * @param k
	 * @param inBlocks 
	 */
	public static void sortStatingKElement(MarkerEntry[] entries,
			Comparator comparator, int k,boolean inBlocks) {
		sortStatingKElement(entries, comparator, 0,
				entries.length - 1, k,inBlocks);
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
