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
package org.eclipse.core.tests.internal.watson;

import java.io.*;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.runtime.IPath;

public abstract class ElementTreeSerializationTest extends WatsonTest implements IPathConstants {

	protected ElementTree fTree;

	String root = System.getProperty("java.io.tmpdir");
	protected File tempFile = new File(root + "/temp/TestFlattening");

	class WriterThread implements Runnable {
		DataOutputStream fDataOutputStream;
		ElementTreeWriter fWriter;

		public void setStream(DataOutputStream stream) {
			fDataOutputStream = stream;
		}

		public void setWriter(ElementTreeWriter writer) {
			fWriter = writer;
		}

		public void run() {
			try {
				doWrite(fWriter, fDataOutputStream);
			} catch (IOException e) {
				assertTrue("Error writing delta", false);
				e.printStackTrace();
			}
		}
	}

	class ReaderThread implements Runnable {
		DataInputStream fDataInputStream;
		ElementTreeReader fReader;
		Object fRefried;

		public void setStream(DataInputStream stream) {
			fDataInputStream = stream;
		}

		public void setReader(ElementTreeReader reader) {
			fReader = reader;
		}

		public Object getReconstitutedObject() {
			return fRefried;
		}

		public void run() {
			try {
				fRefried = doRead(fReader, fDataInputStream);
			} catch (IOException e) {
				assertTrue("Error reading delta", false);
				e.printStackTrace();
			}
		}
	}

	WriterThread writerThread = new WriterThread();
	ReaderThread readerThread = new ReaderThread();

	/**
	 * The subtree to write
	 */
	protected IPath fSubtreePath;

	/**
	 * The depth of the tree to write.
	 */
	protected int fDepth;

	public ElementTreeSerializationTest() {
		super(null);
	}

	/**
	 * ElementTreeSerializationTests constructor comment.
	 * @param name java.lang.String
	 */
	public ElementTreeSerializationTest(String name) {
		super(name);
	}

	/**
	 * Performs exhaustive tests for all subtrees and all depths
	 */
	protected void doExhaustiveTests() {
		IPath[] paths = TestUtil.getTreePaths();
		int[] depths = getTreeDepths();

		for (int p = 0; p < paths.length; p++) {
			for (int d = 0; d < depths.length; d++) {
				doTest(paths[p], depths[d]);
			}
		}
	}

	/** 
	 * Write a flattened element tree to a file and read it back.
	 */
	public Object doFileTest() {
		IElementInfoFlattener fac = getFlattener();

		Object newTree = null;
		ElementTreeWriter writer = new ElementTreeWriter(fac);
		ElementTreeReader reader = new ElementTreeReader(fac);
		FileOutputStream fos = null;
		FileInputStream fis = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;

		/* Write the element tree. */
		try {
			File dir = tempFile.getParentFile();
			dir.mkdirs();
			fos = new FileOutputStream(tempFile);
			dos = new DataOutputStream(fos);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue("Unable to open ouput file", false);
		}
		try {
			doWrite(writer, dos);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue("Error writing tree to file", false);
		} finally {
			try {
				dos.flush();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				assertTrue("Unable to close output file!", false);
			}
		}

		/* Read the element tree. */
		try {
			fis = new FileInputStream(tempFile);
			dis = new DataInputStream(fis);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue("Unable to open input file", false);
		}
		try {
			newTree = doRead(reader, dis);
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue("Error reading tree from file", false);
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
				assertTrue("Unable to close input file!", false);
			}
		}
		return newTree;
	}

	/** Pipe a flattened element tree from writer to reader threads.
	 */
	public Object doPipeTest() {
		IElementInfoFlattener fac = getFlattener();
		Object refried = null;
		ElementTreeWriter w = new ElementTreeWriter(fac);
		ElementTreeReader r = new ElementTreeReader(fac);
		PipedOutputStream pout;
		PipedInputStream pin;
		DataOutputStream oos;
		DataInputStream ois;

		/* Pipe the element tree from writer to reader threads. */
		try {
			pout = new PipedOutputStream();
			pin = new PipedInputStream(pout);
			oos = new DataOutputStream(pout); //new FileOutputStream(FILE_NAME));
			ois = new DataInputStream(pin);
			writerThread.setStream(oos);
			readerThread.setStream(ois);
			writerThread.setWriter(w);
			readerThread.setReader(r);
			Thread thread1 = new Thread(writerThread);
			Thread thread2 = new Thread(readerThread);
			thread1.start();
			thread2.start();
			while (thread2.isAlive()) {
				try {
					thread2.join();
				} catch (InterruptedException excp) {
				}
			}
			refried = readerThread.getReconstitutedObject();
			ois.close();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue("Unable to open stream:", false);
		}
		return refried;
	}

	/**
	 * Performs the serialization activity for this test
	 */
	public abstract Object doRead(ElementTreeReader reader, DataInputStream input) throws IOException;

	/**
	 * Runs a test for this class at a certain depth and path
	 */
	public abstract void doTest(IPath path, int depth);

	/**
	 * Performs the serialization activity for this test
	 */
	public abstract void doWrite(ElementTreeWriter writer, DataOutputStream output) throws IOException;

	/**
	 * Tests the reading and writing of element deltas
	 */
	public IElementInfoFlattener getFlattener() {
		return new IElementInfoFlattener() {
			public void writeElement(IPath path, Object data, DataOutput output) throws IOException {
				if (data == null) {
					output.writeUTF("null");
				} else {
					output.writeUTF((String) data);
				}
			}

			public Object readElement(IPath path, DataInput input) throws IOException {
				String data = input.readUTF();
				if ("null".equals(data)) {
					return null;
				} else {
					return data;
				}
			}
		};
	}

	/**
	 * Returns all the different possible depth values for this tree.
	 * To be conservative, it is okay if some of the returned depths
	 * are not possible.
	 */
	protected int[] getTreeDepths() {
		return new int[] {-1, 0, 1, 2, 3, 4};
	}

	protected void setUp() throws Exception {
		fTree = TestUtil.createTestElementTree();
		/* default subtree we are interested in */
		fSubtreePath = solution;

		/* default depth is the whole tree */
		fDepth = ElementTreeWriter.D_INFINITE;
	}

	/**
	 * 
	 */
	protected void tearDown() throws Exception {
		//ElementTree tests don't use the CoreTest infrastructure
		tempFile.delete();
	}
}
