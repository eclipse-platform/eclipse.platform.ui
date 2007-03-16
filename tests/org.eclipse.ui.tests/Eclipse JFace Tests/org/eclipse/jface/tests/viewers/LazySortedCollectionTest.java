/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.viewers.deferred.LazySortedCollection;

/**
 * @since 3.1
 */
public class LazySortedCollectionTest extends TestCase {
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(LazySortedCollectionTest.class);
    }
    
    public static Test suite() {
        return new TestSuite(LazySortedCollectionTest.class);
    }
    
    private TestComparator comparator;
    private TestComparator comparisonComparator;
    
    // All operations will be done on both collections -- after each operation, the result
    // will be compared
    private LazySortedCollection collection;
    private TreeSet comparisonCollection;

    /**
     * Please don't add or remove from this set -- we rely on the exact insertion order
     * to get full coverage from the removal tests.
     */
    private String[] se = new String[] {
            "v00 aaaaaa",
            "v01 apple",
            "v02 booger",
            "v03 car",
            "v04 dog",
            "v05 elephant",
            "v06 fox",
            "v07 goose",
            "v08 hippie",
            "v09 iguana",
            "v10 junk",
            "v11 karma",
            "v12 lemon",
            "v13 mongoose",
            "v14 noodle",
            "v15 opal",
            "v16 pumpkin",
            "v17 quirks",
            "v18 resteraunt",
            "v19 soap",
            "v20 timmy",
            "v21 ugly",
            "v22 virus",
            "v23 wigwam",
            "v24 xerxes",
            "v25 yellow",
            "v26 zero"
    };
    
    /**
     * Please don't mess with the insertion order here or add or remove from this set -- 
     * we rely on the exact order in order to get full coverage for the removal tests.
     */
    private String[] elements = new String[] {
            se[19],
            se[7],
            se[6],
            se[1],
            se[20],
            se[8],
            se[0],
            se[23],
            se[17],
            se[18],
            se[24],
            se[25],
            se[10],
            se[5],
            se[15],
            se[16],
            se[21],
            se[26],
            se[22],
            se[3],
            se[9],
            se[4],
            se[11],
            se[12],
            se[13],
            se[14],
            se[2]  
        };

    
    public static void printArray(Object[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.println("[" + i + "] = " + array[i]);
        }
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        System.out.println("--- " + getName());
        
        comparator = new TestComparator();
        collection = new LazySortedCollection(comparator);
        // Ensure a predictable tree structure
        collection.enableDebug = true;
     
        comparisonComparator = new TestComparator();
        comparisonCollection = new TreeSet(comparisonComparator);
        
        addAll(elements);
        
        super.setUp();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        System.out.println("Comparisons required by lazy collection: " + comparator.comparisons);
        System.out.println("Comparisons required by reference implementation: " + comparisonComparator.comparisons);
        System.out.println("");
        
        super.tearDown();
    }
    
    /**
     * Computes the entries that are expected to lie in the given range. The result
     * is sorted.
     * 
     * @param start
     * @param length
     * @return
     * @since 3.1
     */
    private Object[] computeExpectedElementsInRange(int start, int length) {
        int counter = 0;
        
        Iterator iter = comparisonCollection.iterator();
        while(iter.hasNext() && counter < start) {
            iter.next();
            counter++;
        }
        
        Object[] result = new Object[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = iter.next(); 
        }
                
        return result;
    }

    private void addAll(Object[] elements) {
        collection.addAll(elements);
        comparisonCollection.addAll(Arrays.asList(elements));
    }
    
    private void add(Object toAdd) {
        collection.add(toAdd);
        comparisonCollection.add(toAdd);
    }
    
    private void remove(Object toRemove) {
        collection.remove(toRemove);
        comparisonCollection.remove(toRemove);
    }
    
    private void removeRange(int start, int length) {
        collection.removeRange(start, length);
        
        Object[] expected = computeExpectedElementsInRange(start, length);

        // Alternative remove implementation that removes one-at-a-time
        for (int i = 0; i < expected.length; i++) {
            comparisonCollection.remove(expected[i]);
        }
    }
    
    private void clear() {
        collection.clear();
        comparisonCollection.clear();
    }
    
    /**
     * Force the collection to sort all elements immediately.
     * 
     * @since 3.1
     */
    private void forceFullSort() {
        queryRange(0, elements.length, true);
    }
    
    private void assertContentsValid() {
        queryRange(0, comparisonCollection.size(), false);
        Assert.assertEquals(comparisonCollection.size(), collection.size());
        Assert.assertEquals(comparisonCollection.isEmpty(), collection.isEmpty());
    }
    
    private void assertIsPermutation(Object[] array1, Object[] array2) {
        Object[] sorted1 = new Object[array1.length];
        System.arraycopy(array1, 0, sorted1, 0, array1.length);
        Arrays.sort(sorted1, new TestComparator());
        
        Object[] sorted2 = new Object[array2.length];
        System.arraycopy(array2, 0, sorted2, 0, array2.length);
        Arrays.sort(sorted2, new TestComparator());
        
        assertArrayEquals(sorted1, sorted2);
    }

    /**
     * Queries the collection for the given range, and throws an assertation failure if the 
     * result was unexpected. Assumes that the "elements" array was initially added and that nothing
     * has been added or removed since.
     * 
     * @param start
     * @param length
     * @param sorted
     * @since 3.1
     */
    private Object[] queryRange(int start, int length, boolean sorted) {
        Object[] result = new Object[length];
        
        int returnValue = collection.getRange(result, start, sorted);
        
        Assert.assertEquals(returnValue, length);
        
        Object[] expectedResult = computeExpectedElementsInRange(start, length);
        
        if (sorted) {
            // If the result is supposed to be sorted, it will match the expected
            // result exactly
            assertArrayEquals(expectedResult, result);
        } else {
            // Otherwise, the result merely needs to be a permutation of the
            // expected result.
            assertIsPermutation(result, expectedResult);
        }
        
        collection.testInvariants();
        
        return result;
    }
    
    private void assertArrayEquals(Object[] array1, Object[] array2) {
        for (int i = 0; i < array1.length; i++) {

            Assert.assertEquals(array1[i], array2[i]);
        }        
    }
    
    public void testComparisonCount() {
        Assert.assertTrue("additions should not require any comparisons", comparator.comparisons == 0);
        
        queryRange(0, elements.length, false);

        Assert.assertTrue("requesting the complete set of unsorted elements should not require any comparisons", comparator.comparisons == 0);
    }
    
    /**
     * Ensure that no comparisons are required for range queries once the collection is fully sorted
     * 
     * @since 3.1
     */
    public void testSortAll() {
        // Test that sorting the entire array works
        queryRange(0, elements.length, true);
        
        int comparisons = comparator.comparisons;
        
        // Ensure that subsequent operations require no comparisons
        queryRange(elements.length - 10, 10, true);
        queryRange(0, 10, false);
        
        Assert.assertEquals("Once the lazy collection is fully sorted, it should not require further comparisons", 
                comparisons, comparator.comparisons);
    }
    
    /**
     * Tests LazySortedCollection.removeNode(int) when removing a leaf node
     */
    public void testRemoveLeafNode() {
        forceFullSort();
        remove(se[9]);
        assertContentsValid();
    }

    /**
     * Tests LazySortedCollection.removeNode(int) when removing a node with no left child
     */
    public void testRemoveNodeWithNoLeftChild() {
        forceFullSort();
        remove(se[23]);
        assertContentsValid();
    }

    /**
     * Tests LazySortedCollection.removeNode(int) when removing a node with no right child
     * 
     * @since 3.1
     */
    public void testRemoveNodeWithNoRightChild() {
        forceFullSort();
        remove(se[13]);
        assertContentsValid();
    }
    
    /**
     * Tests LazySortedCollection.remove when removing the root node
     * 
     * @since 3.1
     */
    public void testRemoveRootNode() {
        forceFullSort();
        remove(se[19]);
        assertContentsValid();
    }

    /**
     * Tests LazySortedCollection.remove when the removal
     * will require swapping with a non-leaf node. (The descendent with the closest value
     * in the largest subtree has at least 1 child).
     * 
     * @since 3.1
     */
    public void testRemoveWhereSwappedNodeIsntLeaf() {
        forceFullSort();
        remove(se[14]);
        assertContentsValid();
    }
    
    /**
     * Tests LazySortedCollection.remove when the removal
     * will require swapping with a non-leaf node, and both the removed node and the swapped
     * node contain unsorted children.
     * 
     * @since 3.1
     */
    public void testRemoveWithUnsortedSwap() {
        // Ensure that we've sorted nodes 13 and 14 
        queryRange(14, 1, true);
        queryRange(13, 1, true);
        
        // Add some unsorted nodes that will become children of the node with value "v13 mongoose"
        addAll(new String[] {"v13 n", "v13 l"});
        // Ensure that the new nodes are pushed down to node 14 by querying its parent (currently at position 8)
        queryRange(8, 1, true);
        
        // Add some unsorted nodes that will become children of the node with value "v14 noodle" 
        addAll(new String[] {"v14 m", "v14 o"});
        // Push down the unsorted nodes by querying for the parent of "v14 noodle" 
        // (the parent is currently at position 7) 
        queryRange(7, 1, true);
        
        // Now remove node with value "v14 noodle" -- this should swap it with the node "v13 mongoose", requiring
        // both sets of unsorted children to be dealt with
        
        remove(se[14]);
        assertContentsValid();
    }
    
    /**
     * Remove an element from an initially unsorted collection
     * 
     * @since 3.1
     */
    public void testRemoveFromUnsorted() {
        remove(se[10]);
        assertContentsValid();
    }
    
    /**
     * Remove the root from an initially-unsorted collection
     * 
     * @since 3.1
     */
    public void testRemoveRootFromUnsorted() {
        remove(se[19]);
        assertContentsValid();
    }
    
    /**
     * Ensure that removing an element that isn't in the collection is a no-op
     * 
     * @since 3.1
     */
    public void testRemoveUnknown() {
        remove("some unknown element");
        assertContentsValid();
    }
    
    /**
     * Tests that the swaps during removal don't mess up the internal hashmap.
     * Perform a removal that will require a swap, add a new item, then
     * remove the item that was previously swapped. If the hashmap was not updated,
     * then the removed item may still be pointing to its old index and the 
     * new item will be removed instead. 
     * 
     * @since 3.1
     */
    public void testRemovePreviouslySwappedNode() {
        queryRange(0, elements.length, true);
        remove(se[14]);
        // Add a new item -- should reuse the same index used by the previous item
        add("something new");
        assertContentsValid();
        remove(se[13]);
        assertContentsValid();
    }
    
    /**
     * Remove all nodes using removeRange
     * 
     * @since 3.1
     */
    public void testRemoveFullRange() {
        removeRange(0, se.length);
        assertContentsValid();
    }
    
    /**
     * Remove a range that includes the start
     * 
     * @since 3.1
     */
    public void testRemoveFromStart() {
        removeRange(0, se.length / 2);
        assertContentsValid();
    }
    
    /**
     * Remove a range that includes the last node
     * 
     * @since 3.1
     */
    public void testRemoveFromEnd() {
        int start = se.length / 2;
        removeRange(start, se.length - start);
        assertContentsValid();
    }
    
    /**
     * Remove a range that includes the root, but leaves nodes in both subtrees intact.
     * 
     * @since 3.1
     */
    public void testRemoveIncludingRoot() {
        removeRange(14, 6);
        assertContentsValid();
    }
    
    /**
     * Test boundary conditions: 
     * 
     * Tests moving an entire right subtree, and a left subtree including the tree itself
     */
    public void testRemoveRightSubtree() {
        removeRange(9, 5);
        assertContentsValid();
    }

    /**
     * Test boundary conditions: Tests moving an entire left subtree
     */
    public void testRemoveLeftSubtree() {
        removeRange(3, 4);
        assertContentsValid();
    }

    /**
     * Test boundary conditions: Tests moving an entire left subtree including the tree itself
     */
    public void testRemoveRightIncludingRoot() {
        removeRange(3, 5);
        assertContentsValid();
    }
    
    public void testClear() {
        clear();
        assertContentsValid();
    }
    
    public void testClearSorted() {
        forceFullSort();
        clear();
        assertContentsValid();
    }
    
    //    
