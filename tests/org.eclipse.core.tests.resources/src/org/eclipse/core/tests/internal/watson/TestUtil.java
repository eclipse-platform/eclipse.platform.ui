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

import java.util.Random;
import java.util.Vector;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Framework for running tests on element tree components.
 * Supplies convenience methods and some representative element trees.
 */
abstract class TestUtil extends WatsonTest implements IPathConstants {
	public TestUtil(String name) {
		super(name);
	}

	static final IElementComparator fComparator = TestElementComparator.getComparator();

	/**
	 * Asserts that the given comparison value has the added
	 * bit set, and the changed/removed bits blank
	 */
	static void assertAdded(int compare) {
		assertTrue(compare == TestElementComparator.ADDED);
	}

	/**
	 * Asserts that the given comparison value has the changed
	 * bit set, and the added/removed bits blank
	 */
	static void assertChanged(int compare) {
		assertTrue(compare == TestElementComparator.CHANGED);
	}

	/**
	 * Asserts that two trees are equal. If they are not
	 * an AssertionFailedError is thrown.
	 * @param message the detail message for this assertion
	 * @param expected the expected value of a tree
	 * @param actual the actual value of a tree
	 */
	static protected void assertEqualTrees(String message, ElementTree expected, ElementTree actual) {
		assertEqualTrees(message, expected, actual, Path.ROOT, ElementTreeWriter.D_INFINITE);
	}

	/**
	 * Asserts that two trees are equal. If they are not
	 * an AssertionFailedError is thrown.
	 * @param message the detail message for this assertion
	 * @param expected the expected value of a tree
	 * @param actual the actual value of a tree
	 * @param depth The depth to compare to.
	 */
	static protected void assertEqualTrees(String message, ElementTree expected, ElementTree actual, int depth) {
		assertEqualTrees(message, expected, actual, Path.ROOT, depth);
	}

	/**
	 * Asserts that two trees are equal from the given path downwards. If they are not
	 * an AssertionFailedError is thrown.
	 * @param message the detail message for this assertion
	 * @param expected the expected value of a tree
	 * @param actual the actual value of a tree
	 */
	static protected void assertEqualTrees(String message, ElementTree expected, ElementTree actual, IPath path) {
		assertEqualTrees(message, expected, actual, path, ElementTreeWriter.D_INFINITE);
	}

	/**
	 * Asserts that two trees are equal. If they are not
	 * an AssertionFailedError is thrown.
	 * @param message the detail message for this assertion
	 * @param expected the expected value of a tree
	 * @param actual the actual value of a tree
	 * @param depth The depth to compare to.
	 */
	static protected void assertEqualTrees(String msg, ElementTree expected, ElementTree actual, IPath path, int depth) {
		/* check node at current path */
		assertTrue(msg, expected.includes(path));
		assertTrue(msg, actual.includes(path));
		assertEquals(msg, expected.getElementData(path), actual.getElementData(path));

		if (depth != 0) {
			/* get the children */
			IPath[] expectedChildren = expected.getChildren(path);
			IPath[] actualChildren = actual.getChildren(path);
			assertTrue("Number of children", expectedChildren.length == actualChildren.length);

			int newDepth = depth;
			if (depth != ElementTreeWriter.D_INFINITE) {
				--newDepth;
			}
			for (int i = 0; i < expectedChildren.length; i++) {
				assertEquals("children IDs", expectedChildren[i], actualChildren[i]);
				assertEqualTrees("Recursive call", expected, actual, expectedChildren[i], newDepth);
			}
		}
	}

	/**
	 * Asserts that the given tree contains all of the given paths
	 */
	static protected void assertHasPaths(ElementTree tree, IPath[] paths) {
		for (int i = 0; i < paths.length; i++) {
			assertTrue("assertHasPaths" + i, tree.includes(paths[i]));
		}
	}

	/**
	 * Asserts that the given comparison value has the changed
	 * bit set, and the added/removed bits blank
	 */
	static void assertNoChange(int compare) {
		assertTrue(compare == 0);
	}

