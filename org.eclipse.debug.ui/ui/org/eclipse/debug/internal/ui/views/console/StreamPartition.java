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
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.jface.text.TypedRegion;

/**
 * A partition from an input/output stream connected to the console. 
 */
public abstract class StreamPartition extends TypedRegion {
	
	/**
	 * Stream identifier
	 */
	private String fStreamIdentifier;
	
	public StreamPartition(String streamIdentifier, int offset, int length, String type) {
		super(offset, length, type);
		fStreamIdentifier = streamIdentifier;
	}
	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object partition) {
		if (super.equals(partition)) {
			fStreamIdentifier.equals(((StreamPartition)partition).getStreamIdentifier());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() + fStreamIdentifier.hashCode();
	}

	/**
	 * Returns this partition's stream identifier
	 * 
	 * @return this partition's stream identifier
 	 */
	public String getStreamIdentifier() {
		return fStreamIdentifier;
	}
	
	/**
	 * Returns whether this partition is allowed to be combined with the
	 * given partition.
	 * 
	 * @param partition
	 * @return boolean
	 */
	public boolean canBeCombinedWith(StreamPartition partition) {
		int start = getOffset();
		int end = start + getLength();
		int otherStart = partition.getOffset();
		int otherEnd = otherStart + partition.getLength();
		boolean overlap = (otherStart >= start && otherStart <= end) || (start >= otherStart && start <= otherEnd);
		return overlap && getType().equals(partition.getType()) && getStreamIdentifier().equals(partition.getStreamIdentifier());
	}
	
	/**
	 * Returns a new partition representing this and the given parition
	 * combined.
	 * 
	 * @param partition
	 * @return partition
 	 */
	public StreamPartition combineWith(StreamPartition partition) {
		int start = getOffset();
		int end = start + getLength();
		int otherStart = partition.getOffset();
		int otherEnd = otherStart + partition.getLength();
		int theStart = Math.min(start, otherStart);
		int theEnd = Math.max(end, otherEnd);
		return createNewPartition(getStreamIdentifier(), theStart, theEnd - theStart);
	}
	
	/**
	 * Creates a new patition of this type with the given color, offset, 
	 * and length.
	 * 
	 * @param streamIdentifer
	 * @param offset
	 * @param length
	 * @return ColorPartition
	 */
	public abstract StreamPartition createNewPartition(String streamIdentifier, int offset, int length);
}
