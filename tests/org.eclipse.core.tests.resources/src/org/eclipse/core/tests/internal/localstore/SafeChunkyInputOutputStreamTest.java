/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.localstore;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.runtime.IPath;

public class SafeChunkyInputOutputStreamTest extends LocalStoreTest {
	protected File temp;

	public SafeChunkyInputOutputStreamTest() {
		super();
	}

	public SafeChunkyInputOutputStreamTest(String name) {
		super(name);
	}

	protected boolean compare(byte[] source, byte[] target) {
		if (source.length != target.length)
			return false;
		for (int i = 0; i < target.length; i++)
			if (source[i] != target[i])
				return false;
		return true;
	}

	protected byte[] merge(byte[] b1, byte[] b2) {
		byte[] result = new byte[b1.length + b2.length];
		for (int i = 0; i < b1.length; i++)
			result[i] = b1[i];
		for (int i = 0; i < b2.length; i++)
			result[b1.length + i] = b2[i];
		return result;
	}

	protected void setUp() throws Exception {
		super.setUp();
		IPath location = getRandomLocation();
		temp = location.append("temp").toFile();
		temp.mkdirs();
		assertTrue("could not create temp directory", temp.isDirectory());
	}

	public static Test suite() {
		return new TestSuite(SafeChunkyInputOutputStreamTest.class);
	}

	protected void tearDown() throws Exception {
		ensureDoesNotExistInFileSystem(temp.getParentFile());
		super.tearDown();
	}

	public void testBufferLimit() {
		File target = new File(temp, "target");
		Workspace.clear(target); // make sure there was nothing here before
		assertTrue("1.0", !target.exists());

		// use only one chunk but bigger than the buffer
		int bufferSize = 10024;
		byte[] chunk = getBigContents(bufferSize);
		SafeChunkyOutputStream output = null;
		try {
			output = new SafeChunkyOutputStream(target);
			try {
				output.write(chunk);
				output.succeed();
			} finally {
				output.close();
			}
		} catch (IOException e) {
			fail("2.0", e);
		}

		// read chunks
		SafeChunkyInputStream input = null;
		try {
			input = new SafeChunkyInputStream(target);
			try {
				byte[] read = new byte[chunk.length];
				assertTrue("3.0", input.read(read) == chunk.length);
				assertTrue("3.6", compare(chunk, read));
			} finally {
				input.close();
			}
		} catch (IOException e) {
			fail("3.20", e);
		}
		Workspace.clear(target); // make sure there was nothing here before
	}

	public void testFailure() {
		File target = new File(temp, "target");
		Workspace.clear(target); // make sure there was nothing here before
		assertTrue("1.0", !target.exists());

		// misc
		byte[] fakeEnd = new byte[ILocalStoreConstants.END_CHUNK.length];
		System.arraycopy(ILocalStoreConstants.END_CHUNK, 0, fakeEnd, 0, ILocalStoreConstants.END_CHUNK.length);
		fakeEnd[fakeEnd.length - 1] = 86;

		// write chunks
		byte[] chunk1 = getRandomString().getBytes();
		byte[] chunk2 = getRandomString().getBytes();
		byte[] chunk3 = getRandomString().getBytes();
		byte[] chunk4 = getRandomString().getBytes();
		byte[] chunk5 = getRandomString().getBytes();
		byte[] chunk6 = getRandomString().getBytes();
		SafeChunkyOutputStream output = null;
		try {
			output = new SafeChunkyOutputStream(target);
			try {
				output.write(chunk1);
				output.succeed();
				output = new SafeChunkyOutputStream(target);
				// fake failure
				output.write(chunk2);
				output.write(ILocalStoreConstants.BEGIN_CHUNK); // another begin
				output.succeed();
				//
				output = new SafeChunkyOutputStream(target);
				output.write(chunk3);
				output.succeed();
				output = new SafeChunkyOutputStream(target);
				// fake failure
				output.write(chunk4);
				output.write(ILocalStoreConstants.END_CHUNK); // another end
				output.succeed();
				//
				output = new SafeChunkyOutputStream(target);
				output.write(chunk5);
				output.succeed();
				// fake failure
				output.write(fakeEnd);
				output.write(chunk6);
				output.succeed();
			} finally {
				output.close();
			}
		} catch (IOException e) {
			fail("2.0", e);
		}

		// read chunks
		SafeChunkyInputStream input = null;
		try {
			input = new SafeChunkyInputStream(target);
			try {
				byte[] read1 = new byte[chunk1.length];
				//byte[] read2 = new byte[chunk2.length];
				byte[] read3 = new byte[chunk3.length];
				byte[] read4 = new byte[chunk4.length];
				byte[] read5 = new byte[chunk5.length];
				byte[] read6 = new byte[fakeEnd.length + chunk6.length];
				assertTrue("3.0", input.read(read1) == chunk1.length);
				//assert("3.1", input.read(read2) == chunk2.length);
				assertTrue("3.2", input.read(read3) == chunk3.length);
				assertTrue("3.3", input.read(read4) == chunk4.length);
				assertTrue("3.4", input.read(read5) == chunk5.length);
				assertTrue("3.5", input.read(read6) == (fakeEnd.length + chunk6.length));
				assertTrue("3.6", compare(chunk1, read1));
				//assert("3.7", compare(chunk2, read2));
				assertTrue("3.8", compare(chunk3, read3));
				assertTrue("3.9", compare(chunk4, read4));
				assertTrue("3.10", compare(chunk5, read5));
				byte[] expected = merge(fakeEnd, chunk6);
				assertTrue("3.11", compare(expected, read6));
			} finally {
				input.close();
			}
		} catch (IOException e) {
			fail("3.20", e);
		}
		Workspace.clear(target); // make sure there was nothing here before
	}

