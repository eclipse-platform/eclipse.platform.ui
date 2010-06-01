/*
 *
 * Copyright (c) 1994, 2009
 * Hewlett-Packard Company
 *
 * Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appear in all copies and
 * that both that copyright notice and this permission notice appear
 * in supporting documentation.  Hewlett-Packard Company makes no
 * representations about the suitability of this software for any
 * purpose.  It is provided "as is" without express or implied warranty.
 *
 * Copyright (c) 1997
 * Silicon Graphics Computer Systems, Inc.
 *
 * Permission to use, copy, modify, distribute and sell this software
 * and its documentation for any purpose is hereby granted without fee,
 * provided that the above copyright notice appear in all copies and
 * that both that copyright notice and this permission notice appear
 * in supporting documentation.  Silicon Graphics makes no
 * representations about the suitability of this software for any
 * purpose.  It is provided "as is" without express or implied warranty.
 * 
 * Contributions:
 *              IBM - Ported the code to Java
 */

package org.eclipse.ui.internal.views.markers;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @since 3.5
 * 
 * @author Hitesh
 */
public class MarkerSortUtil {
	
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

		adjustMaxElement(array, first, middle, last, comparator);
		
		heapToSortedArray(array, first, middle, comparator);
	}

	/**
	 * Swap the max heap element with any greater elements in rest of the array
	 * 
	 * @param heapArray
	 * @param first
	 * @param heapSize
	 * @param last
	 * @param comparator
	 */
	private static void adjustMaxElement(MarkerEntry[] heapArray, int first,
			int heapSize, int last, Comparator comparator) {
		/*
		 * we do not clear caches for heap elements when re-adjusting and
		 * sorting this will ensure sorting remains fast
		 */
		int current = heapSize;
		while (current <= last) {
			if (comparator.compare(heapArray[current], heapArray[first]) < 0) {
				MarkerEntry tmp = heapArray[current];
				heapArray[current] = heapArray[first];
				heapArray[first] = tmp;
				adjustHeap(heapArray, first, first, heapSize, comparator);
			}
			// clear cache of the one not in heap
			heapArray[current].clearCache();

			++current;
		}
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
		MarkerEntry hole = array[position];
		int holeIndex = position;
		holeIndex = leafSearch(array, first, holeIndex, last, comparator);
		holeIndex = bottomUpSearch(array, first, holeIndex,position,hole,last, comparator);
		array[holeIndex] = hole;
	}

	/**
	 * Percolate down the Heap: adjust left ,right, self nodes for heap starting
	 * from hole all the way down the heap 
	 * 
	 * @param array
	 * @param first
	 * @param position
	 * @param last
	 * @param comparator
	 * @return new holeIndex
	 */
	private static int leafSearch(MarkerEntry[] array, int first, int position,
			int last, Comparator comparator) {
		int holeOffset = position - first;
		int len = last - first;
		int childOffset = 2 * holeOffset + 2;
		// 
		while (childOffset < len) {
			if (comparator.compare(array[first + childOffset], array[first
					+ (childOffset - 1)]) < 0)
				--childOffset;
			array[first + holeOffset] = array[first + childOffset];
			holeOffset = childOffset++;
			childOffset *= 2;
		}
		if (childOffset-- == len) {
			array[first + holeOffset] = array[first + childOffset];
			holeOffset = childOffset;
		}
		return holeOffset + first;
	}

	/**
	 * percolate up the Heap: add the hole element back to heap at the right
	 * position, all the way up the heap between fromIndex and toIndex
	 * 
	 * @param array
	 * @param first
	 * @param fromIndex
	 * @param toIndex
	 * @param hole
	 * @param last
	 * @param comparator
	 * @return new holeIndex
	 */
	private static int bottomUpSearch(MarkerEntry[] array, int first, int fromIndex,
			int toIndex, MarkerEntry hole, int last, Comparator comparator) {
		int holeOffset = fromIndex - first;
		int parent = (holeOffset - 1) / 2;
		int top = toIndex - first;

		while (holeOffset != top
				&& (comparator.compare(array[first + parent], hole) < 0)) {
			array[first + holeOffset] = array[first + parent];
			holeOffset = parent;
			parent = (holeOffset - 1) / 2;
		}

		/* 
		 * Using Binary search to locate the parent to replace.
		 * This is worse compared to linear search as most of the 
		 * holes would replace only a few parents above them.
		 * This code has been left commented for future examination.
		 * */		
	    /*
		int top = position - first;
		int lowParent = 1;
		int highParent = 0;
		int from = holeOffset;
		while (from > top) {
			from = (from - 1) / 2;
			highParent++;
		}
		if (from != top)
			highParent--;// Between, exculding top//n++;

		int parentReplaceCount = -1;
		// start at lower ranges because of heap property
		int midParent = lowParent + (highParent - lowParent) / 4;
		while (lowParent <= highParent) {
			// mth parent
			int currenParentIndex = holeOffset;
			int mth = midParent;
			while (mth > 0) {
				currenParentIndex = (currenParentIndex - 1) / 2;
				mth--;
			}
			int value = comparator.compare(array[first + currenParentIndex], hole);
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
		int parent = (holeOffset - 1) / 2;
		for (int i = 1; i <= parentReplaceCount; i++) {
			array[first + holeOffset] = array[first + parent];
			holeOffset = parent;
			parent = (holeOffset - 1) / 2;
		}
		*/
		return first + holeOffset;
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
	 * @param monitor 
	 */
	public static void sortStartingKElement(MarkerEntry[] entries,
			Comparator comparator, int from, int to, int k,IProgressMonitor monitor) {
		// check range valid
		int last = from + k-1;
		if (entries.length == 0 || from < 0 || from >= to || last < from
				|| last > to || to > entries.length - 1 || to < 0)
			return;
		int n=to-from+1;
		if (n <= BATCH_SIZE && (((float) n / k) <= MERGE_OR_HEAP_SWITCH)
				/*|| ((float) n / k) <= MERGE_OR_HEAP_SWITCH*/) { 
			// use arrays sort
			Arrays.sort(entries, from, to + 1, comparator);
			// clear cache for first to middle since we are done with sort
			for (int i = from; i <= to; i++) {
				entries[i].clearCache();
			}
			return;
		}
		
		// do it in blocks of BATCH_SIZE so we get a chance
		// of clearing caches to keep memory usage to a minimum

		//we choose k-1 so that last batch includes last element 
		//in case k is a multiple of  BATCH_SIZE
		int totalBatches = (k-1) / BATCH_SIZE; 
		int batchCount = 0;
		while (totalBatches > 0) {
			if(monitor.isCanceled()){
				return;
			}
			int fromTemp = from + batchCount * BATCH_SIZE;
			int toTemp = from + (batchCount + 1) * BATCH_SIZE;
			partiallySort(entries, fromTemp, toTemp, to, comparator);
			batchCount++;
			totalBatches--;
		}
		if(monitor.isCanceled()){
			return;
		}
		if (last >= from + batchCount * BATCH_SIZE) {
			// the last remaining enteries
			if (last == to) {
				partiallySort(entries, from + batchCount * BATCH_SIZE, last,
						to, comparator);
			} else {
				partiallySort(entries, from + batchCount * BATCH_SIZE, last+1,
						to, comparator);
			}
		}
	}

	/**
	 * @param fArray1
	 * @param comparator
	 * @param from
	 * @param k
	 * @param limit
	 */
	public static void sortStartingKElement(MockMarkerEntry[] fArray1,
			Comparator comparator, int from, int k, int limit) {
		sortStartingKElement(fArray1, comparator, from, k, limit,new NullProgressMonitor());		
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
	 * @param monitor 
	 */
	public static void sortStartingKElement(MarkerEntry[] entries,
			Comparator comparator, int k,IProgressMonitor monitor) {
		sortStartingKElement(entries, comparator, 0, entries.length - 1, k,monitor);
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
	 * @param monitor 
	 */
	public static void sortStartingKElement(MarkerEntry[] entries,
			Comparator comparator, int from, int k, IProgressMonitor monitor) {
		sortStartingKElement(entries, comparator, from, entries.length - 1, k,monitor);
	}
	
}