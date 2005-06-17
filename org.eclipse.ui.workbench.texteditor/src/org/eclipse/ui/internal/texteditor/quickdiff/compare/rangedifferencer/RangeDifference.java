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

import java.util.List;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;

import org.eclipse.ui.internal.texteditor.quickdiff.DiffRegion;

/**
 * Description of a change between two or three ranges of comparable entities.
 * <p>
 * <code>RangeDifference</code> objects are the elements of a compare result returned from
 * the <code>RangeDifferencer</code> <code>find* </code> methods.
 * Clients use these objects as they are returned from the differencer.
 * This class is not intended to be instantiated or subclassed.
 * <p>
 * Note: A range in the <code>RangeDifference</code> object is given as a start index
 * and length in terms of comparable entities. However, these entity indices and counts
 * are not necessarily character positions. For example, if an entity represents a line
 * in a document, the start index would be a line number and the count would be in lines.
 * </p>
 *
 * @see RangeDifferencer
 * @since 3.0
 */
public class RangeDifference {

	/** Two-way change constant indicating no change. */
	public final static int NOCHANGE= 0;
	/** Two-way change constant indicating two-way change (same as <code>RIGHT</code>) */
	public final static int CHANGE= 2;

	/** Three-way change constant indicating a change in both right and left. */
	public final static int CONFLICT= 1;
	/** Three-way change constant indicating a change in right. */
	public final static int RIGHT= 2;
	/** Three-way change constant indicating a change in left. */
	public final static int LEFT= 3;
	/**
	 * Three-way change constant indicating the same change in both right and left,
	 * that is only the ancestor is different.
	 */
	public final static int ANCESTOR= 4;

	/** Constant indicating an unknown change kind. */
	public final static int ERROR= 5;

	/** the kind of change: NOCHANGE, CHANGE, LEFT, RIGHT, ANCESTOR, CONFLICT, ERROR */
	final int fKind;

	int fLeftStart;
	int fLeftLength;
	int fRightStart;
	int fRightLength;
	int lAncestorStart;
	int lAncestorLength;
	private DiffRegion fRegion;

	/**
	 * Creates a new range difference with the given change kind.
	 *
	 * @param changeKind the kind of change
	 */
	public RangeDifference(int changeKind) {
		fKind= changeKind;
	}

	/**
	 * Creates a new <code>RangeDifference</code> with the given change kind
	 * and left and right ranges.
	 *
	 * @param kind the kind of change
	 * @param rightStart start index of entity on right side
	 * @param rightLength number of entities on right side
	 * @param leftStart start index of entity on left side
	 * @param leftLength number of entities on left side
	 */
	public RangeDifference(int kind, int rightStart, int rightLength, int leftStart, int leftLength) {
		fKind= kind;
		fRightStart= rightStart;
		fRightLength= rightLength;
		fLeftStart= leftStart;
		fLeftLength= leftLength;
	}

	/**
	 * Creates a new <code>RangeDifference</code> with the given change kind
	 * and left, right, and ancestor ranges.
	 *
	 * @param kind the kind of change
	 * @param rightStart start index of entity on right side
	 * @param rightLength number of entities on right side
	 * @param leftStart start index of entity on left side
	 * @param leftLength number of entities on left side
	 * @param ancestorStart start index of entity on ancestor side
	 * @param ancestorLength number of entities on ancestor side
	 */
	public RangeDifference(int kind, int rightStart, int rightLength, int leftStart, int leftLength,
									int ancestorStart, int ancestorLength) {
		this(kind, rightStart, rightLength, leftStart, leftLength);
		lAncestorStart= ancestorStart;
		lAncestorLength= ancestorLength;
	}

	/**
	 * Returns the kind of difference.
	 *
	 * @return the kind of difference, one of
	 * <code>NOCHANGE</code>, <code>CHANGE</code>, <code>LEFT</code>, <code>RIGHT</code>,
	 * <code>ANCESTOR</code>, <code>CONFLICT</code>, <code>ERROR</code>
	 */
	public int kind() {
		return fKind;
	}

