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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.text.Assert;




/**
 * Levenstein distance and edit script computation using a dynamic programming algorithm.
 * The algorithm is O(n*m) in time where n and m are the number of elements in
 * the two ranges to compare. It does not implement the greedy Ukkonen algorithm.
 *
 * @since 3.1
 */
public final class Levenstein {
	/* debug output */
	private static final boolean DEBUG= false;
	private static final boolean MATRIX= false;

	/* edit cost constants */
	private static final int COST_DELETE= 1;
	private static final int COST_INSERT= 1;
	private static final int COST_CHANGE= 1;
	private static final int SKIP= Integer.MAX_VALUE;

	private static final RangeDifference[] EMPTY_DIFFERENCES= new RangeDifference[0];
	private static final int NO_DISTANCE= 0;

	private interface CellComputer {
		int computeCell(int row, int col);
	}

	private final class DefaultCellComputer implements CellComputer {
		public int computeCell(int row, int column) {
			if (row == fRowStart)
				return computeNullRow(column);
			else if (column == fColStart)
				return computeNullColumn(row);
			else
				return computeInnerCell(row, column);
		}

		private int computeNullRow(int column) {
			// initialize first row, [0,i] = i if it is valid
			return Math.abs(column - fColStart);
		}

		private int computeNullColumn(int row) {
			// initialize first column
			return Math.abs(row - fRowStart);
		}

		private int computeInnerCell(int row, int col) {
			int fromAbove= sum(getAt(row - fStep, col), COST_INSERT);
			int fromLeft= sum(getAt(row, col - fStep), COST_DELETE);
			int minDiag= getAt(row - fStep, col - fStep);

			int minCellValue= Math.min(Math.min(fromAbove, fromLeft), minDiag);
			int minCost= minCost(row, col, minCellValue);

			if (minCellValue == fromAbove || minCellValue == fromLeft)
				return minCellValue;

			Assert.isTrue(minCellValue == minDiag && fromAbove >= minDiag && fromLeft >= minDiag);

			int nextCharCost= rangesEqual(row, col) ? 0 : COST_CHANGE;
			minCost= sum(minCost, nextCharCost);
			int cost= minDiag + nextCharCost;
			return cost;
		}
	}

	/**
	 * Reduces the needed comparisons - can not be used for hirschberg as we
	 * don't have global values there.
	 */
	private final class OptimizedCellComputer implements CellComputer {
		public int computeCell(int row, int column) {
			if (row == fRowStart)
				return computeNullRow(column);
			else if (column == fColStart)
				return computeNullColumn(row);
			else
				return computeInnerCell(row, column);
		}

		private int computeNullRow(int column) {
			// initialize first row, [0,i] = i if it is valid
			if (minCost(fRowStart, column, Math.abs(column - fColStart)) > fMaxCost)
				return Levenstein.SKIP;
			return Math.abs(column - fColStart);
		}

		private int computeNullColumn(int row) {
			// initialize first column
			if (minCost(row, fColStart, Math.abs(row - fRowStart)) > fMaxCost)
				return Levenstein.SKIP;
			return Math.abs(row - fRowStart);
		}

		private int computeInnerCell(int row, int col) {
			int fromAbove= sum(getAt(row - fStep, col), Levenstein.COST_INSERT);
			int fromLeft= sum(getAt(row, col - fStep), Levenstein.COST_DELETE);
			int minDiag= getAt(row - fStep, col - fStep);

			int minCellValue= Math.min(Math.min(fromAbove, fromLeft), minDiag);
			int minCost= minCost(row, col, minCellValue);

			if (minCost > fMaxCost) {
				return Levenstein.SKIP;
			} else if (minCellValue == fromAbove || minCellValue == fromLeft) {
				return minCellValue;
			} else {
				Assert.isTrue(minCellValue == minDiag && fromAbove >= minDiag && fromLeft >= minDiag);

				int nextCharCost= rangesEqual(row, col) ? 0 : Levenstein.COST_CHANGE;
				minCost= Levenstein.sum(minCost, nextCharCost);
				if (minCost > fMaxCost)
					return Levenstein.SKIP;
				int cost= minDiag + nextCharCost;
				fMaxCost= Math.min(fMaxCost, maxCost(row, col, cost));
				return cost;
			}
		}
	}

