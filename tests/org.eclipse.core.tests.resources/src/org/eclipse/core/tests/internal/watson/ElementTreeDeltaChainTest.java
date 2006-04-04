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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.runtime.IPath;

/**
 * Tests the ElementTree.mergeDeltaChain() method.
 */
public class ElementTreeDeltaChainTest extends WatsonTest implements IPathConstants {
	protected ElementTree fTree;
	protected IPath project3;

	public ElementTreeDeltaChainTest() {
		super(null);
	}

	public ElementTreeDeltaChainTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		fTree = TestUtil.createTestElementTree();
		project3 = solution.append("project3");
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ElementTreeDeltaChainTest.class);
		return suite;
	}

	/**
	 * 
	 */
	protected void tearDown() throws Exception {
		//ElementTree tests don't use the CoreTest infrastructure
	}

	/**
	 * Tries some bogus merges and makes sure an exception is thrown.
	 */
	public void testIllegalMerges() {
		fTree = fTree.newEmptyDelta();

		/* null trees */
		boolean caught = false;
		Exception ex = null;
		try {
			fTree.mergeDeltaChain(solution, null);
		} catch (RuntimeException e) {
			caught = true;
			ex = e;
		} finally {
			assertTrue(ex.getMessage(), caught);
		}

		/* create a tree with a whole bunch of operations in project3 */
		ElementTree projectTree = new ElementTree();
		projectTree.createElement(solution, "Dummy");
		projectTree.createElement(project3, "project3");
		ElementTree[] trees = TestUtil.doManyRoutineOperations(projectTree, project3);

		/* scramble the order of the project trees */
		TestUtil.scramble(trees);

		/* null handle */
		caught = false;
		try {
			fTree.mergeDeltaChain(null, trees);
		} catch (RuntimeException e) {
			caught = true;
			ex = e;
		} finally {
			assertTrue(ex.getMessage(), caught);
		}

		/* non-existent handle */
		caught = false;
		try {
			fTree.mergeDeltaChain(solution.append("bogosity"), trees);
		} catch (RuntimeException e) {
			caught = true;
			ex = e;
		} finally {
			assertTrue(ex.getMessage(), caught);
		}

		/* immutable receiver */
		caught = false;
		try {
			fTree.immutable();
			fTree.mergeDeltaChain(solution, trees);
		} catch (RuntimeException e) {
			caught = true;
			ex = e;
		} finally {
			assertTrue(ex.getMessage(), caught);
		}
	}

	/**
	 * Tests the mergeDeltaChain method
	 */
	public void testMergeDeltaChain() {
		/* create a tree with a whole bunch of operations in project3 */
		ElementTree projectTree = new ElementTree();
		projectTree.createElement(solution, "Dummy");
		projectTree.createElement(project3, "project3");
		ElementTree[] trees = TestUtil.doManyRoutineOperations(projectTree, project3);

		/* create a copy for testing purposes */
		ElementTree copyTree = new ElementTree();
		copyTree.createElement(solution, "Dummy");
		copyTree.createElement(project3, "project3");
		ElementTree[] copies = TestUtil.doManyRoutineOperations(copyTree, project3);

		/* scramble the order of the project trees */
		TestUtil.scramble(trees, copies);

		/* do a bunch of operations on fTree to build a delta chain */
		TestUtil.doRoutineOperations(fTree, solution);
		fTree = fTree.newEmptyDelta();

		/* merge the delta chains */
		ElementTree newTree = fTree.mergeDeltaChain(project3, trees);
		assertTrue("returned tree should be different", !(newTree == fTree));
		assertTrue("returned tree should be open", !newTree.isImmutable());

		/* make sure old and new trees have same structure */
		for (int i = 0; i < trees.length; i++) {
			TestUtil.assertEqualTrees("testMergeDeltaChain: " + i, copies[i].getSubtree(project3), trees[i].getSubtree(project3));
		}

		TestUtil.assertHasPaths(newTree, TestUtil.getTreePaths());
		TestUtil.assertHasPaths(newTree, new IPath[] {project3});
	}

	/**
	 * Performs merge on trees that have nodes in common.  The chain
	 * being merged should overwrite the receiver.
	 */
	public void testMergeOverwrite() {
		/* create a tree with a whole bunch of operations in project3 */
		ElementTree projectTree = new ElementTree();
		projectTree.createElement(solution, "Dummy");
		projectTree.createElement(project3, "project3");

		/* form a delta chain on fTree */
		ElementTree[] trees = TestUtil.doManyRoutineOperations(fTree, solution);

		/* scramble the order of the project trees */
		TestUtil.scramble(trees);

		/* merge the delta chains */
		ElementTree newTree = projectTree.mergeDeltaChain(solution, trees);

		assertTrue(!(newTree == projectTree));
		assertTrue(newTree.getElementData(solution).equals("solution"));
		TestUtil.assertTreeStructure(newTree);
	}
}