	/**
	 * Asserts that the given tree doesn not contain all of the given paths
	 */
	static protected void assertNoPaths(ElementTree tree, IPath[] paths) {
		for (int i = 0; i < paths.length; i++) {
			assertTrue("assertNoPaths: " + paths[i], !tree.includes(paths[i]));
		}
	}

	/**
	 * Asserts that the given comparison value has the removed
	 * bit set, and the changed/added bits blank
	 */
	static void assertRemoved(int compare) {
		assertTrue(compare == TestElementComparator.REMOVED);
	}

	/**
	 * Asserts that the Test ElementTree has its original structure
	 */
	static protected void assertTreeStructure(ElementTree tree) {
		assertHasPaths(tree, getTreePaths());

		IPath[] children = tree.getChildren(Path.ROOT);
		assertTrue(children.length == 1);

		/* solution children */
		children = tree.getChildren(children[0]);
		assertTrue(children.length == 2);
		assertTrue(tree.getChildren(project1).length == 0);

		/* project2 children */
		children = tree.getChildren(children[1]);
		assertTrue(children.length == 3);
		assertTrue(tree.getChildren(file1).length == 0);
		assertTrue(tree.getChildren(folder2).length == 0);

		/* folder1 children */
		children = tree.getChildren(children[1]);
		assertTrue(children.length == 3);
		assertTrue(tree.getChildren(file2).length == 0);
		assertTrue(tree.getChildren(folder4).length == 0);

		/* folder3 children */
		children = tree.getChildren(children[1]);
		assertTrue(children.length == 1);
		assertTrue(tree.getChildren(file3).length == 0);
	}

	static ElementTree createTestElementTree() {
		/**
		 * This is a sample element tree on which tests are performed. 
		 * The Paths in this tree are found in IPathConstants.  The tree's 
		 * basic structure is as follows:
		 *	solution
		 *		project1
		 *		project2
		 *			file1
		 *			folder1
		 *				file2
		 *				folder3
		 *					file3
		 *				folder4
		 *			folder2
		 */

		ElementTree tree = new ElementTree();
		tree.createElement(solution, new String("solution"));
		tree.createElement(project1, new String("project1"));
		tree.createElement(project2, new String("project2"));
		tree.createElement(file1, new String("file1"));
		tree.createElement(folder1, new String("folder1"));
		tree.createElement(folder2, new String("folder2"));
		tree.createElement(file2, new String("file2"));
		tree.createElement(folder3, new String("folder3"));
		tree.createElement(folder4, new String("folder4"));
		tree.createElement(file3, new String("file3"));

		return tree;
	}

	/**
	 * Does several routine operations on the subtree rooted at
	 * the given path.  Opens a new delta after every operation.
	 * Returns an array of all the intermediary ElementTrees.
	 */
	static protected ElementTree[] doManyRoutineOperations(ElementTree tree, IPath path) {
		Vector trees = new Vector();
		trees.addElement(tree);

		int repeat = 1;

		/* create file elements */
		tree = tree.newEmptyDelta();
		IPath[] files = getFilePaths(path);
		for (int i = 0; i < files.length; i++) {
			Object data = files[i].toString();
			tree.createElement(files[i], data);

			tree.immutable();
			trees.addElement(tree);
			tree = tree.newEmptyDelta();
		}

		/* modify the data of all file elements a few times */
		for (int i = 0; i < repeat; i++) {
			Object data = "data" + i;
			for (int f = 0; f < files.length; f++) {
				tree.setElementData(files[f], data);
				tree.immutable();
				trees.addElement(tree);
				tree = tree.newEmptyDelta();
			}
		}

		/* delete all file elements */
		for (int i = 0; i < files.length; i++) {
			tree.deleteElement(files[i]);
			tree.immutable();
			trees.addElement(tree);
			tree = tree.newEmptyDelta();
		}

		ElementTree[] results = new ElementTree[trees.size()];
		trees.copyInto(results);
		return results;
	}

