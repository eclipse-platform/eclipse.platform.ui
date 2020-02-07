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
package org.eclipse.core.tests.internal.dtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.internal.dtree.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for complete data trees.
 */
public class DataTreeTest {
	DataTree tree, emptyTree;
	IPath rootKey, leftKey, rightKey;

	/**
	 * Init tests
	 */
	@Before
	public void setUp() {
		//data tree tests don't use the CoreTest environment

		emptyTree = new DataTree();
		tree = new DataTree();
		rootKey = Path.ROOT;

		/* Add two children to root */
		try {
			tree.createChild(rootKey, "leftOfRoot");
			tree.createChild(rootKey, "rightOfRoot");
		} catch (ObjectNotFoundException e) {
			fail("Error in setUp");
		}

		leftKey = rootKey.append("leftOfRoot");
		rightKey = rootKey.append("rightOfRoot");

		/* Add three children to left of root and one to right of root */
		try {
			tree.createChild(leftKey, "one");
			tree.createChild(leftKey, "two");
			tree.createChild(leftKey, "three");

			tree.createChild(rightKey, "rightOfRight");
		} catch (ObjectNotFoundException e) {
			fail("Error in setUp");
		}

	}

	/**
	 * Create a child of the specified node and give it the specified local name.<p>
	 * If a child with such a name exists, replace it with the new child
	 * @exception ObjectNotFoundException
	 *	parentKey does not exist in the receiver
	 */
	@Test
	public void testCreateChild() {

		boolean caught;
		int size;

		caught = false;
		/* Create child with bogus parent key */
		try {
			tree.createChild(rootKey.append("bogus"), "foobar");
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertTrue("1", caught);
		}

		caught = false;
		/* Create child of empty tree with bogus parent */
		try {
			emptyTree.createChild(rootKey.append("bogus"), "foobar");
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertTrue("2", caught);
		}

		/* Add child to empty tree */
		try {
			emptyTree.createChild(rootKey, "first");
		} catch (ObjectNotFoundException e) {
		}
		assertTrue("3", emptyTree.includes(rootKey.append("first")));

		/* Add root level child to non-empty tree */
		try {
			tree.createChild(rootKey, "NewTopLevel");
		} catch (ObjectNotFoundException e) {
		}
		assertTrue("4", tree.includes(rootKey.append("NewTopLevel")));
		assertTrue("5", tree.includes(leftKey));
		assertTrue("6", tree.includes(rightKey));
		assertTrue("7", tree.includes(leftKey.append("one")));

		/* Add child to leaf in non-empty tree */
		try {
			tree.createChild(leftKey.append("one"), "NewBottom");
		} catch (ObjectNotFoundException e) {
		}
		assertTrue("8", tree.includes(leftKey));
		assertTrue("9", tree.includes(rightKey));
		assertTrue("10", tree.includes(leftKey.append("one")));
		assertTrue("11", tree.includes(leftKey.append("one").append("NewBottom")));

		/* Add child to node containing only one child */
		try {
			tree.createChild(rightKey, "NewRight");
		} catch (ObjectNotFoundException e) {
		}
		assertTrue("12", tree.includes(leftKey));
		assertTrue("13", tree.includes(rightKey));
		assertTrue("14", tree.includes(rightKey.append("rightOfRight")));
		assertTrue("15", tree.includes(rightKey.append("NewRight")));

		/* Add same child twice */
		size = (tree.getNamesOfChildren(leftKey)).length;
		tree.createChild(leftKey, "double");
		tree.createChild(leftKey, "double");
		/* Make sure size has only increased by one */
		assertEquals(size + 1, tree.getNamesOfChildren(leftKey).length);

	}

	/**
	 * Delete the child with the specified local name from the specified
	 * node.  Note: this method requires both parentKey and localName,
	 * making it impossible to delete the root node.<p>
	 *
	 * @exception ObjectNotFoundException
	 *	a child of parentKey with name localName does not exist in the receiver
	 */
	@Test
	public void testDeleteChild() {

		boolean caught;

		/* Delete from empty */
		caught = false;
		try {
			emptyTree.deleteChild(rootKey, "non-existant");
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertTrue("1", caught);
		}

		/* delete a child that is not the child of parentKey */
		caught = false;
		try {
			tree.deleteChild(rootKey, "rightOfRight");
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertTrue("2", caught);
		}
		assertTrue("3", tree.includes(rightKey.append("rightOfRight")));

		/* delete with bogus parent */
		caught = false;
		try {
			tree.deleteChild(rootKey.append("bogus"), "rightOfRight");
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertTrue("4", caught);
		}
		assertTrue("5", tree.includes(rightKey.append("rightOfRight")));

		/* delete with bogus local name */
		caught = false;
		try {
			tree.deleteChild(leftKey, "four");
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertTrue("6", caught);
		}
		assertTrue("7", tree.includes(leftKey));

		/* Delete a node with children */
		try {
			tree.deleteChild(rootKey, "leftOfRoot");
		} catch (ObjectNotFoundException e) {
		}

		assertFalse("8", tree.includes(leftKey));
		assertFalse("9", tree.includes(leftKey.append("one")));
		assertTrue("10", tree.includes(rootKey));

		/* delete a leaf */
		try {
			tree.deleteChild(rightKey, "rightOfRight");
		} catch (ObjectNotFoundException e) {
		}
		assertFalse("11", tree.includes(rightKey.append("rightOfRight")));
		assertTrue("12", tree.includes(rightKey));
	}

