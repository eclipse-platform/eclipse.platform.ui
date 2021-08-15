/*******************************************************************************
 *  Copyright (c) 2021 John Dallaway and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     John Dallaway - initial implementation
 *******************************************************************************/
package org.eclipse.debug.tests.view.memory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.views.memory.renderings.AbstractIntegerRendering;
import org.eclipse.debug.internal.ui.views.memory.renderings.HexIntegerRendering;
import org.eclipse.debug.internal.ui.views.memory.renderings.RenderingsUtil;
import org.junit.Test;

/**
 * Tests for translation of memory bytes between in-memory representation and UI
 * presentation
 */
@SuppressWarnings("restriction")
public class TableRenderingTests {

	private static final byte[] TWO_BYTES = new byte[] {
			(byte) 0x67, (byte) 0x89 };
	private static final byte[] FOUR_BYTES = new byte[] {
			(byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0xab };
	private static final byte[] EIGHT_BYTES = new byte[] {
			(byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89,
			(byte) 0xab, (byte) 0xcd, (byte) 0xef };

	@Test
	public void testHexIntegerRendering() throws DebugException {
		testIntegerRendering(createHexIntegerRendering(1), TWO_BYTES, "6789", "8967"); //$NON-NLS-1$ //$NON-NLS-2$
		testIntegerRendering(createHexIntegerRendering(2), TWO_BYTES, "6789", "6789"); //$NON-NLS-1$ //$NON-NLS-2$
		testIntegerRendering(createHexIntegerRendering(1), FOUR_BYTES, "456789AB", "AB896745"); //$NON-NLS-1$ //$NON-NLS-2$
		testIntegerRendering(createHexIntegerRendering(2), FOUR_BYTES, "456789AB", "89AB4567"); //$NON-NLS-1$ //$NON-NLS-2$
		testIntegerRendering(createHexIntegerRendering(4), FOUR_BYTES, "456789AB", "456789AB"); //$NON-NLS-1$ //$NON-NLS-2$
		testIntegerRendering(createHexIntegerRendering(1), EIGHT_BYTES, "0123456789ABCDEF", "EFCDAB8967452301"); //$NON-NLS-1$ //$NON-NLS-2$
		testIntegerRendering(createHexIntegerRendering(2), EIGHT_BYTES, "0123456789ABCDEF", "CDEF89AB45670123"); //$NON-NLS-1$ //$NON-NLS-2$
		testIntegerRendering(createHexIntegerRendering(4), EIGHT_BYTES, "0123456789ABCDEF", "89ABCDEF01234567"); //$NON-NLS-1$ //$NON-NLS-2$
		testIntegerRendering(createHexIntegerRendering(8), EIGHT_BYTES, "0123456789ABCDEF", "0123456789ABCDEF"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void testIntegerRendering(AbstractIntegerRendering rendering, byte[] bytes, String bigEndianString, String littleEndianString) throws DebugException {
		final IMemoryBlockExtension block = new TableRenderingTestsMemoryBlock(bytes, rendering.getAddressableSize());
		rendering.init(null, block);
		final MemoryByte[] memoryBytes = block.getBytesFromOffset(BigInteger.ZERO, bytes.length / rendering.getAddressableSize());

		rendering.setDisplayEndianess(RenderingsUtil.BIG_ENDIAN);
		assertEquals(bigEndianString, rendering.getString(null, null, memoryBytes));
		assertArrayEquals(bytes, rendering.getBytes(null, null, memoryBytes, bigEndianString));

		rendering.setDisplayEndianess(RenderingsUtil.LITTLE_ENDIAN);
		assertEquals(littleEndianString, rendering.getString(null, null, memoryBytes));
		assertArrayEquals(bytes, rendering.getBytes(null, null, memoryBytes, littleEndianString));
	}

	private HexIntegerRendering createHexIntegerRendering(int addressableSize) {
		return new HexIntegerRendering(null) {
			@Override
			public int getAddressableSize() {
				return addressableSize;
			}
		};
	}

}
