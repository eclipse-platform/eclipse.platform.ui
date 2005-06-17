/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.workbench.texteditor.quickdiff.tests;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.IRangeComparator;
import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.Levenstein;


/**
 * @since 3.1
 */
public class OptimizedLevensteinTest extends TestCase {
	private final static int SKIP= Integer.MAX_VALUE;

	protected Levenstein createLevenstein(IRangeComparator left, IRangeComparator right) {
		Levenstein ls= new Levenstein(left, right);
		LevensteinTestHelper helper= new LevensteinTestHelper(ls);
		helper.setOptimizedCellComputer();
		return ls;
	}
	
	public void testInternalEditDistance() {
		IRangeComparator c1= new SequenceComparator("abcd"); 
		IRangeComparator c2= new SequenceComparator("abcd");
		
		Levenstein levenstein= createLevenstein(c1, c2);
		LevensteinTestHelper helper= new LevensteinTestHelper(levenstein);
		helper.initRows();
		
		// full compare
		helper.internalEditDistance(1, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { SKIP, SKIP, SKIP, SKIP, 0 }, helper.getPreviousRow()));

		// partial compare
		helper.internalEditDistance(1, 2, 1, 4);
		assertTrue(Arrays.equals(new int[] { SKIP, SKIP, 0, 1, 2 }, helper.getPreviousRow()));
		
		helper.internalEditDistance(3, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { SKIP, SKIP, SKIP, SKIP, 2 }, helper.getPreviousRow()));
		
		helper.internalEditDistance(1, 4, 1, 2);
		assertEquals(SKIP, helper.getPreviousRow()[0]);
		assertEquals(SKIP, helper.getPreviousRow()[1]);
		assertEquals(2, helper.getPreviousRow()[2]);
		
		// empty right
		helper.internalEditDistance(1, 0, 1, 4);
		assertTrue(Arrays.equals(new int[] { 0, 1, 2, 3, 4 }, helper.getPreviousRow()));
		
		// empty left
		helper.internalEditDistance(1, 4, 1, 0);
		assertEquals(4, helper.getPreviousRow()[0]);
		
		// empty both
		helper.internalEditDistance(1, 0, 1, 0);
		assertEquals(0, helper.getPreviousRow()[0]);
		
		// test insertion
		c1= new SequenceComparator("a"); 
		c2= new SequenceComparator("ab");
		levenstein= createLevenstein(c1, c2);
		helper= new LevensteinTestHelper(levenstein);
		helper.initRows();

		helper.internalEditDistance(1, 1, 1, 1);
		assertTrue(Arrays.equals(new int[] { SKIP, 0 }, helper.getPreviousRow()));

		helper.internalEditDistance(2, 2, 1, 1);
		assertTrue(Arrays.equals(new int[] { SKIP, 1 }, helper.getPreviousRow()));
	}
	
	public void testInternalReverseEditDistance() {
		IRangeComparator c1= new SequenceComparator("abcd"); 
		IRangeComparator c2= new SequenceComparator("abcd");
		
		Levenstein levenstein= createLevenstein(c1, c2);
		LevensteinTestHelper helper= new LevensteinTestHelper(levenstein);
		helper.initRows();
		
		// full compare
		helper.internalReverseEditDistance(1, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { 0, SKIP, SKIP, SKIP, SKIP }, helper.getPreviousRow()));

		// partial compare
		helper.internalReverseEditDistance(1, 2, 1, 4);
		assertTrue(Arrays.equals(new int[] { 2, SKIP, SKIP, SKIP, SKIP }, helper.getPreviousRow()));
		
		helper.internalReverseEditDistance(3, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { 2, 1, 0, SKIP, SKIP }, helper.getPreviousRow()));
		
		helper.internalReverseEditDistance(1, 4, 1, 2);
		assertEquals(SKIP, helper.getPreviousRow()[2]);
		assertEquals(SKIP, helper.getPreviousRow()[1]);
		assertEquals(2, helper.getPreviousRow()[0]);
		
		// empty right
		helper.internalReverseEditDistance(1, 0, 1, 4);
		assertTrue(Arrays.equals(new int[] { 4, 3, 2, 1, 0 }, helper.getPreviousRow()));
		
		// empty left
		helper.internalReverseEditDistance(1, 4, 5, 4);
		assertEquals(4, helper.getPreviousRow()[4]);
		helper.internalReverseEditDistance(1, 4, 1, 0);
		assertEquals(4, helper.getPreviousRow()[0]);
		
		// empty both
		helper.internalReverseEditDistance(1, 0, 1, 0);
		assertEquals(0, helper.getPreviousRow()[0]);

		// test insertion
		c1= new SequenceComparator("a"); 
		c2= new SequenceComparator("ab");
		levenstein= createLevenstein(c1, c2);
		helper= new LevensteinTestHelper(levenstein);
		helper.initRows();

		helper.internalReverseEditDistance(1, 1, 1, 1);
		assertTrue(Arrays.equals(new int[] { 0, SKIP }, helper.getPreviousRow()));

		helper.internalReverseEditDistance(2, 2, 1, 1);
		assertTrue(Arrays.equals(new int[] { 1, SKIP }, helper.getPreviousRow()));
	}
	


}
