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
package org.eclipse.core.tests.runtime.content;

import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import junit.framework.*;
import junit.framework.Test;
import junit.framework.TestCase;
import org.eclipse.core.internal.content.LazyInputStream;

public class LazyInputStreamTest extends TestCase {

	/**
	 * Opens up protected methods from LazyInputStream.
	 */
	private static class OpenLazyInputStream extends LazyInputStream {

		public OpenLazyInputStream(InputStream in, int blockCapacity) {
			super(in, blockCapacity);
		}

		public int getBlockCount() {
			return super.getBlockCount();
		}

		public int getBufferSize() {
			return super.getBufferSize();
		}

		public int getMark() {
			return super.getMark();
		}

		public int getOffset() {
			return super.getOffset();
		}
	}

	private final static String DATA = "012345678901234567890123456789";

	private final static int[] VARIOUS_INTS = {0xFF, 0xFE, 0xA0, 0x7F, 0x70, 0x10, 0x00};

	public LazyInputStreamTest(String name) {
		super(name);
	}

	public void testReadSingleByte() throws UnsupportedEncodingException, IOException {
		ByteArrayInputStream underlying = new ByteArrayInputStream(DATA.getBytes());
		OpenLazyInputStream stream = new OpenLazyInputStream(underlying, 7);
		assertEquals("1.0", '0', stream.read());
		assertEquals("1.1", '1', stream.read());
		stream.skip(10);
		assertEquals("1.2", '2', stream.read());
		assertEquals("1.3", 13, stream.getOffset());
	}

	public void testReadBlock() throws UnsupportedEncodingException, IOException {
		ByteArrayInputStream underlying = new ByteArrayInputStream(DATA.getBytes());
		OpenLazyInputStream stream = new OpenLazyInputStream(underlying, 7);
		stream.skip(4);
		byte[] buffer = new byte[7];
		int read = stream.read(buffer);
		assertEquals("1.0", buffer.length, read);
		assertEquals("1.1", DATA.substring(4, 4 + buffer.length), new String(buffer));
		assertEquals("1.2", 11, stream.getOffset());
		read = stream.read(buffer, 3, 4);
		assertEquals("2.0", 4, read);
		assertEquals("2.1", DATA.substring(11, 11 + read), new String(buffer, 3, read));
		assertEquals("2.2", 15, stream.getOffset());
		stream.mark(0);
		buffer = new byte[100];
		read = stream.read(buffer);
		assertEquals("3.0", DATA.length() - 15, read);
		assertEquals("3.1", DATA.substring(15, 15 + read), new String(buffer, 0, read));
		assertEquals("3.2", 0, stream.available());
		stream.reset();
		assertEquals("4.0", 15, stream.getOffset());
		read = stream.read(buffer, 10, 14);
		assertEquals("4.1", 29, stream.getOffset());
		assertEquals("4.2", 1, stream.available());
		assertEquals("4.3", 14, read);
		assertEquals("4.4", DATA.substring(15, 15 + read), new String(buffer, 10, read));
		read = stream.read(buffer);
		assertEquals("5.0", 30, stream.getOffset());
		assertEquals("5.1", 0, stream.available());
		assertEquals("5.2", 1, read);
		assertEquals("5.3", (byte) DATA.charAt(29), buffer[0]);
		read = stream.read(buffer);
		assertEquals("6.0", 30, stream.getOffset());
		assertEquals("6.1", 0, stream.available());
		assertEquals("6.2", -1, read);
	}

	public void testMarkAndReset() throws UnsupportedEncodingException, IOException {
		ByteArrayInputStream underlying = new ByteArrayInputStream(DATA.getBytes());
		OpenLazyInputStream stream = new OpenLazyInputStream(underlying, 7);
		assertEquals("0.1", 30, stream.available());
		stream.skip(13);
		assertEquals("0.2", 17, stream.available());
		stream.mark(0);
		assertEquals("2.0", 13, stream.getMark());
		assertEquals("2.1", '3', stream.read());
		assertEquals("2.2", '4', stream.read());
		assertEquals("2.3", 15, stream.getOffset());
		assertEquals("2.4", 15, stream.available());
		stream.reset();
		assertEquals("2.5", 17, stream.available());
		assertEquals("2.6", 13, stream.getOffset());
		stream.reset();
		assertEquals("2.7", 17, stream.available());
		assertEquals("2.8", 13, stream.getOffset());
	}

	public void testContentHasEOF() throws IOException {
		byte[] changedData = DATA.getBytes();
		changedData[0] = (byte) 0xFF;
		ByteArrayInputStream underlying = new ByteArrayInputStream(changedData);
		OpenLazyInputStream stream = new OpenLazyInputStream(underlying, 7);
		int c = stream.read();
		assertTrue("1.0", -1 != c);
		assertEquals("2.0", 0xFF, c);
	}

	public void testVariedContent() throws IOException {
		byte[] contents = new byte[VARIOUS_INTS.length];
		for (int i = 0; i < contents.length; i++)
			contents[i] = (byte) VARIOUS_INTS[i];
		ByteArrayInputStream underlying = new ByteArrayInputStream(contents);
		OpenLazyInputStream stream = new OpenLazyInputStream(underlying, 7);
		for (int i = 0; i < VARIOUS_INTS.length; i++)
			assertEquals("1.0." + i, VARIOUS_INTS[i], stream.read());
	}

	public static Test suite() {
		return new TestSuite(LazyInputStreamTest.class);
	}
}