package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.swt.graphics.Color;

/**
 * A partition in a console document that represents a break after an input
 * partition. This represents a hard break that the user cannot backsapce/delete.
 */
public class BreakPartition extends ColorPartition {

	/**
	 * Partition type
	 */
	public static final String BREAK_PARTITION_TYPE = DebugUIPlugin.getUniqueIdentifier() + ".BREAK_PARTITION_TYPE";
	
	
	public BreakPartition(Color color, int offset, int length) {
		super(color, offset, length, BREAK_PARTITION_TYPE);
	}
	
	/**
	 * @see org.eclipse.debug.internal.ui.views.console.ColorPartition#createNewPartition(org.eclipse.swt.graphics.Color, int, int)
	 */
	public ColorPartition createNewPartition(Color color, int offset, int length) {
		return new BreakPartition(color, offset, length);
	}

}