//    
//    public static void testAdditions() {
//        TestComparator comparator = new TestComparator();
//        LazySortedCollection collection = new LazySortedCollection(comparator);
//
//        addSomeElements(collection);
//        
//        System.out.println("comparisons after add = " + comparator.comparisons);
//        comparator.comparisons = 0;
//        
//        // Getfirst10Elements
//        Object[] first10Elements = new Object[10];
//        collection.getFirst(first10Elements, false);
//        System.out.println("first 10 elements:");
//        printArray(first10Elements);
//        System.out.println("comparisons after getFirst = " + comparator.comparisons);
//        comparator.comparisons = 0;
//        
//        collection.print();
//        
//        // remove the 10th element
//        collection.remove(first10Elements[9]);
//        
//        collection.print();
//
////        
////        collection.getFirst(first10Elements, true);
////        System.out.println("first 10 elements (sorted):");
////        printArray(first10Elements);
////        System.out.println("comparisons after getFirst = " + comparator.comparisons);
////        comparator.comparisons = 0;
////        
////        
////        
////        // get elements 8-13
////        Object[] eightThrough14 = new Object[7];
////        collection.getRange(eightThrough14, 8, false);
////        System.out.println("elements 8-14:");
////        printArray(eightThrough14);
////        System.out.println("comparisons after 8-14 query = " + comparator.comparisons);
////        comparator.comparisons = 0;
//        
//        collection.print();
//    }
}
