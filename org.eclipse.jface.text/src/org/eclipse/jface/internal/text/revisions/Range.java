/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.revisions;

import org.eclipse.jface.text.source.ILineRange;

/**
 * A variable {@link org.eclipse.jface.text.source.ILineRange} with the following invariant:
 * <ul>
 * <li>{@link #start() start} &gt;= 0
 * <li>{@link #length() length} &gt; 0, i.e. a range cannot be empty
 * </ul>
 * <p>
 * Attempts to create or modify a <code>Range</code> such that this invariant would be violated
 * result in a {@link LineIndexOutOfBoundsException} being
 * thrown.
 * </p>
 *
 * @since 3.2
 */
public final class Range implements ILineRange, Cloneable {
	/**
	 * Creates a new range with the same start and length as the passed line range.
	 *
	 * @param range the range to copy
	 * @return a <code>Range</code> with the same start and length as <code>range</code>
	 * @throws LineIndexOutOfBoundsException if the passed {@link ILineRange} does not adhere to the
	 *         contract of {@link Range}
	 */
	public static Range copy(ILineRange range) throws LineIndexOutOfBoundsException {
		return createRelative(range.getStartLine(), range.getNumberOfLines());
	}

	/**
	 * Creates a new range equal to the passed line range.
	 *
	 * @param range the range to copy
	 * @return a <code>Range</code> equal to <code>range</code>
	 */
	public static Range copy(Range range) {
		return createRelative(range.start(), range.length());
	}

	/**
	 * Creates a new range with the given start offset and length.
	 *
	 * @param start the first line of the new range, must be &gt;= 0
	 * @param length the number of lines included in the new range, must be &gt; 0
	 * @return a <code>Range</code> with the given start and length
	 * @throws LineIndexOutOfBoundsException if the parameters violate the invariant of
	 *         {@link Range}
	 */
	public static Range createRelative(int start, int length) throws LineIndexOutOfBoundsException {
		return new Range(start, length);
	}

	/**
	 * Creates a new range with the given start and end offsets.
	 *
	 * @param start the first line of the new range, must be &gt;= 0
	 * @param end the first line not in the range any more (exclusive), must be &gt; <code>start</code>
	 * @return a <code>Range</code> with the given start and end offsets
	 * @throws LineIndexOutOfBoundsException if the parameters violate the invariant of
	 *         {@link Range}
	 */
	public static Range createAbsolute(int start, int end) {
		return new Range(start, end - start);
	}

	private int fStart;
	private int fLength;