	/**
	 * Returns the start index of the entity range on the ancestor side.
	 *
	 * @return the start index of the entity range on the ancestor side
	 */
	public int ancestorStart() {
		return lAncestorStart;
	}

	/**
	 * Returns the number of entities on the ancestor side.
	 *
	 * @return the number of entities on the ancestor side
	 */
	public int ancestorLength() {
		return lAncestorLength;
	}

	/**
	 * Returns the end index of the entity range on the ancestor side.
	 *
	 * @return the end index of the entity range on the ancestor side
	 */
	public int ancestorEnd() {
		return lAncestorStart + lAncestorLength;
	}

	/**
	 * Returns the start index of the entity range on the right side.
	 *
	 * @return the start index of the entity range on the right side
	 */
	public int rightStart() {
		return fRightStart;
	}

	/**
	 * Returns the number of entities on the right side.
	 *
	 * @return the number of entities on the right side
	 */
	public int rightLength() {
		return fRightLength;
	}

	/**
	 * Returns the end index of the entity range on the right side.
	 *
	 * @return the end index of the entity range on the right side
	 */
	public int rightEnd() {
		return fRightStart + fRightLength;
	}

	/**
	 * Returns the start index of the entity range on the left side.
	 *
	 * @return the start index of the entity range on the left side
	 */
	public int leftStart() {
		return fLeftStart;
	}

	/**
	 * Returns the number of entities on the left side.
	 *
	 * @return the number of entities on the left side
	 */
	public int leftLength() {
		return fLeftLength;
	}

	/**
	 * Returns the end index of the entity range on the left side.
	 *
	 * @return the end index of the entity range on the left side
	 */
	public int leftEnd() {
		return fLeftStart + fLeftLength;
	}

	/**
	 * Returns the maximum number of entities in the left, right, and ancestor sides of this range.
	 *
	 * @return the maximum number of entities in the left, right, and ancestor sides of this range
	 */
	public int maxLength() {
		return Math.max(fRightLength, Math.max(fLeftLength, lAncestorLength));
	}

	/**
	 * Shifts the offset into the left document of the receiver.
	 *
	 * @param shift the number of elements to shift
	 */
	public void shiftLeft(int shift) {
		Assert.isTrue(shift + fLeftStart >= 0);
		fLeftStart += shift;
	}

	/**
	 * Shifts the offset into the right document of the receiver.
	 *
	 * @param shift the number of elements to shift
	 */
	public void shiftRight(int shift) {
		Assert.isTrue(shift + fRightStart >= 0);
		fRightStart += shift;
	}

	/**
	 * Resizes the receiver <code>shift</code> units, on both sides, by
	 * moving the start of the difference.
	 *
	 * @param shift the number of elements to shift
	 */
	public void extendStart(int shift) {
		Assert.isTrue(shift + fRightStart >= 0);
		Assert.isTrue(shift + fLeftStart >= 0);
		fRightStart += shift;
		fRightLength -= shift;
		fLeftStart += shift;
		fLeftLength -= shift;
	}

	/**
	 * Resizes the receiver <code>shift</code> units, on both sides, by
	 * moving the end of the difference.
	 *
	 * @param shift the number of elements to shift
	 */
	public void extendEnd(int shift) {
		Assert.isTrue(shift + fRightLength >= 0);
		Assert.isTrue(shift + fLeftLength >= 0);
		fRightLength += shift;
		fLeftLength += shift;
	}

	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof RangeDifference) {
			RangeDifference d= (RangeDifference) obj;
			return fKind == d.fKind && fRightStart == d.fRightStart && fRightLength == d.fRightLength && fLeftStart == d.fLeftStart && fLeftLength == d.fLeftLength;
		}
		return false;
	}

	/**
	 * Returns {@link Object#hashCode()}.
	 *
	 * @return the hash code which is {@link Object#hashCode()}
	 */
	public final int hashCode() {
		return super.hashCode();
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

