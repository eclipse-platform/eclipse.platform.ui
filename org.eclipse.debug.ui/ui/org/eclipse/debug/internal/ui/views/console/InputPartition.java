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


import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * A partition in a console document that contains input from the keyboard.
 */
public class InputPartition extends StreamPartition {
	
	/**
	 * Once an input partition has been written to standard-in, it cannot
	 * be modified.
	 */
	private boolean fReadOnly = false;

	/**
	 * Partition type
	 */
	public static final String INPUT_PARTITION_TYPE = DebugUIPlugin.getUniqueIdentifier() + ".INPUT_PARTITION_TYPE"; //$NON-NLS-1$
	
	
	public InputPartition(String streamIdentifier, int offset, int length) {
		super(streamIdentifier, offset, length, INPUT_PARTITION_TYPE);
	}
	
	/**
	 * @see org.eclipse.debug.internal.ui.views.console.StreamPartition#createNewPartition(String, int, int)
	 */
	public StreamPartition createNewPartition(String streamIdentifier, int offset, int length) {
		return new InputPartition(streamIdentifier, offset, length);
	}	
	
	/**
	 * Sets whether this partition is read-only.
	 * 
	 * @param readOnly whether this partition is read-only
	 */
	public void setReadOnly(boolean readOnly) {
		fReadOnly = readOnly; 
	}
	
	/**
	 * Returns whether this partition is read-only.
	 * 
	 * @return whether this partition is read-only
	 */
	public boolean isReadOnly() {
		return fReadOnly;
	}
	
	/**
	 * Returns whether this partition is allowed to be combined with the
	 * given partition. Once read-only, this partition cannot be combined.
	 * 
	 * @param partition
	 * @return boolean
	 */
	public boolean canBeCombinedWith(StreamPartition partition) {
		return (!isReadOnly() && super.canBeCombinedWith(partition));
	}	
}