	/*
	 * Private constructor.
	 */
	private Range(int start, int length) {
		moveTo(start);
		setLength(length);
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineRange#getStartLine()
	 */
	public int getStartLine() {
		return start();
	}

	/*
	 * @see org.eclipse.jface.text.source.ILineRange#getNumberOfLines()
	 */
	public int getNumberOfLines() {
		return length();
	}

	/**
	 * Returns the first line contained in this range. Short equivalent of {@link #getStartLine()}.
	 *
	 * @return the first line contained in this range
	 */
	public int start() {
		return fStart;
	}

	/**
	 * Returns the number of lines contained in this range. Short equivalent of {@link #getNumberOfLines()}.
	 *
	 * @return the number of lines contained in this range
	 */
	public int length() {
		return fLength;
	}

	/**
	 * Returns the first line after this range. Equivalent to {@linkplain #start() start} + {@linkplain #length() length}.
	 *
	 * @return the first line after this range
	 */
	public int end() {
		return start() + length();
	}

	/**
	 * Moves the receiver to <code>start</code>, keeping {@link #length()} constant.
	 *
	 * @param start the new start, must be &gt;= 0
	 * @throws LineIndexOutOfBoundsException if <code>start</code> &lt; 0
	 */
	public void moveTo(int start) throws LineIndexOutOfBoundsException {
		if (!(start >= 0))
			throw new LineIndexOutOfBoundsException("Cannot set a negative start: " + start); //$NON-NLS-1$
		fStart= start;
	}

	/**
	 * Moves this range such that the {@link #end()} is at <code>end</code>, keeping
	 * {@link #length()} constant.
	 *
	 * @param end the new end
	 * @throws LineIndexOutOfBoundsException if <code>end</code> &lt;= {@link #start()}
	 */
	public void moveEndTo(int end) throws LineIndexOutOfBoundsException {
		moveTo(end - length());
	}

	/**
	 * Moves the range by <code>delta</code> lines, keeping {@link #length()} constant. The
	 * resulting start line must be &gt;= 0.
	 *
	 * @param delta the number of lines to shift the range
	 * @throws LineIndexOutOfBoundsException if <code>-delta</code> &gt; {@link #start()}
	 */
	public void moveBy(int delta) throws LineIndexOutOfBoundsException {
		moveTo(start() + delta);
	}

	/**
	 * Moves the start offset to <code>start</code>, keeping {@link #end()} constant.
	 *
	 * @param start the new start, must be &gt;= 0 and &lt; {@link #end()}
	 * @throws LineIndexOutOfBoundsException if <code>start</code> &lt; 0 or &gt;= {@link #end()}
	 */
	public void setStart(int start) throws LineIndexOutOfBoundsException {
		int end= end();
		if (!(start >= 0 && start < end))
			throw new LineIndexOutOfBoundsException("Cannot set a negative start: " + start); //$NON-NLS-1$
		moveTo(start);
		setEnd(end);
	}

	/**
	 * Sets the end of this range, keeping {@link #start()} constant.
	 *
	 * @param end the new end, must be &gt; {@link #start()}
	 * @throws LineIndexOutOfBoundsException if <code>end</code> &lt;= {@link #start()}
	 */
	public void setEnd(int end) throws LineIndexOutOfBoundsException {
		setLength(end - start());
	}

	/**
	 * Sets the length of this range, keeping {@link #start()} constant.
	 *
	 * @param length the new length, must be &gt; 0
	 * @throws LineIndexOutOfBoundsException if <code>length</code> &lt;= 0
	 */
	public void setLength(int length) throws LineIndexOutOfBoundsException {
		if (!(length > 0))
			throw new LineIndexOutOfBoundsException("Cannot set length <= 0: " + length); //$NON-NLS-1$
		fLength= length;
	}

	/**
	 * Sets the length of this range, keeping {@link #end()} constant.
	 *
	 * @param length the new length, must be &gt; 0 and &lt;= {@link #end()}
	 * @throws LineIndexOutOfBoundsException if <code>length</code> &lt;= 0
	 */
	public void setLengthAndMove(int length) throws LineIndexOutOfBoundsException {
		setStart(end() - length);
	}

	/**
	 * Resizes the range by <code>delta</code> lines, keeping {@link #start()} constant.
	 *
	 * @param delta the number of lines to resize the range
	 * @throws LineIndexOutOfBoundsException if <code>-delta</code> &gt;= {@link #length()}
	 */
	public void resizeBy(int delta) throws LineIndexOutOfBoundsException {
		setLength(length() + delta);
	}

	/**
	 * Resizes the range by <code>delta</code> lines by moving the start offset, {@link #end()} remains unchanged.
	 *
	 * @param delta the number of lines to resize the range
	 * @throws LineIndexOutOfBoundsException if <code>-delta</code> &gt;= {@link #length()}
	 */
	public void resizeAndMoveBy(int delta) throws LineIndexOutOfBoundsException {
		setStart(start() + delta);
	}

	/**
	 * Splits a range off the end of the receiver. The receiver is shortened to only include
	 * <code>remaining</code> lines after the split.
	 *
	 * @param remaining the number of lines to remain in the receiver, must be in [1, {@link #length() length})
	 * @return the split off range
	 * @throws LineIndexOutOfBoundsException if <code>remaining</code>&gt;= {@link #length()} or <code>remaining</code>&ltt;= 0
	 */
	public Range split(int remaining) throws LineIndexOutOfBoundsException {
		if (!(remaining < length())) // assert before modification
			throw new LineIndexOutOfBoundsException("Remaining must be less than length: " + length()); //$NON-NLS-1$

		int splitLength= length() - remaining;
		setLength(remaining);
		return new Range(end(), splitLength);
	}

	/**
	 * Returns <code>true</code> if the passed range has the same offset and length as the receiver.
	 *
	 * @param range another line range to compare the receiver to
	 * @return <code>true</code> if <code>range</code> has the same offset and length as the receiver
	 */
	public boolean equalRange(ILineRange range) {
		if (range == this)
			return true;
		if (range == null)
			return false;
		return range.getStartLine() == start() && range.getNumberOfLines() == length();
	}

	/*
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return Range.copy(this);
	}
}