package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.jface.text.TypedRegion;
import org.eclipse.swt.graphics.Color;

/**
 * A colored partition
 */
public abstract class ColorPartition extends TypedRegion {
	
	/**
	 * Partition color	 */
	private Color fColor;
	
	public ColorPartition(Color color, int offset, int length, String type) {
		super(offset, length, type);
		fColor = color;
	}
	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object partition) {
		if (super.equals(partition)) {
			fColor.equals(((ColorPartition)partition).getColor());
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() + fColor.hashCode();
	}

	/**
	 * Returns this partition's color
	 * 
	 * @return this partition's color
 	 */
	public Color getColor() {
		return fColor;
	}
	
	/**
	 * Returns whether this partition is allowed to be combined with the
	 * given partition.
	 * 	 * @param partition	 * @return boolean	 */
	public boolean canBeCombinedWith(ColorPartition partition) {
		int start = getOffset();
		int end = start + getLength();
		int otherStart = partition.getOffset();
		int otherEnd = otherStart + partition.getLength();
		boolean overlap = (otherStart >= start && otherStart <= end) || (start >= otherStart && start <= otherEnd);
		return overlap && getType().equals(partition.getType()) && getColor().equals(partition.getColor());
	}
	
	/**
	 * Returns a new partition representing this and the given parition
	 * combined.
	 * 
	 * @param partition
	 * @return partition
 	 */
	public ColorPartition combineWith(ColorPartition partition) {
		int start = getOffset();
		int end = start + getLength();
		int otherStart = partition.getOffset();
		int otherEnd = otherStart + partition.getLength();
		int theStart = Math.min(start, otherStart);
		int theEnd = Math.max(end, otherEnd);
		return createNewPartition(getColor(), theStart, theEnd - theStart);
	}
	
	/**
	 * Creates a new patition of this type with the given color, offset, 
	 * and length.
	 * 	 * @param color	 * @param offset	 * @param length	 * @return ColorPartition	 */
	public abstract ColorPartition createNewPartition(Color color, int offset, int length);
}
