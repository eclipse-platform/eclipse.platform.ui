/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

 
/**
 * Positions describe text ranges of a document. Positions are adapted to
 * changes applied to that document. The text range is specified by an offset
 * and a length. Positions can be marked as deleted. Deleted positions are
 * considered to no longer represent a valid text range in the managing
 * document.
 * <p>
 * Positions attached to documents are usually updated by position updaters.
 * Because position updaters are freely definable and because of the frequency
 * in which they are used, the fields of a position are made publicly
 * accessible. Clients other than position updaters are not allowed to access
 * these public fields.
 * <p>
 * Position can not be used as keys in hash tables as they override
 * <code>equals</code> and <code>hashCode</code> as they would be value
 * objects.
 * 
 * @see org.eclipse.jface.text.IDocument
 */
public class Position {
	
	/** The offset of the position */
	public int offset;
	/** The length of the position */
	public int length;
	/** Indicates whether the position has been deleted */
	public boolean isDeleted;
	
	/**
	 * Creates a new position with the given offset and length 0.
	 *
	 * @param offset the position offset, must be >= 0
	 */
	public Position(int offset) {
		this(offset, 0);
	}
	
	/**
	 * Creates a new position with the given offset and length.
	 *
	 * @param offset the position offset, must be >= 0
	 * @param length the position length, must be >= 0
	 */
	public Position(int offset, int length) {
		Assert.isTrue(offset >= 0);
		Assert.isTrue(length >= 0);
		this.offset= offset;
		this.length= length;
	}
	
	/**
	 * Creates a new, not initialized position.
	 */
	protected Position() {
	}
	
	 /*
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
	 	int deleted= isDeleted ? 0 : 1;
	 	return (offset << 24) | (length << 16) | deleted;
	 }
	
	/**
	 * Marks this position as deleted.
	 */
	public void delete() {
		isDeleted= true;
	}
	
	/**
	 * Marks this position as not deleted.
	 * 
	 * @since 2.0
	 */
	public void undelete() {
		isDeleted= false;
	}
	
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if (other instanceof Position) {
			Position rp= (Position) other;
			return (rp.offset == offset) && (rp.length == length);
		}
		return super.equals(other);
	}
		
	/**
	 * Returns the length of this position.
	 *
	 * @return the length of this position
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * Returns the offset of this position.
	 *
	 * @return the length of this position
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * Checks whether the given offset is inside
	 * of this position's text range.
	 *
	 * @param offset the offset to check
	 * @return <code>true</code> if offset is inside of this position 
	 */
	public boolean includes(int offset) {
		
		if (isDeleted)
			return false;
		
		return (this.offset <= offset) && (offset < this.offset + length);
	}
	
	/**
	 * Checks whether the intersection of the given text range
	 * and the text range represented by this position is empty
	 * or not.
	 *
	 * @param offset the offset of the range to check
	 * @param length the length of the range to check
	 * @return <code>true</code> if intersection is not empty
	 */
	public boolean overlapsWith(int offset, int length) {
		
		if (isDeleted)
			return false;
			
		int end= offset + length;
		int thisEnd= this.offset + this.length;
		
		if (length > 0) {
			if (this.length > 0)
				return this.offset < end && offset < thisEnd;
			return  offset <= this.offset && this.offset < end;
		}
		
		if (this.length > 0)
			return this.offset <= offset && offset < thisEnd;
		return this.offset == offset;
	}
	
	/**
	 * Returns whether this position has been deleted or not.
	 *
	 * @return <code>true</code> if position has been deleted
	 */
	public boolean isDeleted() {
		return isDeleted;
	}
	
	/**
	 * Changes the length of this position to the given length.
	 *
	 * @param length the new length of this position
	 */
	public void setLength(int length) {
		Assert.isTrue(length >= 0);
		this.length= length;
	}
	
	/**
	 * Changes the offset of this position to the given offset.
	 *
	 * @param offset the new offset of this position
	 */
	public void setOffset(int offset) {
		Assert.isTrue(offset >= 0);
		this.offset= offset;
	}
}
