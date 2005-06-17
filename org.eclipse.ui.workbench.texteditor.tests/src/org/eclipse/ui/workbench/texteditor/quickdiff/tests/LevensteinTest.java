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

import org.eclipse.jface.text.Assert;

import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.IRangeComparator;
import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.Levenstein;
import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.RangeDifference;
import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.RangeDifferencer;


/**
 * @since 3.1
 */
public class LevensteinTest extends TestCase {
	
	
	protected void setUp() throws Exception {
		System.out.println();
	}

	public void testEditDistance() {
		assertEditDistance(0, "", "");

		assertEditDistance(1, "a", "");

		assertEditDistance(1, "a", "b");

		assertEditDistance(1, "a", "ab");

		assertEditDistance(2, "a", "bb");

		assertEditDistance(2, "abcdefghijklmnopqrstuvwxyz", "abcdefhijklmnogpqrstuvwxyz");
		
		assertEditDistance(16, "abcdefghijklmnopqrstuvwxyz", "abcdefwxyz");

		assertEditDistance(28, "abcdefghijklmnopqrstuvwxyz", "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz");

		assertEditDistance(28, "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz");

		assertEditDistance(0, "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz", "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz");

		assertEditDistance(54, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
		
		assertEditDistance(51, "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc", "abc");

		assertEditDistance(25, "abcdefgxxxxxxxxxxxabcabcdefgxxxxxxx", "abcabcdefg");

	}
	
	public void testEditDistanceHirschberg() {
		assertEditDistanceHirschberg(0, "", "");

		assertEditDistanceHirschberg(1, "a", "");

		assertEditDistanceHirschberg(1, "a", "b");

		assertEditDistanceHirschberg(1, "a", "ab");

		assertEditDistanceHirschberg(2, "a", "bb");

		assertEditDistanceHirschberg(2, "abcdefghijklmnopqrstuvwxyz", "abcdefhijklmnogpqrstuvwxyz");
		
		assertEditDistanceHirschberg(16, "abcdefghijklmnopqrstuvwxyz", "abcdefwxyz");

		assertEditDistanceHirschberg(28, "abcdefghijklmnopqrstuvwxyz", "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz");

		assertEditDistanceHirschberg(28, "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz");

		assertEditDistanceHirschberg(0, "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz", "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz");

		assertEditDistanceHirschberg(54, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
		
		assertEditDistanceHirschberg(51, "abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc", "abc");

		assertEditDistanceHirschberg(25, "abcdefgxxxxxxxxxxxabcabcdefgxxxxxxx", "abcabcdefg");

	}
	
	public void testEditScript() {
		assertEditScript("", "");

		assertEditScript("a", "");

		assertEditScript("a", "b");

		assertEditScript("a", "ab");

		assertEditScript("a", "bb");

		assertEditScript("abcdefghijklmnopqrstuvwxyz", "abcdefhijklmnogpqrstuvwxyz");
		
		assertEditScript("abcdefghijklmnopqrstuvwxyz", "abcdefwxyz");

		assertEditScript("abcdefghijklmnopqrstuvwxyz", "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz");

		assertEditScript("abcdefghijklm0000000000000000000000000000nopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz");

		assertEditScript("abcdefghijklm0000000000000000000000000000nopqrstuvwxyz", "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz");

		assertEditScript("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
		
		assertEditScript("abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc", "abc");

		assertEditScript("abcdefgxxxxxxxxxxxabcabcdefgxxxxxxx", "abcabcdefg");

	}
	
	public void testInternalEditDistance() {
		IRangeComparator c1= new SequenceComparator("abcd"); 
		IRangeComparator c2= new SequenceComparator("abcd");
		
		Levenstein levenstein= createLevenstein(c1, c2);
		LevensteinTestHelper helper= new LevensteinTestHelper(levenstein);
		helper.initRows();
		
		// full compare
		helper.internalEditDistance(1, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { 4, 3, 2, 1, 0 }, helper.getPreviousRow()));

		// partial compare
		helper.internalEditDistance(1, 2, 1, 4);
		assertTrue(Arrays.equals(new int[] { 2, 1, 0, 1, 2 }, helper.getPreviousRow()));
		
		helper.internalEditDistance(3, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { 2, 2, 2, 3, 2 }, helper.getPreviousRow()));
		
		helper.internalEditDistance(1, 4, 1, 2);
		assertEquals(4, helper.getPreviousRow()[0]);
		assertEquals(3, helper.getPreviousRow()[1]);
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
		assertTrue(Arrays.equals(new int[] { 1, 0 }, helper.getPreviousRow()));

		helper.internalEditDistance(2, 2, 1, 1);
		assertTrue(Arrays.equals(new int[] { 1, 1 }, helper.getPreviousRow()));
	}
	
	protected Levenstein createLevenstein(IRangeComparator left, IRangeComparator right) {
		return new Levenstein(left, right);
	}

	public void testInternalReverseEditDistance() {
		IRangeComparator c1= new SequenceComparator("abcd"); 
		IRangeComparator c2= new SequenceComparator("abcd");
		
		Levenstein levenstein= createLevenstein(c1, c2);
		LevensteinTestHelper helper= new LevensteinTestHelper(levenstein);
		helper.initRows();
		
		// full compare
		helper.internalReverseEditDistance(1, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { 0, 1, 2, 3, 4 }, helper.getPreviousRow()));

		// partial compare
		helper.internalReverseEditDistance(1, 2, 1, 4);
		assertTrue(Arrays.equals(new int[] { 2, 3, 2, 2, 2 }, helper.getPreviousRow()));
		
		helper.internalReverseEditDistance(3, 4, 1, 4);
		assertTrue(Arrays.equals(new int[] { 2, 1, 0, 1, 2 }, helper.getPreviousRow()));
		
		helper.internalReverseEditDistance(1, 4, 1, 2);
		assertEquals(4, helper.getPreviousRow()[2]);
		assertEquals(3, helper.getPreviousRow()[1]);
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
		assertTrue(Arrays.equals(new int[] { 0, 1 }, helper.getPreviousRow()));

		helper.internalReverseEditDistance(2, 2, 1, 1);
		assertTrue(Arrays.equals(new int[] { 1, 1 }, helper.getPreviousRow()));
	}
	
