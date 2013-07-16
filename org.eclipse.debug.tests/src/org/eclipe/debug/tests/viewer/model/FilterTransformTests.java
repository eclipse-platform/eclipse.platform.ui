/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipe.debug.tests.viewer.model;

import junit.framework.TestCase;

import org.eclipse.debug.internal.ui.viewers.model.FilterTransform;
import org.eclipse.jface.viewers.TreePath;

/**
 * Tests the virtual viewer's filter transform
 * @since 3.3
 */
public class FilterTransformTests extends TestCase {
	
	public Object root;
	public Object element0;
	public Object element1;
	public Object element2;
	public Object element3;
	public Object element4;
	public Object element5;
	public Object element6;
	public Object element7;
	
	public FilterTransform transform;
	
	/**
	 * @param name
	 */
	public FilterTransformTests(String name) {
		super(name);
	}
	
	/**
	 * Builds a filter transform. Model has 8 elements,
	 * and elements 0, 2, 3, 6, 7 are filtered. Elements
	 * 1, 4, 5 are visible.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		transform = new FilterTransform();
		root = new Object();
		element0 = new Object();
		element1 = new Object();
		element2 = new Object();
		element3 = new Object();
		element4 = new Object();
		element5 = new Object();
		element6 = new Object();
		element7 = new Object();
		assertTrue(transform.addFilteredIndex(TreePath.EMPTY, 0, element0));
		assertTrue(transform.addFilteredIndex(TreePath.EMPTY, 2, element2));
		assertTrue(transform.addFilteredIndex(TreePath.EMPTY, 3, element3));
		assertTrue(transform.addFilteredIndex(TreePath.EMPTY, 6, element6));
		assertTrue(transform.addFilteredIndex(TreePath.EMPTY, 7, element7));
	}
	
	protected boolean equals(int[] a, int[] b) {
		if (a.length == b.length) {
			for (int i = 0; i < b.length; i++) {
				if (a[i] != b[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public void testRemoveMiddleElementFromFilters() {
		assertTrue("Element should be filtered", transform.isFiltered(TreePath.EMPTY, 3)); //$NON-NLS-1$
		transform.removeElementFromFilters(TreePath.EMPTY, element3);
		assertFalse("Element should be unfiltered", transform.isFiltered(TreePath.EMPTY, 3)); //$NON-NLS-1$
		assertTrue("Wrong filter state", equals(transform.getFilteredChildren(TreePath.EMPTY), new int[] { 0, 2, 5, 6 })); //$NON-NLS-1$
	}
	
	public void testRemoveFirstElementFromFilters() {
		assertTrue("Element should be filtered", transform.isFiltered(TreePath.EMPTY, 0)); //$NON-NLS-1$
		transform.removeElementFromFilters(TreePath.EMPTY, element0);
		assertFalse("Element should be unfiltered", transform.isFiltered(TreePath.EMPTY, 0)); //$NON-NLS-1$
		assertTrue("Wrong filter state", equals(transform.getFilteredChildren(TreePath.EMPTY), new int[] { 1, 2, 5, 6 })); //$NON-NLS-1$
	}	

	public void testRemoveLastFromFilters() {
		assertTrue("Element should be filtered", transform.isFiltered(TreePath.EMPTY, 7)); //$NON-NLS-1$
		transform.removeElementFromFilters(TreePath.EMPTY, element7);
		assertFalse("Element should be unfiltered", transform.isFiltered(TreePath.EMPTY, 7)); //$NON-NLS-1$
		assertTrue("Wrong filter state", equals(transform.getFilteredChildren(TreePath.EMPTY), new int[] { 0, 2, 3, 6 })); //$NON-NLS-1$
	}
	
	public void testClearMiddleElementFromFilters() {
		assertTrue("Element should be filtered", transform.isFiltered(TreePath.EMPTY, 3)); //$NON-NLS-1$
		transform.clear(TreePath.EMPTY, 3);
		assertFalse("Element should be unfiltered", transform.isFiltered(TreePath.EMPTY, 3)); //$NON-NLS-1$
		assertTrue("Wrong filter state", equals(transform.getFilteredChildren(TreePath.EMPTY), new int[] { 0, 2, 6, 7 })); //$NON-NLS-1$
	}
	
	public void testClearFirstElementFromFilters() {
		assertTrue("Element should be filtered", transform.isFiltered(TreePath.EMPTY, 0)); //$NON-NLS-1$
		transform.clear(TreePath.EMPTY, 0);
		assertFalse("Element should be unfiltered", transform.isFiltered(TreePath.EMPTY, 0)); //$NON-NLS-1$
		assertTrue("Wrong filter state", equals(transform.getFilteredChildren(TreePath.EMPTY), new int[] { 2, 3, 6, 7 })); //$NON-NLS-1$
	}	

	public void testClearLastFromFilters() {
		assertTrue("Element should be filtered", transform.isFiltered(TreePath.EMPTY, 7)); //$NON-NLS-1$
		transform.clear(TreePath.EMPTY, 7);
		assertFalse("Element should be unfiltered", transform.isFiltered(TreePath.EMPTY, 7)); //$NON-NLS-1$
		assertTrue("Wrong filter state", equals(transform.getFilteredChildren(TreePath.EMPTY), new int[] { 0, 2, 3, 6 })); //$NON-NLS-1$
	}	
	
	public void testViewToModelCount() {
		assertEquals("Wrong model count", 8, transform.viewToModelCount(TreePath.EMPTY, 3)); //$NON-NLS-1$
	}
	
	public void testViewToModelIndex() {
		assertEquals("Wrong model index", 1, transform.viewToModelIndex(TreePath.EMPTY, 0)); //$NON-NLS-1$
		assertEquals("Wrong model index", 4, transform.viewToModelIndex(TreePath.EMPTY, 1)); //$NON-NLS-1$
		assertEquals("Wrong model index", 5, transform.viewToModelIndex(TreePath.EMPTY, 2)); //$NON-NLS-1$
	}
	
	public void testAddAlreadyFiltered() {
		assertTrue("Element should be filtered", transform.isFiltered(TreePath.EMPTY, 0)); //$NON-NLS-1$
		boolean added = transform.addFilteredIndex(TreePath.EMPTY, 0, element0);
		assertFalse("Filter should not be added - should already have been there", added); //$NON-NLS-1$
	}
	
	
	/**
	 * Test to make sure that setModelChildCount() updates internal arrays appropriately.
	 * See bug 200325.
	 */
	public void testRegression200325() {
		transform.setModelChildCount(TreePath.EMPTY, 2);
		try {
			transform.addFilteredIndex(TreePath.EMPTY, 3, new Object());
		} catch (ArrayIndexOutOfBoundsException e) {
			fail("AIOOBE Exception should not be thrown here, " + //$NON-NLS-1$
			"setModelChildCount should leave " + //$NON-NLS-1$
			"FilterTransform$Node in a consistent state"); //$NON-NLS-1$
		}
	}
}
