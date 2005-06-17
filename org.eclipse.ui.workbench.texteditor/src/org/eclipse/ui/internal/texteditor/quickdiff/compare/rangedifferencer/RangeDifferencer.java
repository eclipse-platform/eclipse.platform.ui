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
package org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer;

import java.util.*;

import org.eclipse.jface.util.Assert;

import org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer.LinkedRangeFactory.LowMemoryException;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A <code>RangeDifferencer</code> finds the differences between two or three <code>IRangeComparator</code>s.
 * <p>
 * To use the differencer, clients provide an <code>IRangeComparator</code>
 * that breaks their input data into a sequence of comparable entities. The differencer
 * returns the differences among these sequences as an array of <code>RangeDifference</code> objects
 * (<code>findDifferences</code> methods).
 * Every <code>RangeDifference</code> represents a single kind of difference
 * and the corresponding ranges of the underlying comparable entities in the
 * left, right, and optionally ancestor sides.
 * <p>
 * Alternatively, the <code>findRanges</code> methods not only return objects for
 * the differing ranges but for non-differing ranges too.
 * <p>
 * The algorithm used is an objectified version of one described in:
 * <it>A File Comparison Program,</it> by Webb Miller and Eugene W. Myers,
 * Software Practice and Experience, Vol. 15, Nov. 1985.
 *
 * @see IRangeComparator
 * @see RangeDifference
 * @since 3.0
 */
public final class RangeDifferencer {

	private static final RangeDifference[] EMPTY_RESULT= new RangeDifference[0];

	/* (non Javadoc)
	 * Non instantiateable!
	 */
	private RangeDifferencer() {
	}

	/**
	 * Finds the differences between two <code>IRangeComparator</code>s.
	 * The differences are returned as an array of <code>RangeDifference</code>s.
	 * If no differences are detected an empty array is returned.
	 *
	 * @param left the left range comparator
	 * @param right the right range comparator
	 * @return an array of range differences, or an empty array if no differences were found
	 */
	public static RangeDifference[] findDifferences(IRangeComparator left, IRangeComparator right) {
		return findDifferences((IProgressMonitor)null, left, right);
	}

	/**
	 * Finds the differences between two <code>IRangeComparator</code>s.
	 * The differences are returned as an array of <code>RangeDifference</code>s.
	 * If no differences are detected an empty array is returned.
	 *
	 * @param pm if not <code>null</code> used to report progress
	 * @param left the left range comparator
	 * @param right the right range comparator
	 * @return an array of range differences, or an empty array if no differences were found
	 * @since 2.0
	 */
	private static RangeDifference[] findDifferences(IProgressMonitor pm, IRangeComparator left, IRangeComparator right) {
		try {
			return findDifferencesUkkonen(pm, left, right);
		} catch (LowMemoryException e) {
			return Levenstein.findDifferences(pm, left, right);
		}
	}

