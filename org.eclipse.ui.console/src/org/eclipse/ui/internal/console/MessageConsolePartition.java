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
package org.eclipse.ui.internal.console;


import org.eclipse.jface.text.TypedRegion;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * A partition from a message stream connected to a message console. 
 */
public class MessageConsolePartition extends TypedRegion {
	
	/**
	 * Associated stream
	 */
	private MessageConsoleStream fStream;
	
	/**
	 * Partition type
	 */
	public static final String MESSAGE_PARTITION_TYPE = ConsolePlugin.getUniqueIdentifier() + ".MESSAGE_PARTITION_TYPE"; //$NON-NLS-1$	
	
	public MessageConsolePartition(MessageConsoleStream stream, int offset, int length) {
		super(offset, length, MESSAGE_PARTITION_TYPE);
		fStream = stream;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object partition) {
		return super.equals(partition) && fStream.equals(((MessageConsolePartition)partition).getStream());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() + fStream.hashCode();
	}

	/**
	 * Returns this partition's stream
	 * 
	 * @return this partition's stream
 	 */
	public MessageConsoleStream getStream() {
		return fStream;
	}
	
	/**
	 * Returns whether this partition is allowed to be combined with the
	 * given partition.
	 * 
	 * @param partition
	 * @return boolean
	 */
	public boolean canBeCombinedWith(MessageConsolePartition partition) {
		int start = getOffset();
		int end = start + getLength();
		int otherStart = partition.getOffset();
		int otherEnd = otherStart + partition.getLength();
		boolean overlap = (otherStart >= start && otherStart <= end) || (start >= otherStart && start <= otherEnd);
		return overlap && getType().equals(partition.getType()) && getStream().equals(partition.getStream());
	}
	
	/**
	 * Returns a new partition representing this and the given parition
	 * combined.
	 * 
	 * @param partition
	 * @return partition
 	 */
	public MessageConsolePartition combineWith(MessageConsolePartition partition) {
		int start = getOffset();
		int end = start + getLength();
		int otherStart = partition.getOffset();
		int otherEnd = otherStart + partition.getLength();
		int theStart = Math.min(start, otherStart);
		int theEnd = Math.max(end, otherEnd);
		return createNewPartition(theStart, theEnd - theStart);
	}
	
	/**
	 * Creates a new patition of this type with the given color, offset, 
	 * and length.
	 * 
	 * @param offset
	 * @param length
	 * @return a new partition with the given range
	 */
	public MessageConsolePartition createNewPartition(int offset, int length) {
		return new MessageConsolePartition(getStream(), offset, length);
	}
}