	/* the domain ranges we compare */
	private IRangeComparator fLeft;
	private IRangeComparator fRight;
	private IProgressMonitor fProgressMonitor;

	/* algorithmic variables - may or may not be used by a method, always
	 * nulled out by clear().
	 */
	/* package visible for testing */

	/** The matrix for full blown N * M edit distance and script computation. */
	int[][] fMatrix;
	/** The two columns for dynamic algorithms - the last row. */
	int[] fPreviousRow;
	/** The two columns for dynamic algorithms - the current row. */
	int[] fCurrentRow;
	/** Used by hirschberg's algorithm to store the result of one run. */
	private int[] fResultRow;
	/** Direction of the matrix calculation - either <code>1</code> or <code>-1</code>. */
	private int fStep;
	/** The current row of the dynamic matrix calculation. */
	private int fRow;
	/** The first row of the matrix to calculate. */
	private int fRowStart;
	/** The last (inclusive) row of the matrix. */
	private int fRowEnd;
	/** The first column of the matrix to calculate. */
	private int fColStart;
	/** The last (inclusive) column of the matrix. */
	private int fColEnd;
	/**
	 * Maximum cost of the remaining computation given the current state of the
	 * computation. For edit distance calculation, this may be used to prune
	 * impossible cells.
	 */
	private int fMaxCost;

	/* statistics collection */
	/** Keeps track of the number of needed comparisons. */
	private long fComparisons;

	/**
	 * For each row, Hirschberg stores the column of the optimal alignment
	 * in the next row of the matrix.
	 */
	private int[] fOptimalSplitColumn;
	/**
	 * For each row, this stores whether the domain elements at the [row,column]
	 * returned equality, where column is the value in <code>fOptimalSplitColumn</code>.
	 */
	private boolean[] fOptimalSplitValues;
	/** List of differences computed by the walkback methods. */
	private List fDiffs;

	/** Normal matrix cell computer. */
	final CellComputer fStandardCC= new DefaultCellComputer();
	/** Optimized cell computer for that prunes impossible cells. */
	final CellComputer fOptimizedCC= new OptimizedCellComputer();
	/** The current cell computer. */
	CellComputer fCellComputer= fStandardCC;

	/**
	 * Convenience method to compute the edit script between two range
	 * comparators, see <code>RangeDifferencer</code>.
	 *
	 * @param left the left hand side domain range
	 * @param right the right hand side domain range
	 * @return the edit script from left to right
	 */
	public static RangeDifference[] findDifferences(IRangeComparator left, IRangeComparator right) {
		Levenstein levenstein= new Levenstein(left, right);
		return levenstein.editScriptHirschberg();
	}

	/**
	 * Convenience method to compute the edit script between two range
	 * comparators, see <code>RangeDifferencer</code>.
	 *
	 * @param pm a progress monitor, or <code>null</code> if no progress should be reported
	 * @param left the left hand side domain range
	 * @param right the right hand side domain range
	 * @return the edit script from left to right
	 */
	public static RangeDifference[] findDifferences(IProgressMonitor pm, IRangeComparator left, IRangeComparator right) {
		Levenstein levenstein= new Levenstein(pm, left, right);
		return levenstein.editScriptHirschberg();
	}

	/**
	 * Create a new differ that operates on the two domain range comparators
	 * given.
	 *
	 * @param left the left domain range
	 * @param right the right domain range
	 */
	public Levenstein(IRangeComparator left, IRangeComparator right) {
		this(null, left, right);
	}

	/**
	 * Create a new differ that operates on the two domain range comparators
	 * given.
	 *
	 * @param pm a progress monitor, or <code>null</code> if no progress should be reported
	 * @param left the left domain range
	 * @param right the right domain range
	 */
	public Levenstein(IProgressMonitor pm, IRangeComparator left, IRangeComparator right) {
		if (left == null || right == null)
			throw new NullPointerException();
		fLeft= left;
		fRight= right;
		if (pm != null)
			fProgressMonitor= pm;
		else
			fProgressMonitor= new NullProgressMonitor();
	}

