/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer;

import java.util.Arrays;

import junit.framework.TestCase;


/**
 * @since 3.0
 */
public class OptimizedLevensteinTest extends TestCase {
	private final static int SKIP= Integer.MAX_VALUE;

	protected Levenstein createLevenstein(IRangeComparator left, IRangeComparator right) {
		Levenstein ls= new Levenstein(left, right);
		ls.fCellComputer= ls.fOptimizedCC;
		return ls;
	}
	
	public void testInternalEditDistance() {
		IRangeComparator c1= new SequenceComparator("abcd"); 
		IRangeComparator c2= new SequenceComparator("abcd");
		
		Levenstein levenstein= createLevenstein(c1, c2);
		levenstein.initRows();
		
		// full compare
		levenstein.internalEditDistance(1, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { SKIP, SKIP, SKIP, SKIP, 0 }, levenstein.fPreviousRow));

		// partial compare
		levenstein.internalEditDistance(1, 2, 1, 4);
		assertTrue(Arrays.equals(new int[] { SKIP, SKIP, 0, 1, 2 }, levenstein.fPreviousRow));
		
		levenstein.internalEditDistance(3, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { SKIP, SKIP, SKIP, SKIP, 2 }, levenstein.fPreviousRow));
		
		levenstein.internalEditDistance(1, 4, 1, 2);
		assertEquals(SKIP, levenstein.fPreviousRow[0]);
		assertEquals(SKIP, levenstein.fPreviousRow[1]);
		assertEquals(2, levenstein.fPreviousRow[2]);
		
		// empty right
		levenstein.internalEditDistance(1, 0, 1, 4);
		assertTrue(Arrays.equals(new int[] { 0, 1, 2, 3, 4 }, levenstein.fPreviousRow));
		
		// empty left
		levenstein.internalEditDistance(1, 4, 1, 0);
		assertEquals(4, levenstein.fPreviousRow[0]);
		
		// empty both
		levenstein.internalEditDistance(1, 0, 1, 0);
		assertEquals(0, levenstein.fPreviousRow[0]);
		
		// test insertion
		c1= new SequenceComparator("a"); 
		c2= new SequenceComparator("ab");
		levenstein= createLevenstein(c1, c2);
		levenstein.initRows();

		levenstein.internalEditDistance(1, 1, 1, 1);
		assertTrue(Arrays.equals(new int[] { SKIP, 0 }, levenstein.fPreviousRow));

		levenstein.internalEditDistance(2, 2, 1, 1);
		assertTrue(Arrays.equals(new int[] { SKIP, 1 }, levenstein.fPreviousRow));
	}
	
	public void testInternalReverseEditDistance() {
		IRangeComparator c1= new SequenceComparator("abcd"); 
		IRangeComparator c2= new SequenceComparator("abcd");
		
		Levenstein levenstein= createLevenstein(c1, c2);
		levenstein.initRows();
		
		// full compare
		levenstein.internalReverseEditDistance(1, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { 0, SKIP, SKIP, SKIP, SKIP }, levenstein.fPreviousRow));

		// partial compare
		levenstein.internalReverseEditDistance(1, 2, 1, 4);
		assertTrue(Arrays.equals(new int[] { 2, SKIP, SKIP, SKIP, SKIP }, levenstein.fPreviousRow));
		
		levenstein.internalReverseEditDistance(3, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { 2, 1, 0, SKIP, SKIP }, levenstein.fPreviousRow));
		
		levenstein.internalReverseEditDistance(1, 4, 1, 2);
		assertEquals(SKIP, levenstein.fPreviousRow[2]);
		assertEquals(SKIP, levenstein.fPreviousRow[1]);
		assertEquals(2, levenstein.fPreviousRow[0]);
		
		// empty right
		levenstein.internalReverseEditDistance(1, 0, 1, 4);
		assertTrue(Arrays.equals(new int[] { 4, 3, 2, 1, 0 }, levenstein.fPreviousRow));
		
		// empty left
		levenstein.internalReverseEditDistance(1, 4, 5, 4);
		assertEquals(4, levenstein.fPreviousRow[4]);
		levenstein.internalReverseEditDistance(1, 4, 1, 0);
		assertEquals(4, levenstein.fPreviousRow[0]);
		
		// empty both
		levenstein.internalReverseEditDistance(1, 0, 1, 0);
		assertEquals(0, levenstein.fPreviousRow[0]);

		// test insertion
		c1= new SequenceComparator("a"); 
		c2= new SequenceComparator("ab");
		levenstein= createLevenstein(c1, c2);
		levenstein.initRows();

		levenstein.internalReverseEditDistance(1, 1, 1, 1);
		assertTrue(Arrays.equals(new int[] { 0, SKIP }, levenstein.fPreviousRow));

		levenstein.internalReverseEditDistance(2, 2, 1, 1);
		assertTrue(Arrays.equals(new int[] { 1, SKIP }, levenstein.fPreviousRow));
	}
	


}
