/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import java.math.BigInteger;

import org.eclipse.debug.core.model.MemoryByte;

/**
 * When a MemoryViewTab is created without a renderer defined, use
 * this empty renderer to avoid exceptions in the code.
 * 
 * @since 3.0
 */
public class EmptyRenderer extends AbstractMemoryRenderer {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.AbstractMemoryRenderer#getString(java.lang.String, java.math.BigInteger, org.eclipse.debug.internal.core.memory.MemoryByte[], java.lang.String)
	 */
	public String getString(
		String dataType,
		BigInteger address,
		MemoryByte[] data, String paddedStr) {
		
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.AbstractMemoryRenderer#getBytes(java.lang.String, java.math.BigInteger, org.eclipse.debug.internal.core.memory.MemoryByte[], java.lang.String)
	 */
	public byte[] getBytes(
		String dataType,
		BigInteger address,
		MemoryByte[] currentValues,
		String data) {
		
		return new byte[0];
	}
}
