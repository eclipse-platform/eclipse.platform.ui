/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.quickdiff.compare.equivalence;

import java.util.ConcurrentModificationException;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.source.ILineRange;

import org.eclipse.compare.rangedifferencer.IRangeComparator;


/**
 * Implements the <code>IRangeComparator</code> interface for lines in
 * a document. A <code>DocEquivalenceComparator</code> is used as the
 * input for the <code>RangeDifferencer</code> engine to perform a
 * line oriented compare on documents.
 * <p>
 * A <code>DocEquivalenceComparator</code> uses a
 * <code>DocumentEquivalenceClass</code> to compare ranges.
 * </p>
 */
public final class DocEquivalenceComparator implements IRangeComparator {

	private final DocumentEquivalenceClass fEquivalenceClass;
	private final int fLineOffset;
	private final int fLines;

	private boolean fSkip= false;

	public DocEquivalenceComparator(DocumentEquivalenceClass equivalenceClass, ILineRange range) {
		fEquivalenceClass= equivalenceClass;
		if (range == null) {
			fLineOffset= 0;
			fLines= fEquivalenceClass.getCount();
		} else {
			fLineOffset= range.getStartLine();
			fLines= range.getNumberOfLines();
			Assert.isTrue(fLineOffset >= 0);
			Assert.isTrue(fLineOffset + fLines <= fEquivalenceClass.getCount());
		}
	}

	/**
	 * Returns the number of lines in the document.
	 *
	 * @return number of lines
	 */
	public int getRangeCount() {
		return fLines;
	}

	/**
	 * Returns <code>true</code> if a line given by the first index
	 * matches a line specified by the other <code>IRangeComparator</code> and index.
	 *
	 * @param thisIndex	the number of the line within this range comparator
	 * @param other the range comparator to compare this with
	 * @param otherIndex the number of the line within the other comparator
	 * @return <code>true</code> if the lines are equal
	 */
	public boolean rangesEqual(int thisIndex, IRangeComparator other, int otherIndex) {
		if (other instanceof DocEquivalenceComparator) {
			DocEquivalenceComparator dec= (DocEquivalenceComparator) other;
			try {
				Hash ourHash= getHash(thisIndex);
				Hash otherHash= dec.getHash(otherIndex);
				return ourHash.equals(otherHash);
			} catch (ConcurrentModificationException e) {
				fSkip= true;
			} catch (IndexOutOfBoundsException e) {
				fSkip= true;
			}
		}
		return false;
	}

	Hash getHash(int index) {
		return fEquivalenceClass.getHash(fLineOffset + index);
	}

	/**
	 * Aborts the comparison if the number of tokens is too large.
	 *
	 * @param length the current edit distance
	 * @param max the maximal edit distance
	 * @param other the comparator with which to compare
	 * @return <code>true</code> to abort a token comparison
	 */
	public boolean skipRangeComparison(int length, int max, IRangeComparator other) {
		return fSkip;
	}
}

