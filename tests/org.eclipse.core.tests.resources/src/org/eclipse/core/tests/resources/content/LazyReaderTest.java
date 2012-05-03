/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources.content;

import java.io.*;
import junit.framework.*;
import org.eclipse.core.internal.content.LazyReader;

/**
 * Tests for {@link LazyReader}.
 */
public class LazyReaderTest extends TestCase {

	/**
	 * Opens up protected methods from LazyInputStream.
	 */
	private static class OpenLazyReader extends LazyReader {

		public OpenLazyReader(Reader in, int blockCapacity) {
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

	public LazyReaderTest(String name) {
		super(name);
	}

	public void testReadSingleChar() throws UnsupportedEncodingException, IOException {
		CharArrayReader underlying = new CharArrayReader(DATA.toCharArray());
		OpenLazyReader stream = new OpenLazyReader(underlying, 7);
		assertEquals("1.0", '0', stream.read());
		assertEquals("1.1", '1', stream.read());
		stream.skip(10);
		assertEquals("1.2", '2', stream.read());
		assertEquals("1.3", 13, stream.getOffset());
		stream.close();
	}

	public void testReadBlock() throws UnsupportedEncodingException, IOException {
		CharArrayReader underlying = new CharArrayReader(DATA.toCharArray());
		OpenLazyReader stream = new OpenLazyReader(underlying, 7);
		stream.skip(4);
		char[] buffer = new char[7];
		int read = stream.read(buffer);
		assertEquals("1.0", buffer.length, read);
		assertEquals("1.1", DATA.substring(4, 4 + buffer.length), new String(buffer));
		assertEquals("1.2", 11, stream.getOffset());
		read = stream.read(buffer, 3, 4);
		assertEquals("2.0", 4, read);
		assertEquals("2.1", DATA.substring(11, 11 + read), new String(buffer, 3, read));
		assertEquals("2.2", 15, stream.getOffset());
		stream.mark(0);
		buffer = new char[100];
		read = stream.read(buffer);
		assertEquals("3.0", DATA.length() - 15, read);
		assertEquals("3.1", DATA.substring(15, 15 + read), new String(buffer, 0, read));
		assertFalse("3.2", stream.ready());
		stream.reset();
		assertEquals("4.0", 15, stream.getOffset());
		read = stream.read(buffer, 10, 14);
		assertEquals("4.1", 29, stream.getOffset());
		assertTrue("4.2", stream.ready());
		assertEquals("4.3", 14, read);
		assertEquals("4.4", DATA.substring(15, 15 + read), new String(buffer, 10, read));
		read = stream.read(buffer);
		assertEquals("5.0", 30, stream.getOffset());
		assertFalse("5.1", stream.ready());
		assertEquals("5.2", 1, read);
		assertEquals("5.3", (byte) DATA.charAt(29), buffer[0]);
		read = stream.read(buffer);
		assertEquals("6.0", 30, stream.getOffset());
		assertFalse("6.1", stream.ready());
		assertEquals("6.2", -1, read);
		stream.close();
	}

	public void testMarkAndReset() throws UnsupportedEncodingException, IOException {
		CharArrayReader underlying = new CharArrayReader(DATA.toCharArray());
		OpenLazyReader stream = new OpenLazyReader(underlying, 7);
		assertTrue("0.1", stream.ready());
		stream.skip(13);
		assertTrue("0.2", stream.ready());
		stream.mark(0);
		assertEquals("2.0", 13, stream.getMark());
		assertEquals("2.1", '3', stream.read());
		assertEquals("2.2", '4', stream.read());
		assertEquals("2.3", 15, stream.getOffset());
		assertTrue("2.4", stream.ready());
		stream.reset();
		assertTrue("2.5", stream.ready());
		assertEquals("2.6", 13, stream.getOffset());
		assertEquals("2.7", 17, stream.skip(1000));
		assertFalse("2.8", stream.ready());
		stream.reset();
		assertTrue("2.9", stream.ready());
		assertEquals("2.10", 13, stream.getOffset());
		stream.reset();
		assertTrue("2.11", stream.ready());
		assertEquals("2.12", 13, stream.getOffset());
		stream.rewind();
		assertEquals("3.0", 0, stream.getOffset());
		stream.close();
	}

	public static Test suite() {
		return new TestSuite(LazyReaderTest.class);
	}
}