	/**
	 * Does several routine operations on the subtree rooted at
	 * the given path.  Returns an array of all the intermediary elementtrees.
	 */
	static protected ElementTree[] doRoutineOperations(ElementTree tree, IPath path) {
		Vector trees = new Vector();
		trees.addElement(tree);

		int repeat = 1;

		/* create file elements */
		tree = tree.newEmptyDelta();
		IPath[] files = getFilePaths(path);
		for (int i = 0; i < files.length; i++) {
			Object data = files[i].toString();

			tree.createElement(files[i], data);
		}

		tree.immutable();
		trees.addElement(tree);
		tree = tree.newEmptyDelta();

		/* modify the data of all file elements a few times */
		for (int i = 0; i < repeat; i++) {
			Object data = "data" + i;
			for (int f = 0; f < files.length; f++) {
				tree.setElementData(files[f], data);
			}
		}

		tree.immutable();
		trees.addElement(tree);
		tree = tree.newEmptyDelta();

		/* delete all file elements */
		for (int i = 0; i < files.length; i++) {
			tree.deleteElement(files[i]);
		}

		tree.immutable();
		trees.addElement(tree);

		ElementTree[] results = new ElementTree[trees.size()];
		trees.copyInto(results);
		return results;
	}

	/**
	 * Returns an element tree comparator
	 */
	static IElementComparator getComparator() {
		return fComparator;
	}

	/**
	 * Returns an array of paths below the given path.
	 */
	static protected IPath[] getFilePaths(IPath project3) {
		String[] names = getJavaLangUnits();
		IPath[] paths = new IPath[names.length];
		for (int i = 0; i < paths.length; i++) {
			paths[i] = project3.append(names[i]);
		}
		return paths;
	}

	/**
	 * Returns strings representing javalang classes
	 */
	static protected String[] getJavaLangUnits() {
		return new String[] {"AbstractMethodError.java", "ArithmeticException.java", "ArrayIndexOutOfBoundsException.java", "ArrayStoreException.java", "Boolean.java", "Byte.java", "Character.java", "Class.java", "ClassCastException.java", "ClassCircularityError.java", "ClassFormatError.java", "ClassLoader.java", "ClassNotFoundException.java", "Cloneable.java", "CloneNotSupportedException.java", "Compiler.java", "Double.java", "Error.java",};
	}

	/**
	 * Returns the paths for all the elements in the tree in top-down order
	 */
	static protected IPath[] getTreePaths() {
		return new IPath[] {solution, project1, project2, folder1, folder2, folder3, folder4, file1, file2, file3};
	}

	/**
	 * Randomly scrambles an array.
	 */
	static protected void scramble(Object[] first) {
		Random random = new Random(System.currentTimeMillis());

		final int len = first.length;
		for (int i = 0; i < len * 100; i++) {
			/* get any array offset */
			int off1 = (int) (random.nextFloat() * len);
			if (off1 == len)
				continue;

			/* get another array offset */
			int off2 = (int) (random.nextFloat() * len);
			if (off2 == len)
				continue;

			/* switch */
			Object temp = first[off1];
			first[off1] = first[off2];
			first[off2] = temp;
		}
	}

	/**
	 * Randomly scrambles two arrays, ensuring that both
	 * arrays undergo the same permutation.
	 */
	static protected void scramble(Object[] first, Object[] second) {
		assertTrue(first.length == second.length);
		Random random = new Random(System.currentTimeMillis());

		final int len = first.length;
		for (int i = 0; i < len * 100; i++) {
			/* get any array offset */
			int off1 = (int) (random.nextFloat() * len);
			if (off1 == len)
				continue;

			/* get another array offset */
			int off2 = (int) (random.nextFloat() * len);
			if (off2 == len)
				continue;

			/* switch */
			Object temp = first[off1];
			first[off1] = first[off2];
			first[off2] = temp;

			temp = second[off1];
			second[off1] = second[off2];
			second[off2] = temp;
		}
	}

}