	/**
	 * Initialize the receiver so that it is a complete, empty tree.  It does
	 * not represent a delta on another tree.  An empty tree is defined to
	 * have a root node with nil data and no children.
	 */
	@Test
	public void testEmpty() {

		assertTrue("1", emptyTree.includes(rootKey));
		assertNotNull("2", TestHelper.getRootNode(emptyTree));
		assertEquals("3", 0, TestHelper.getRootNode(emptyTree).getChildren().length);
	}

	/**
	 * Answer the key of the child with the given index of the
	 * specified node.<p>
	 *
	 * @param NodeKey, int
	 * @return NodeKey
	 *
	 * @exception ObjectNotFoundException
	 * 	parentKey does not exist in the receiver
	 * @exception ArrayIndexOutOfBoundsException
	 *	if no child with the given index (runtime exception)
	 */
	@Test
	public void testGetChild() {

		boolean caught;

		/* Get valid children */
		caught = false;
		try {
			assertTrue(tree.getChild(rootKey, 0).equals(leftKey));
			assertTrue(tree.getChild(leftKey, 2).equals(leftKey.append("two")));
		} catch (ObjectNotFoundException e) {
			caught = true;
		}
		assertFalse(caught);

		/* Get non-existant child of root */
		caught = false;
		try {
			tree.getChild(rootKey, 99);
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		} catch (ObjectNotFoundException e) {
			fail();
		}
		assertTrue(caught);

		/* Get non-existant child of interior node */
		caught = false;
		try {
			tree.getChild(leftKey, 99);
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		} catch (ObjectNotFoundException e) {
			fail();
		}
		assertTrue(caught);

		/* Get non-existant child of leaf node */
		caught = false;
		try {
			tree.getChild(leftKey.append("one"), 99);
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		} catch (ObjectNotFoundException e) {
			fail();
		}
		assertTrue(caught);

		/* Try to getChild using non-existent key */
		caught = false;
		try {
			tree.getChild(rootKey.append("bogus"), 0);
		} catch (ObjectNotFoundException e) {
			caught = true;
		}
		assertTrue(caught);
	}

	/**
	 * Answer  the number of children of the specified node.
	 *
	 * @exception ObjectNotFoundException
	 *	parentKey does not exist in the receiver
	 */
	@Test
	public void testGetChildCount() {

		boolean caught;

		caught = false;
		try {
			/* empty tree */
			assertEquals("1", 0, emptyTree.getChildCount(rootKey));

			/* root node */
			assertEquals("2", 2, tree.getChildCount(rootKey));

			/* interior nodes */
			assertEquals("3", 3, tree.getChildCount(leftKey));
			assertEquals("4", 1, tree.getChildCount(rightKey));

			/* leaf nodes */
			assertEquals("5", 0, tree.getChildCount(leftKey.append("one")));
			assertEquals("6", 0, tree.getChildCount(leftKey.append("three")));
			assertEquals("7", 0, tree.getChildCount(rightKey.append("rightOfRight")));
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertFalse(caught);
		}

		caught = false;
		/* invalid parent key */
		try {
			tree.getChildCount(rootKey.append("bogus"));
		} catch (ObjectNotFoundException e) {
			caught = true;
		}
		assertTrue(caught);

		/* invalid parent of empty tree */
		caught = false;
		try {
			emptyTree.getChildCount(rootKey.append("bogus"));
		} catch (ObjectNotFoundException e) {
			caught = true;
		}
		assertTrue(caught);
	}

