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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.runtime.IPath;

public class DeltaChainFlatteningTest extends ElementTreeSerializationTest {
	protected ElementTree[] fDeltaChain;
	protected ElementTree[] fRefriedDeltaChain;

	public DeltaChainFlatteningTest() {
		super(null);
	}

	public DeltaChainFlatteningTest(String name) {
		super(name);
	}

	/**
	 * doRead method comment.
	 */
	public Object doRead(ElementTreeReader reader, DataInputStream input) throws IOException {
		return reader.readDeltaChain(input);
	}

	/**
	 * Runs a test for this class at a certain depth and path
	 */
	public void doTest(IPath path, int depth) {
		fSubtreePath = path;
		fDepth = depth;
		fDeltaChain = TestUtil.doRoutineOperations(fTree, project1);
		TestUtil.scramble(fDeltaChain);

		ElementTree[] refried = (ElementTree[]) doPipeTest();
		for (int j = 0; j < refried.length; j++) {
			TestUtil.assertEqualTrees("Same after delta chain serialize", fDeltaChain[j], refried[j], fSubtreePath, fDepth);
		}
	}

	/**
	 * doWrite method comment.
	 */
	public void doWrite(ElementTreeWriter writer, DataOutputStream output) throws IOException {
		writer.writeDeltaChain(fDeltaChain, fSubtreePath, fDepth, output, DefaultElementComparator.getComparator());
	}

	/**
	 * Sets up the delta chain to be serialized
	 */
	protected void setUp() throws Exception {
		super.setUp();
		fTree = TestUtil.createTestElementTree();
		/* do a bunch of operations on fTree to build a delta chain */
		fDeltaChain = TestUtil.doManyRoutineOperations(fTree, project1);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(DeltaChainFlatteningTest.class);
		return suite;
	}

	/**
	 * Tests the reading and writing of element deltas
	 */
	public void test0() {
		doExhaustiveTests();
	}
}
