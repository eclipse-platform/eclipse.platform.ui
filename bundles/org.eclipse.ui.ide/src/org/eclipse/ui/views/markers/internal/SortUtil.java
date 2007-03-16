/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 */
class SortUtil {

    /**
     *  * Returns the k smallest items in the given collection. Runs in
     * O(n) time, average case. The resulting collection is not sorted.
     * @param elements the MarkerList to check
     * @param c the comparator
     * @param k the number of items to collect
     * @param mon the monitor
     * @return MarkerList
     */
    public static MarkerList getFirst(MarkerList elements, Comparator c, int k,
            IProgressMonitor mon) {
    	Collection start = elements.asList();
        Collection result = new ArrayList(start.size());

        mon.beginTask(MarkerMessages.SortUtil_finding_first, 1000); 

        getFirst(result, start, c, k, mon, 1000);

        mon.done();

        return new MarkerList(result);
    }

    private static void getFirst(Collection result, Collection elements,
            Comparator c, int k, IProgressMonitor mon, int totalWork) {

        if (mon.isCanceled()) {
            return;
        }

        if (elements.size() <= k) {
            result.addAll(elements);
            mon.worked(totalWork);
            return;
        }

        Object pivot;

        if (elements instanceof ArrayList) {
            pivot = ((ArrayList) elements).get(elements.size() / 2);
        } else {
            pivot = elements.iterator().next();
        }
        Collection more = new ArrayList(elements.size());
        Collection less = new ArrayList(elements.size());
        Collection equal = new ArrayList();

        partitionHelper(less, more, equal, elements, c, pivot, mon,
                totalWork / 2);

        if (less.size() >= k) {
            getFirst(result, less, c, k, mon, totalWork / 2);
        } else if (less.size() + equal.size() >= k) {

            int count = k - less.size();

            result.addAll(less);

            Iterator iter = equal.iterator();
            while (iter.hasNext() && count > 0) {
                Object next = iter.next();

                result.add(next);
                count--;
            }
            mon.worked(totalWork / 2);
        } else if (less.size() + equal.size() + more.size() >= k) {
            result.addAll(less);
            result.addAll(equal);

            getFirst(result, more, c, k - less.size() - equal.size(), mon,
                    totalWork / 2);
        }
    }

    private static void partitionHelper(Collection less, Collection more,
            Collection equal, Collection input, Comparator c, Object toTest,
            IProgressMonitor mon, int totalWork) {
        int workRemaining = totalWork;
        int counter = 0;
        int totalItems = input.size();

        Iterator iter = input.iterator();

        while (iter.hasNext()) {
            Object next = iter.next();

            int compareResult = c.compare(next, toTest);

            if (compareResult < 0) {
                less.add(next);
            } else if (compareResult > 0) {
                more.add(next);
            } else {
                equal.add(next);
            }

            counter++;
            if (counter > 100) {
                if (mon.isCanceled()) {
                    return;
                }
                int nextWorked = counter * workRemaining / totalItems;
                mon.worked(nextWorked);
                workRemaining -= nextWorked;
                totalItems -= counter;
                counter = 0;
            }
        }

        mon.worked(workRemaining);
    }

    /**
     * Divides the items in the input collection into three sets based on whether they are less than,
     * equal to, or greater than the test item.
     * 
     * If the given monitor is cancelled (possibly by another thread), the operation will
     * be aborted. In this case, the insertions may only be partially complete. 
     * 
     * @param less
     * @param more
     * @param equal
     * @param input
     * @param c
     * @param toTest
     * @param mon
     */
    public static void partition(Collection less, Collection more,
            Collection equal, Collection input, Comparator c, Object toTest,
            IProgressMonitor mon) {
        mon
                .beginTask(
                        MarkerMessages.SortUtil_partitioning, input.size()); 

        if (toTest == null || c == null) {
            int counter = 0;
            Iterator iter = input.iterator();
            while (iter.hasNext()) {
                Object next = iter.next();

                counter++;
                if (counter >= 20) {
                    mon.worked(counter);
                    counter = 0;
                    if (mon.isCanceled()) {
                        return;
                    }
                }

                more.add(next);
            }
            mon.worked(counter);
        } else {
            partitionHelper(less, more, equal, input, c, toTest, mon, input
                    .size());
        }

        mon.done();
    }

    /**
     * Removes and returns the first n items from the given collection.
     * 
     * @param collection
     * @param numToRemove
     * @return List
     */
    public static List removeFirst(Collection collection, int numToRemove) {
        int toRemove = Math.min(collection.size(), numToRemove);

        List removed = new ArrayList(toRemove);

        Iterator iter = collection.iterator();

        for (int idx = 0; idx < toRemove; idx++) {
            removed.add(iter.next());

            iter.remove();
        }

        return removed;
    }

    /**
     * Finds and returns the greatest element in the given collection, or null if the collection
     * is empty.
     *  
     * @param toSearch collection to search
     * @param c comparator used to determine the greatest item
     * @return the greatest item in the collection
     */
    public static Object findGreatest(Collection toSearch, Comparator c) {
        // If this set is already sorted using the given comparator, just return the last element
        if (toSearch instanceof SortedSet
                && ((SortedSet) toSearch).comparator().equals(c)) {
            return ((SortedSet) toSearch).last();
        }

        // Otherwise, exhaustively search for the greatest element
        Object result = null;
        Iterator iter = toSearch.iterator();

        while (iter.hasNext()) {
            Object next = iter.next();

            if (result == null || c.compare(result, next) > 0) {
                result = next;
            }
        }

        return result;
    }
}