	/**
	 * Computes the edit distance.
	 *
	 * @return the edit distance of the two range comparators
	 */
	public int editDistance() {
		try {
			fCellComputer= fOptimizedCC;
			initRows();

			if (MATRIX) printHeader(fLeft, fRight);

			internalEditDistance(1, fRight.getRangeCount(), 1, fLeft.getRangeCount());

			if (fProgressMonitor.isCanceled())
				return NO_DISTANCE;

			if (DEBUG)
				System.out.println("" + fComparisons + " comparisons");  //$NON-NLS-1$//$NON-NLS-2$

			return getAt(fRowEnd, fColEnd);
		} finally {
			clear();
		}
	}

	/**
	 * Computes the edit script. This is quadratic in space and time.
	 *
	 * @return the shortest edit script between the two range comparators
	 */
	public RangeDifference[] editScript() {
		try {
			fCellComputer= fOptimizedCC;
			initMatrix();

			if (MATRIX)
				printHeader(fLeft, fRight);

			// build the matrix
			internalEditDistance(1, fRight.getRangeCount(), 1, fLeft.getRangeCount());

			if (fProgressMonitor.isCanceled())
				return EMPTY_DIFFERENCES;

			if (DEBUG)
				System.out.println("" + fComparisons + " comparisons"); //$NON-NLS-1$//$NON-NLS-2$

			RangeDifference[] script= walkback();

			return script;

		} finally {
			clear();
		}
	}

	/**
	 * Computes the edit script. This is quadratic in time but linear in space;
	 * it is about twice as slow as the <code>editScript</code> method.
	 *
	 * @return the shortest edit script between the two range comparators
	 */
	public RangeDifference[] editScriptHirschberg() {
		try {
			fCellComputer= fStandardCC;

			initRows();
			fResultRow= new int[fCurrentRow.length];
			fOptimalSplitColumn= new int[fRight.getRangeCount() + 1];
			fOptimalSplitValues= new boolean[fRight.getRangeCount() + 1];

			hirschberg(1, fRight.getRangeCount(), 1, fLeft.getRangeCount());

			if (fProgressMonitor.isCanceled())
				return EMPTY_DIFFERENCES;

			if (DEBUG)
				System.out.println("" + fComparisons + " comparisons"); //$NON-NLS-1$//$NON-NLS-2$

			RangeDifference[] script= buildDifferencesHirschberg();

			return script;

		} finally {
			clear();
		}
	}

	public int editDistanceHirschberg() {
		try {
			fCellComputer= fStandardCC;

			initRows();
			fResultRow= new int[fLeft.getRangeCount() + 1];
			fOptimalSplitColumn= new int[fRight.getRangeCount() + 1];
			fOptimalSplitValues= new boolean[fRight.getRangeCount() + 1];

			int dist= hirschberg(1, fRight.getRangeCount(), 1, fLeft.getRangeCount());

			if (fProgressMonitor.isCanceled())
				return NO_DISTANCE;

			if (DEBUG)
				System.out.println("" + fComparisons + " comparisons"); //$NON-NLS-1$//$NON-NLS-2$

			return dist;

		} finally {
			clear();
		}
	}

	void initMatrix() {
		initMatrix(fRight.getRangeCount() + 1, fLeft.getRangeCount() + 1);
	}

	void initMatrix(int rows, int columns) {
		if (fMatrix == null || fMatrix.length < rows || fMatrix[0].length < columns)
			fMatrix= new int[rows][columns];
	}

	void initRows() {
		initRows(fLeft.getRangeCount() + 1);
	}

	void initRows(int columns) {
		if (fCurrentRow == null || fCurrentRow.length < columns)
			fCurrentRow= new int[columns];
		if (fPreviousRow == null || fPreviousRow.length < columns)
			fPreviousRow= new int[columns];
	}

	/*
	 * Fill the matrix, but do not allocate it.
	 */
	void internalEditDistance(int rStart, int rEnd, int lStart, int lEnd) {

		Assert.isTrue(rStart <= rEnd + 1);
		Assert.isTrue(lStart <= lEnd + 1);

		// build the matrix
		fStep= 1;
		fRowStart= rStart - fStep;
		fRowEnd= rEnd;

		fColStart= lStart - fStep;
		fColEnd= lEnd;

		fMaxCost= maxCost(fRowStart, fColStart, 0);

		for (fRow= fRowStart; fRow <= fRowEnd; fRow += fStep) { // for every row

			fProgressMonitor.worked(1);

			for (int col= fColStart; col <= fColEnd; col += fStep) { // for every column

				if (fProgressMonitor.isCanceled())
					return;

				setAt(fRow, col, fCellComputer.computeCell(fRow, col));
			}

			if (MATRIX) printRow();

			swapRows();
		}
	}

