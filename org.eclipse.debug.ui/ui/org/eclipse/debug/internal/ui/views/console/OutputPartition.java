package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.DebugUIPlugin;

/**
 * A partition in a console document that contains output from a process.
 */
public class OutputPartition extends StreamPartition {

	/**
	 * Partition type
	 */
	public static final String OUTPUT_PARTITION_TYPE = DebugUIPlugin.getUniqueIdentifier() + ".OUTPUT_PARTITION_TYPE"; //$NON-NLS-1$
	
	
	public OutputPartition(String streamIdentifier, int offset, int length) {
		super(streamIdentifier, offset, length, OUTPUT_PARTITION_TYPE);
	}
	
	/**
	 * @see org.eclipse.debug.internal.ui.views.console.StreamPartition#createNewPartition(String, int, int)
	 */
	public StreamPartition createNewPartition(String streamIdetifier, int offset, int length) {
		return new OutputPartition(streamIdetifier, offset, length);
	}	
}