	/**
	 * Finds the differences between two <code>IRangeComparator</code>s.
	 * The differences are returned as an array of <code>RangeDifference</code>s.
	 * If no differences are detected an empty array is returned.
	 *
	 * @param pm if not <code>null</code> used to report progress
	 * @param left the left range comparator
	 * @param right the right range comparator
	 * @return an array of range differences, or an empty array if no differences were found
	 * @throws LowMemoryException if the differencer runs out of memory
	 * @since 2.0
	 */
	private static RangeDifference[] findDifferencesUkkonen(IProgressMonitor pm, IRangeComparator left, IRangeComparator right) throws LowMemoryException {

		// assert that both IRangeComparators are of the same class
		Assert.isTrue(right.getClass().equals(left.getClass()));

		int rightSize= right.getRangeCount();
		int leftSize= left.getRangeCount();
		//
		// Differences matrix:
		// only the last d of each diagonal is stored, i.e., lastDiagonal[k] = row of d
		//
		int diagLen= 2 * Math.max(rightSize, leftSize); // bound on the size of edit script
		int maxDiagonal= diagLen;
		int lastDiagonal[]= new int[diagLen + 1]; // the row containing the last d
		Arrays.fill(lastDiagonal, -1);
		// on diagonal k (lastDiagonal[k] = row)
		int origin= diagLen / 2; // origin of diagonal 0

		// script corresponding to d[k]
		LinkedRangeDifference script[]= new LinkedRangeDifference[diagLen + 1];
		int row, col;

		// find common prefix
		for (row= 0; row < rightSize && row < leftSize && rangesEqual(right, row, left, row); ++row) {
			// do nothing
		}

		lastDiagonal[origin]= row;
		script[origin]= null;
		int lower= (row == rightSize) ? origin + 1 : origin - 1;
		int upper= (row == leftSize) ? origin - 1 : origin + 1;

		if (lower > upper)
			return EMPTY_RESULT;

		//System.out.println("findDifferences: " + maxDiagonal + " " + lower + " " + upper);
		LinkedRangeFactory factory= new LinkedRangeFactory();

		// for each value of the edit distance
		for (int d= 1; d <= maxDiagonal; ++d) { // d is the current edit distance

			if (pm != null)
				pm.worked(1);

			if (right.skipRangeComparison(d, maxDiagonal, left))
				return EMPTY_RESULT; // should be something we already found

			// for each relevant diagonal (-d, -d+2 ..., d-2, d)
			for (int k= lower; k <= upper; k += 2) { // k is the current diagonal
				LinkedRangeDifference edit;

				if (pm != null && pm.isCanceled())
					return EMPTY_RESULT;

				if (k == origin - d || k != origin + d && lastDiagonal[k + 1] >= lastDiagonal[k - 1]) {
					//
					// move down
					//
					row= lastDiagonal[k + 1] + 1;
					edit= factory.newRange(script[k + 1], LinkedRangeDifference.DELETE);
				} else {
					//
					// move right
					//
					row= lastDiagonal[k - 1];
					edit= factory.newRange(script[k - 1], LinkedRangeDifference.INSERT);
				}
				col= row + k - origin;
				edit.fRightStart= row;
				edit.fLeftStart= col;
				Assert.isTrue(k >= 0 && k <= maxDiagonal);
				script[k]= edit;

				// slide down the diagonal as far as possible
				while (row < rightSize && col < leftSize && rangesEqual(right, row, left, col) == true) {
					++row;
					++col;
				}

				Assert.isTrue(k >= 0 && k <= maxDiagonal); // Unreasonable value for diagonal index
				lastDiagonal[k]= row;

				if (row == rightSize && col == leftSize) {
					//showScript(script[k], right, left);
					return createDifferencesRanges(script[k]);
				}
				if (row == rightSize)
					lower= k + 2;
				if (col == leftSize)
					upper= k - 2;
			}
			--lower;
			++upper;
		}
		// too many differences
		Assert.isTrue(false);
		return null;
	}

	/**
	 * Finds the differences among two <code>IRangeComparator</code>s.
	 * In contrast to <code>findDifferences</code>, the result
	 * contains <code>RangeDifference</code> elements for non-differing ranges too.
	 *
	 * @param left the left range comparator
	 * @param right the right range comparator
	 * @return an array of range differences
	 */
	public static List findRanges(IRangeComparator left, IRangeComparator right) {
		return findRanges((IProgressMonitor)null, left, right);
	}