	/*
	 * Fill the matrix, but do not allocate it.
	 */
	void internalReverseEditDistance(int rStart, int rEnd, int lStart, int lEnd) {

		Assert.isTrue(rStart <= rEnd + 1);
		Assert.isTrue(lStart <= lEnd + 1);

		// build the matrix
		fStep= -1;
		fRowStart= rEnd - fStep;
		fRowEnd= rStart;

		fColStart= lEnd - fStep;
		fColEnd= lStart;

		fMaxCost= maxCost(fRowStart, fColStart, 0);

		for (fRow= fRowStart; fRow >= fRowEnd; fRow += fStep) { // for every row

			fProgressMonitor.worked(1);

			for (int col= fColStart; col >= fColEnd; col += fStep) { // for every column

				if (fProgressMonitor.isCanceled())
					return;

				setAt(fRow, col, fCellComputer.computeCell(fRow, col));
			}

			if (MATRIX) printRow();

			swapRows();
		}
	}

	private void swapRows() {
		int[] tmp;
		tmp= fPreviousRow;
		fPreviousRow= fCurrentRow;
		fCurrentRow= tmp;
	}

	private void clear() {
		fPreviousRow= null;
		fCurrentRow= null;
		fMatrix= null;
		fDiffs= null;
		fResultRow= null;
		fOptimalSplitColumn= null;
		fOptimalSplitValues= null;
	}

	/* access methods for the compare algorithm */

	/**
	 * Returns the matrix value for [row, column]. Note that not the entire
	 * matrix may be available at all times.
	 *
	 * @param row the row (right domain index)
	 * @param column (left domain index)
	 * @return the matrix value for the given row and column
	 */
	private int getAt(int row, int column) {

		// shift reverse iteration towards left by one
		if (fStep < 0)
			column--;

		if (fMatrix != null)
			return fMatrix[row][column];

		if (row == fRow)
			return fCurrentRow[column];

		if (row == fRow - fStep && ((fStep > 0 && row >= fRowStart && row <= fRowEnd) || fStep < 0 && row <= fRowStart && row >= fRowEnd))
			return fPreviousRow[column];

		Assert.isTrue(false, "random access to matrix not allowed"); //$NON-NLS-1$
		return SKIP; // dummy
	}

	/**
	 * Sets the matrix value at [row, column]. Note that not the entire
	 * matrix may be available at all times.
	 *
	 * @param row the row (right domain index)
	 * @param column (left domain index)
	 * @param value the value to set
	 */
	private void setAt(int row, int column, int value) {

		// shift reverse iteration towards left by one
		if (fStep < 0)
			column--;

		if (fMatrix != null) {
			fMatrix[row][column]= value;
		} else {
			if (row == fRow)
				fCurrentRow[column]= value;
			else if (row == fRow - fStep
					&& ((fStep > 0 && row >= fRowStart && row <= fRowEnd)
					  || fStep < 0 && row <= fRowStart && row >= fRowEnd))
				fPreviousRow[column]= value;
			else
				Assert.isTrue(false, "random access to matrix not allowed"); //$NON-NLS-1$
		}
	}

	/*
	 * Compares the two domain element ranges corresponding to the cell at
	 * [r,l], that is the (zero-based) elements at r - 1 and l - 1.
	 */
	private boolean rangesEqual(int r, int l) {
		fComparisons++;
		return fLeft.rangesEqual(l - 1, fRight, r - 1);
	}

	/*
	 * Adds two cell cost values, never exceeding SKIP.
	 */
	private static int sum(int c1, int c2) {
		int sum= c1 + c2;
		if (sum < 0)
			return SKIP;
		return sum;
	}