	/**
	 * Answer the keys for the children of the specified node.
	 *
	 * @exception ObjectNotFoundException
	 *	parentKey does not exist in the receiver"
	 */
	@Test
	public void testGetChildren() {

		boolean caught;
		IPath testChildren[], rootChildren[] = {leftKey, rightKey}, leftChildren[] = {leftKey.append("one"), leftKey.append("two"), leftKey.append("three")}, rightChildren[] = {rightKey.append("rightOfRight")};

		caught = false;
		try {
			/* empty tree */
			testChildren = emptyTree.getChildren(rootKey);
			assertEquals("1", 0, testChildren.length);

			/* root node */
			testChildren = tree.getChildren(rootKey);
			assertEquals("2", 2, testChildren.length);
			assertTrue("3", testChildren[0].equals(rootChildren[0]));
			assertTrue("4", testChildren[1].equals(rootChildren[1]));

			/* interior nodes */
			testChildren = tree.getChildren(leftKey);
			assertEquals("5", 3, testChildren.length);
			assertTrue("6", testChildren[0].equals(leftChildren[0]));
			assertTrue("7", testChildren[2].equals(leftChildren[1]));
			assertTrue("8", testChildren[1].equals(leftChildren[2]));

			/* leaf nodes */
			testChildren = tree.getChildren(leftChildren[0]);
			assertEquals("9", 0, testChildren.length);

			testChildren = tree.getChildren(rightChildren[0]);
			assertEquals("10", 0, testChildren.length);
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertFalse("11", caught);
		}

		caught = false;
		/* invalid parent key */
		try {
			tree.getChildren(rootKey.append("bogus"));
		} catch (ObjectNotFoundException e) {
			caught = true;
		}
		assertTrue("12", caught);

		/* invalid parent of empty tree */
		caught = false;
		try {
			emptyTree.getChildren(rootKey.append("bogus"));
		} catch (ObjectNotFoundException e) {
			caught = true;
		}
		assertTrue("13", caught);
	}

	/**
	 * Answer the local names for the children of the specified node.
	 *
	 * @exception ObjectNotFoundException
	 *	parentKey does not exist in the receiver
	 */
	@Test
	public void testGetNamesOfChildren() {

		boolean caught;
		String testChildren[], rootChildren[] = {"leftOfRoot", "rightOfRoot"}, leftChildren[] = {"one", "two", "three"};

		caught = false;
		try {
			/* empty tree */
			testChildren = emptyTree.getNamesOfChildren(rootKey);
			assertEquals("1", 0, testChildren.length);

			/* root node */
			testChildren = tree.getNamesOfChildren(rootKey);
			assertEquals("2", 2, testChildren.length);
			assertTrue("3", testChildren[0].equals(rootChildren[0]));
			assertTrue("4", testChildren[1].equals(rootChildren[1]));

			/* interior nodes */
			testChildren = tree.getNamesOfChildren(leftKey);
			assertEquals("5", 3, testChildren.length);
			assertTrue("6", testChildren[0].equals(leftChildren[0]));
			assertTrue("7", testChildren[2].equals(leftChildren[1]));
			assertTrue("8", testChildren[1].equals(leftChildren[2]));

			/* leaf nodes */
			testChildren = tree.getNamesOfChildren(leftKey.append("one"));
			assertEquals("9", 0, testChildren.length);

			testChildren = tree.getNamesOfChildren(rightKey.append("rightOfRight"));
			assertEquals("10", 0, testChildren.length);
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertFalse("11", caught);
		}

		caught = false;
		/* invalid parent key */
		try {
			tree.getNamesOfChildren(rootKey.append("bogus"));
		} catch (ObjectNotFoundException e) {
			caught = true;
		}
		assertTrue("12", caught);

		/* invalid parent of empty tree */
		caught = false;
		try {
			emptyTree.getNamesOfChildren(rootKey.append("bogus"));
		} catch (ObjectNotFoundException e) {
			caught = true;
		}
		assertTrue("13", caught);
	}

	/**
	 * Answer true if the receiver includes a node with the given key, false
	 * otherwise.
	 */
	@Test
	public void testIncludes() {

		/* tested in testCreateChild() and testDeleteChild() */

		assertTrue(emptyTree.includes(rootKey));
		assertTrue(tree.includes(rootKey));
		assertTrue(tree.includes(leftKey));
		assertTrue(tree.includes(rightKey));
		assertTrue(tree.includes(leftKey.append("one")));
		assertTrue(tree.includes(rightKey.append("rightOfRight")));

		assertFalse(emptyTree.includes(rootKey.append("bogus")));
		assertFalse(tree.includes(rootKey.append("bogus")));
		assertFalse(tree.includes(leftKey.append("bogus")));
		assertFalse(tree.includes(leftKey.append("one").append("bogus")));
		assertFalse(tree.includes(rightKey.append("bogus")));
	}
}