	/**
	 * Finds the differences among two <code>IRangeComparator</code>s.
	 * In contrast to <code>findDifferences</code>, the result
	 * contains <code>RangeDifference</code> elements for non-differing ranges too.
	 *
	 * @param pm if not <code>null</code> used to report progress
	 * @param left the left range comparator
	 * @param right the right range comparator
	 * @return an array of range differences
	 * @since 2.0
	 */
	public static List findRanges(IProgressMonitor pm, IRangeComparator left, IRangeComparator right) {
		RangeDifference[] in= findDifferences(pm, left, right);
		List out= new ArrayList();

		RangeDifference rd;

		int mstart= 0;
		int ystart= 0;

		for (int i= 0; i < in.length; i++) {
			RangeDifference es= in[i];

			rd= new RangeDifference(RangeDifference.NOCHANGE, mstart, es.rightStart() - mstart, ystart, es.leftStart() - ystart);
			if (rd.maxLength() != 0)
				out.add(rd);

			out.add(es);

			mstart= es.rightEnd();
			ystart= es.leftEnd();
		}
		rd= new RangeDifference(RangeDifference.NOCHANGE, mstart, right.getRangeCount() - mstart, ystart, left.getRangeCount() - ystart);
		if (rd.maxLength() > 0)
			out.add(rd);

		return out;
	}

	//---- private methods

	/**
	 * Creates an array <code>DifferencesRanges</code> out of the
	 * <code>LinkedRangeDifference</code>. It coalesces adjacent changes. In
	 * addition, indices are changed such that the ranges are 1) open, i.e, the
	 * end of the range is not included, and 2) are zero based.
	 *
	 * @param start the start difference
	 * @return the created array of difference ranges
	 */
	private static RangeDifference[] createDifferencesRanges(LinkedRangeDifference start) {

		LinkedRangeDifference ep= reverseDifferences(start);
		ArrayList result= new ArrayList();
		RangeDifference es= null;

		while (ep != null) {
			es= new RangeDifference(RangeDifference.CHANGE);

			if (ep.isInsert()) {
				es.fRightStart= ep.fRightStart + 1;
				es.fLeftStart= ep.fLeftStart;
				RangeDifference b= ep;
				do {
					ep= ep.getNext();
					es.fLeftLength++;
				} while (ep != null && ep.isInsert() && ep.fRightStart == b.fRightStart);
			} else {
				es.fRightStart= ep.fRightStart;
				es.fLeftStart= ep.fLeftStart;

				RangeDifference a= ep;
				//
				// deleted lines
				//
				do {
					a= ep;
					ep= ep.getNext();
					es.fRightLength++;
				} while (ep != null && ep.isDelete() && ep.fRightStart == a.fRightStart + 1);

				boolean change= (ep != null && ep.isInsert() && ep.fRightStart == a.fRightStart);

				if (change) {
					RangeDifference b= ep;
					//
					// replacement lines
					//
					do {
						ep= ep.getNext();
						es.fLeftLength++;
					} while (ep != null && ep.isInsert() && ep.fRightStart == b.fRightStart);
				} else {
					es.fLeftLength= 0;
				}
				es.fLeftStart++; // meaning of range changes from "insert after", to "replace with"

			}
			//
			// the script commands are 1 based, subtract one to make them zero based
			//
			es.fRightStart--;
			es.fLeftStart--;
			result.add(es);
		}
		return (RangeDifference[]) result.toArray(EMPTY_RESULT);
	}

	/**
	 * Tests whether two ranges at the given indices are equal.
	 *
	 * @param a the first comparator
	 * @param ai the index of the first range
	 * @param b the second comparator
	 * @param bi the index of the second range
	 * @return <code>true</code> if the ranges are equal, <code>false</code> otherwise
	 */
	private static boolean rangesEqual(IRangeComparator a, int ai, IRangeComparator b, int bi) {
		return a.rangesEqual(ai, b, bi);
	}

	/**
	 * Reverses the list of range differences thus that the given start difference becomes the
	 * end of the list.
	 *
	 * @param start the start of the list
	 * @return the reverted list
	 */
	private static LinkedRangeDifference reverseDifferences(LinkedRangeDifference start) {
		LinkedRangeDifference ep, behind, ahead;

		ahead= start;
		ep= null;
		while (ahead != null) {
			behind= ep;
			ep= ahead;
			ahead= ahead.getNext();
			ep.setNext(behind);
		}
		return ep;
	}
}

