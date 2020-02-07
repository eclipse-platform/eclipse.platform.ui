/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.watson;

import java.io.*;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for <code>ElementTreeWriter</code> and
 * <code>ElementTreeReader</code>.
 */
public class TreeFlatteningTest extends ElementTreeSerializationTest {

	/**
	 * Performs the serialization activity for this test
	 */
	@Override
	public Object doRead(ElementTreeReader reader, DataInputStream input) throws IOException {
		return reader.readTree(input);
	}

	/**
	 * Runs a test for this class at a certain depth and path
	 */
	@Override
	public void doTest(IPath path, int depth) {
		/* Get an element tree from somewhere. */
		fTree = TestUtil.createTestElementTree();
		fSubtreePath = path;
		fDepth = depth;

		ElementTree newTree = (ElementTree) doPipeTest();

		TestUtil.assertEqualTrees(this.getClass() + "test0", fTree, newTree, fSubtreePath, fDepth);
	}

	/**
	 * Performs the serialization activity for this test
	 */
	@Override
	public void doWrite(ElementTreeWriter writer, DataOutputStream output) throws IOException {
		writer.writeTree(fTree, fSubtreePath, fDepth, output);
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		fTree = TestUtil.createTestElementTree();
	}

	@Test
	public void test0() {
		/* Get an element tree from somewhere. */
		fTree = TestUtil.createTestElementTree();
		ElementTree newTree = (ElementTree) doFileTest();

		TestUtil.assertEqualTrees(this.getClass() + "test0", fTree, newTree);
	}

	/**
	 * Tests the reading and writing of element deltas
	 */
	@Test
	public void testExhaustive() {
		doExhaustiveTests();
	}

	@Test
	public void testNullData() {
		/* Get an element tree from somewhere. */
		fTree = TestUtil.createTestElementTree();
		fTree = fTree.newEmptyDelta();

		/* set some elements to have null data */
		fTree.setElementData(solution, null);
		fTree.setElementData(folder2, null);
		fTree.immutable();

		ElementTree newTree = (ElementTree) doPipeTest();

		TestUtil.assertEqualTrees(this.getClass() + "test0", fTree, newTree);
	}

	@Test
	public void testWriteRoot() {
		/* Get an element tree from somewhere. */
		fTree = TestUtil.createTestElementTree();
		fSubtreePath = Path.ROOT;

		ElementTree newTree = (ElementTree) doPipeTest();

		TestUtil.assertEqualTrees(this.getClass() + "test0", fTree, newTree, fSubtreePath);
	}
}
