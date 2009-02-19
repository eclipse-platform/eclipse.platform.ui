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
package org.eclipse.ui.internal.texteditor.quickdiff;

import java.util.List;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.IDocument;

import org.eclipse.compare.rangedifferencer.RangeDifferencer;


/**
 * Description of a change between two or three ranges of comparable entities.
 * <p>
 * <code>RangeDifference</code> objects are the elements of a compare result returned from the
 * <code>RangeDifferencer</code> <code>find* </code> methods. Clients use these objects as they are
 * returned from the differencer. This class is not intended to be instantiated or subclassed.
 * <p>
 * Note: A range in the <code>RangeDifference</code> object is given as a start index and length in
 * terms of comparable entities. However, these entity indices and counts are not necessarily
 * character positions. For example, if an entity represents a line in a document, the start index
 * would be a line number and the count would be in lines.
 * </p>
 * 
 * @see RangeDifferencer
 * @since 3.0 (originally in org.eclipse.ui.internal.texteditor.quickdiff.compare.rangedifferencer)
 */
public class QuickDiffRangeDifference extends org.eclipse.compare.rangedifferencer.RangeDifference {

	private DiffRegion fRegion;

	/**
	 * Creates a new <code>RangeDifference</code> with the given change kind and left and right
	 * ranges.
	 * 
	 * @param kind the kind of change
	 * @param rightStart start index of entity on right side
	 * @param rightLength number of entities on right side
	 * @param leftStart start index of entity on left side
	 * @param leftLength number of entities on left side
	 */
	public QuickDiffRangeDifference(int kind, int rightStart, int rightLength, int leftStart, int leftLength) {
		super(kind, rightStart, rightLength, leftStart, leftLength);
	}

	/**
	 * Creates a new <code>RangeDifference</code> with the given change kind.
	 * 
	 * @since 3.5
	 */
	public QuickDiffRangeDifference() {
		super(NOCHANGE);
	}

	/**
	 * Shifts the offset into the left document of the receiver.
	 * 
	 * @param shift the number of elements to shift
	 */
	public void shiftLeft(int shift) {
		Assert.isTrue(shift + leftStart >= 0);
		leftStart+= shift;
	}

	/**
	 * Shifts the offset into the right document of the receiver.
	 *
	 * @param shift the number of elements to shift
	 */
	public void shiftRight(int shift) {
		Assert.isTrue(shift + rightStart >= 0);
		rightStart+= shift;
	}

	/**
	 * Resizes the receiver <code>shift</code> units, on both sides, by
	 * moving the start of the difference.
	 *
	 * @param shift the number of elements to shift
	 */
	public void extendStart(int shift) {
		Assert.isTrue(shift + rightStart >= 0);
		Assert.isTrue(shift + leftStart >= 0);
		rightStart+= shift;
		rightLength-= shift;
		leftStart+= shift;
		leftLength-= shift;
	}

	/**
	 * Resizes the receiver <code>shift</code> units, on both sides, by
	 * moving the end of the difference.
	 *
	 * @param shift the number of elements to shift
	 */
	public void extendEnd(int shift) {
		Assert.isTrue(shift + rightLength >= 0);
		Assert.isTrue(shift + leftLength >= 0);
		rightLength+= shift;
		leftLength+= shift;
	}

	/**
	 * Returns the diff region corresponding to this range difference.
	 *
	 * @param differences the list of differences around this one difference
	 * @param source the original document (left document) that this difference refers to
	 * @return a <code>DiffRegion</code> corresponding to this difference
	 */
	public DiffRegion getDiffRegion(List differences, IDocument source) {
		if (fRegion == null)
			fRegion= new DiffRegion(this, 0, differences, source);
		return fRegion;
	}
}