	public void testEditScriptHirschberg() {
		assertEditScriptHirschberg("abc", "abc");
		
		assertEditScriptHirschberg("a", "b");
		
		assertEditScriptHirschberg("", "");

		assertEditScriptHirschberg("a", "");

		assertEditScriptHirschberg("a", "ab");

		assertEditScriptHirschberg("a", "bb");

		assertEditScriptHirschberg("abcdefghijklmnopqrstuvwxyz", "abcdefhijklmnogpqrstuvwxyz");
		
		assertEditScriptHirschberg("abcdefghijklmnopqrstuvwxyz", "abcdefwxyz");

		assertEditScriptHirschberg("abcdefghijklmnopqrstuvwxyz", "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz");

		assertEditScriptHirschberg("abcdefghijklm0000000000000000000000000000nopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz");

		assertEditScriptHirschberg("abcdefghijklm0000000000000000000000000000nopqrstuvwxyz", "abcdefghijklm0000000000000000000000000000nopqrstuvwxyz");

		assertEditScriptHirschberg("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
		
		assertEditScriptHirschberg("abcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabcabc", "abc");

		assertEditScriptHirschberg("abcdefgxxxxxxxxxxxabcabcdefgxxxxxxx", "abcabcdefg");

	}
	
	private void assertEditScript(String s1, String s2) {
		SequenceComparator sc1= new SequenceComparator(s1);
		SequenceComparator sc2= new SequenceComparator(s2);
		RangeDifference[] expected= RangeDifferencer.findDifferences(sc1, sc2);
		Levenstein levenstein= createLevenstein(sc1,sc2);
		RangeDifference[] tested= levenstein.editScript();
		
		assertTrue(Arrays.equals(expected, tested));
	}

	private void assertEditScriptHirschberg(String s1, String s2) {
		SequenceComparator sc1= new SequenceComparator(s1);
		SequenceComparator sc2= new SequenceComparator(s2);
		RangeDifference[] expected= RangeDifferencer.findDifferences(sc1, sc2);
		Levenstein levenstein= createLevenstein(sc1,sc2);
		RangeDifference[] tested= levenstein.editScriptHirschberg();
		
		assertTrue(Arrays.equals(expected, tested));
	}

	private void assertEditDistance(int expected, String s1, String s2) {
		int dist= computeDistance(new SequenceComparator(s1), new SequenceComparator(s2));
		assertEquals(expected, dist);
	}

	private void assertEditDistanceHirschberg(int expected, String s1, String s2) {
		int dist= computeDistanceHirschberg(new SequenceComparator(s1), new SequenceComparator(s2));
		assertEquals(expected, dist);
	}

	private int computeDistance(SequenceComparator comp1, SequenceComparator comp2) {
		SequenceComparator s1= comp1;
		SequenceComparator s2= comp2;
		Levenstein levenstein= createLevenstein(s1,s2);
		int dist= levenstein.editDistance();
		return dist;
	}

	private int computeDistanceHirschberg(SequenceComparator comp1, SequenceComparator comp2) {
		SequenceComparator s1= comp1;
		SequenceComparator s2= comp2;
		Levenstein levenstein= createLevenstein(s1, s2);
		int dist= levenstein.editDistanceHirschberg();
		return dist;
	}

}


class SequenceComparator implements IRangeComparator {
	private final CharSequence fSequence;
	
	
	public SequenceComparator(CharSequence string) {
		Assert.isNotNull(string);
		fSequence= string;
	}

	public int getRangeCount() {
		return fSequence.length();
	}

	public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
		return fSequence.charAt(thisIndex) == ((SequenceComparator) other).fSequence.charAt(otherIndex);
	}

	public boolean skipRangeComparison(int length, int maxLength, IRangeComparator other) {
		return false;
	}
	
	public String toString() {
		return "SequenceComparator [" + fSequence + "]";
	}
	
}