	/*
	 * Computes the best possible edit distance from cell [r,l] if getting
	 * there has cost cCur.
	 */
	private int minCost(int r, int l, int cCur) {
		// minimal cost from cell [r,l] to [rCount, lCount] if cell cost == cost
		// Assume that the minimum of the remaining columns / rows are equal, and just
		// the rest of the ranges has to be inserted / deleted
		if (cCur == SKIP)
			return SKIP;
		return cCur + Math.abs((fRowEnd - r) - (fColEnd - l)) * COST_INSERT; // can be either insert or delete
	}

	/*
	 * Computes the worst possible edit distance from cell [r,l] if getting
	 * there has cost cCur.
	 */
	private int maxCost(int r, int l, int cCur) {
		// maximal cost from cell [r,l] to [rCount, lCount] if cell cost == cost
		// maximal additional cost is the maximum remaining columns / rows
		if (cCur == SKIP)
			return SKIP;
		return cCur + Math.max(Math.abs(fRowEnd - r), Math.abs(fColEnd - l)) * COST_CHANGE;
	}

	/* classic implementation */

	private RangeDifference[] walkback() {
		fDiffs= new LinkedList();

		int row= fRowEnd, col= fColEnd;
		RangeDifference difference= null;

		int cell= fMatrix[row][col]; // edit distance

		while (row > 0 || col > 0) {
			int diag, above, left;

			if (row == 0) {
				// slide deletes along row 0
				diag= SKIP;
				above= SKIP;
				left= col - 1;
			} else if (col == 0) {
				// slide inserts along column 0
				diag= SKIP;
				above= row - 1;
				left= SKIP;
			} else {
				// inner cells
				diag= fMatrix[row - 1][col - 1];
				above= fMatrix[row - 1][col];
				left= fMatrix[row][col - 1];
			}

			if (left == cell - 1 && left <= diag && left <= above) {
				// delete
				col--;
				difference= getChange(difference);
				difference.fLeftStart= col;
				difference.fLeftLength++;
				difference.fRightStart= row;

				cell= left;
			} else if (above == cell - 1 && above <= diag) {
				// insert
				row--;
				difference= getChange(difference);
				difference.fLeftStart= col;
				difference.fRightStart= row;
				difference.fRightLength++;

				cell= above;
			} else {
				col--;
				row--;
				if (cell == diag) {
					// match
					// alternatively, create NOCHANGE ranges for findRanges
					difference= null;
				} else if (cell == diag + 1) {
					// change
					difference= getChange(difference);
					difference.fLeftStart= col;
					difference.fLeftLength++;
					difference.fRightStart= row;
					difference.fRightLength++;
				} else {
					Assert.isTrue(false, "illegal matrix"); //$NON-NLS-1$
				}

				cell= diag;
			}

		}

		return (RangeDifference[]) fDiffs.toArray(new RangeDifference[fDiffs.size()]);
	}

	private RangeDifference getChange(RangeDifference difference) {
		if (difference != null)
			return difference;

		difference= new RangeDifference(RangeDifference.CHANGE);
		fDiffs.add(0, difference);
		return difference;
	}

	/* hirschberg's algorithm */