	public void testAlmostEmpty() {
		File target = new File(temp, "target");
		Workspace.clear(target); // make sure there was nothing here before
		assertTrue("1.0", !target.exists());

		// open the file but don't write anything.
		SafeChunkyOutputStream output = null;
		try {
			output = new SafeChunkyOutputStream(target);
		} catch (IOException e) {
			fail("1.0", e);
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					fail("1.1", e);
				}
		}

		SafeChunkyInputStream input = null;
		try {
			input = new SafeChunkyInputStream(target);
			new DataInputStream(input).readUTF();
			fail("2.0");
		} catch (EOFException e) {
			// should hit here
		} catch (IOException e) {
			fail("2.1", e);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					fail("2.2", e);
				}
		}
	}

	public void testSimple() {
		File target = new File(temp, "target");
		Workspace.clear(target); // make sure there was nothing here before
		assertTrue("1.0", !target.exists());

		// write chunks
		byte[] chunk1 = getRandomString().getBytes();
		byte[] chunk2 = getRandomString().getBytes();
		byte[] chunk3 = getRandomString().getBytes();
		byte[] chunk4 = getRandomString().getBytes();
		byte[] chunk5 = getRandomString().getBytes();
		SafeChunkyOutputStream output = null;
		try {
			output = new SafeChunkyOutputStream(target);
			try {
				output.write(chunk1);
				output.succeed();
				output.write(chunk2);
				output.succeed();
				output.write(chunk3);
				output.succeed();
				output.write(chunk4);
				output.succeed();
				output.write(chunk5);
				output.succeed();
			} finally {
				output.close();
			}
		} catch (IOException e) {
			fail("2.0", e);
		}

		// read chunks
		SafeChunkyInputStream input = null;
		try {
			input = new SafeChunkyInputStream(target);
			try {
				byte[] read1 = new byte[chunk1.length];
				byte[] read2 = new byte[chunk2.length];
				byte[] read3 = new byte[chunk3.length];
				byte[] read4 = new byte[chunk4.length];
				byte[] read5 = new byte[chunk5.length];
				assertTrue("3.0", input.read(read1) == chunk1.length);
				assertTrue("3.1", input.read(read2) == chunk2.length);
				assertTrue("3.2", input.read(read3) == chunk3.length);
				assertTrue("3.3", input.read(read4) == chunk4.length);
				assertTrue("3.4", input.read(read5) == chunk5.length);
				assertTrue("3.5", compare(chunk1, read1));
				assertTrue("3.6", compare(chunk2, read2));
				assertTrue("3.7", compare(chunk3, read3));
				assertTrue("3.8", compare(chunk4, read4));
				assertTrue("3.9", compare(chunk5, read5));
			} finally {
				input.close();
			}
		} catch (IOException e) {
			fail("3.10", e);
		}
		Workspace.clear(target); // make sure there was nothing here before
	}
}
