/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;

/**
 * Example memory block
 */
public class PDAMemoryBlock extends PDADebugElement implements IMemoryBlock {
	
	/**
	 * The bytes
	 */
	private byte[] fBytes = null;
	private long fStart, fLength;
	
	/**
	 * Constructs a new memory block
	 */
	public PDAMemoryBlock(PDADebugTarget target, long start, long length) {
		super(target);
		fBytes = new byte[(int)length];
		fStart = start;
		fLength = length;
		byte b = 0;
		for (int i = 0; i < fBytes.length; i++) {
			fBytes[i] = b++;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getStartAddress()
	 */
	@Override
	public long getStartAddress() {
		return fStart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getLength()
	 */
	@Override
	public long getLength() {
		return fLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#getBytes()
	 */
	@Override
	public byte[] getBytes() throws DebugException {
		return fBytes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#supportsValueModification()
	 */
	@Override
	public boolean supportsValueModification() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IMemoryBlock#setValue(long, byte[])
	 */
	@Override
	public void setValue(long offset, byte[] bytes) throws DebugException {
		int i = 0;
		long off = offset;
		while (off < fBytes.length && i < bytes.length) {
			fBytes[(int)off++] = bytes[i++];
		}
		fireChangeEvent(DebugEvent.CONTENT);
	}

}