	private int hirschberg(int rStart, int rEnd, int lStart, int lEnd) {

		/* trivial cases */

		if (rEnd < rStart) {
			// right range is empty
			return lEnd - lStart + 1;
		} else if (rStart == rEnd) {
			// right has length 1: look for a match and split
			internalEditDistance(rStart, rEnd, lStart, lEnd);
			int distance= SKIP;
			for (int col= lStart - 1; col <= lEnd; col++) {
				distance= fPreviousRow[col];
				if (distance == 0) {
					fOptimalSplitColumn[rStart]= col;
					fOptimalSplitValues[rStart]= true;
					return 0;
				}
			}
			fOptimalSplitColumn[rStart]= lEnd;
			fOptimalSplitValues[rStart]= false;
			if (distance == SKIP)
				return 1;
			return distance;
		}
//		else if (lEnd < lStart) {
//			// left is empty // perhaps not necessary
//			Arrays.fill(fOptimalSplitColumn, rStart, rEnd + 1, lEnd);
//			Arrays.fill(fOptimalSplitValues, rStart, rEnd + 1, false);
//			return rEnd - rStart + 1;
//		}

		/* divide & conquer */

		// split rows at half
		int rowSplit= (rStart + rEnd + 1) / 2 - 1;

		// compute edit distance of (r1,left) in linear space into fPreviousRow
		internalEditDistance(rStart, rowSplit, lStart, lEnd);
		int[] tmp= fPreviousRow;
		fPreviousRow= fResultRow;
		fResultRow= tmp;
		// compute backwards edit distance of (r2,left) in linear space into fPreviousRow
		internalReverseEditDistance(rowSplit + 1, rEnd, lStart, lEnd);

		// find optimal alignment - the column in which to split the
		// left hand side
		int columnSplit= SKIP, distance= SKIP;
		for (int col= lStart - 1; col <= lEnd; col++) {
			int sum= sum(fResultRow[col], fPreviousRow[col ]);
			if (sum < distance) {
				distance= sum;
				columnSplit= col;
			}
		}

		if (fProgressMonitor.isCanceled())
			return NO_DISTANCE;

		Assert.isTrue(distance != SKIP);
		Assert.isTrue(columnSplit != SKIP);

		if (distance == 0) {
			// optimize for large unchanged parts
			// no further partitioning needed, this part is equal
			Assert.isTrue(rEnd - rStart == lEnd - lStart);
			int col= lStart; int row= rStart;
			while (row <= rEnd) {
				fOptimalSplitColumn[row]= col;
				fOptimalSplitValues[row]= true;
				col++;
				row++;
			}
			return distance;
		}

		// store alignment: from [rowSplit, ?] connect to [rowSplit + 1, columnSplit]
		fOptimalSplitColumn[rowSplit]= columnSplit;
		fOptimalSplitValues[rowSplit]= false;

		// divide at column & conquer
		// TODO guard against stack overflow
		hirschberg(rStart, rowSplit, lStart, columnSplit);
		hirschberg(rowSplit + 1, rEnd, columnSplit + 1, lEnd);

		return distance;
	}

	private RangeDifference[] buildDifferencesHirschberg() {
		fDiffs= new LinkedList();

		RangeDifference difference= null;
		int previousColumn= 0;

		for (int row= 1; row < fOptimalSplitColumn.length; row++) {
			int previousRow= row - 1;
			int column= fOptimalSplitColumn[row]; // from (row-1), jump to column (column) in row (row)

			if (column == previousColumn + 1) {
				// diagonal
				if (fOptimalSplitValues[row]) {
					// match
					// alternatively, create NOCHANGE ranges for findRanges
					difference= null;
				} else {
					// change
					difference= getChange(difference, previousRow, previousColumn);
					difference.fLeftLength++;
					difference.fRightLength++;
				}
			} else if (column == previousColumn) {
				// downwards / insert
				difference= getChange(difference, previousRow, previousColumn);
				difference.fRightLength++;

			} else if (column > previousColumn) {
				// rightward / deletes
				difference= getChange(difference, previousRow, previousColumn);
				difference.fLeftLength += column - previousColumn - 1;
			} else {
				Assert.isTrue(false, "Illegal edit description"); //$NON-NLS-1$
			}

			previousColumn= column;
		}

		if (previousColumn < fLeft.getRangeCount()) {

			// trailing deletions
			difference= getChange(difference, fOptimalSplitColumn.length - 1, previousColumn);
			difference.fLeftLength += fLeft.getRangeCount() - previousColumn;
		}

		return (RangeDifference[]) fDiffs.toArray(new RangeDifference[fDiffs.size()]);
	}

	private RangeDifference getChange(RangeDifference difference, int row, int column) {
		if (difference != null)
			return difference;

		difference= new RangeDifference(RangeDifference.CHANGE, row, 0, column, 0);
		fDiffs.add(difference);
		return difference;
	}

	/* pretty printing for debug output */

	private void printRow() {
		if (fMatrix != null)
			print(fMatrix[fRow]);
		else
			print(fCurrentRow);
	}

	private static void printHeader(IRangeComparator left, IRangeComparator right) {
		System.out.println("============================="); //$NON-NLS-1$
		System.out.println("= s1: " + left.toString()); //$NON-NLS-1$
		System.out.println("= s2: " + right.toString()); //$NON-NLS-1$
		System.out.println();
	}

	private static void print(int[] row) {
		for (int i= 0; i < row.length; i++) {
			System.out.print("\t" + (row[i] == Integer.MAX_VALUE ? "-" : "" + row[i])); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		System.out.println();
	}

}
