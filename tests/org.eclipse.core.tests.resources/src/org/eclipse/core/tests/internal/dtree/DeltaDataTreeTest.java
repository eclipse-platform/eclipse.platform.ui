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
package org.eclipse.core.tests.internal.dtree;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.dtree.*;
import org.eclipse.core.internal.watson.DefaultElementComparator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Test class for delta trees.
 */
public class DeltaDataTreeTest extends DataTreeTest {

	DeltaDataTree tree, emptyTree, changedTree, deltaTree;

	public DeltaDataTreeTest() {
		super();
	}

	public DeltaDataTreeTest(String name) {
		super(name);
	}

	/**
	 * Assert that the given tree is the same as the final delta tree
	 * created in the string of deltas in testLongDeltaChain and testReroot
	 *
	 */
	public void assertDelta(DeltaDataTree originalTree) {

		/* compare to tree */
		assertTrue("1", originalTree.includes(rootKey));
		assertTrue("2", originalTree.includes(leftKey));
		assertTrue("3", originalTree.includes(rightKey));
		assertTrue("4", originalTree.includes(rootKey.append("newTopLevel")));

		assertTrue("5", originalTree.includes(leftKey.append("new")));
		assertTrue("6", originalTree.includes(leftKey.append("two")));
		assertTrue("7", originalTree.includes(leftKey.append("three")));
		assertTrue("8", originalTree.includes(rightKey.append("rightOfRight")));

		/* this was removed from "tree" */
		assertTrue("9", !originalTree.includes(leftKey.append("one")));
	}

	/**
	 * Assert that the given tree is the same as the original "tree" created
	 * during setup
	 */
	public void assertTree(DeltaDataTree originalTree) {

		/* compare to tree */
		assertTrue("1", originalTree.includes(rootKey));
		assertTrue("2", originalTree.includes(leftKey));
		assertTrue("3", originalTree.includes(rightKey));

		assertTrue("4", originalTree.includes(leftKey.append("one")));
		assertTrue("5", originalTree.includes(leftKey.append("two")));
		assertTrue("6", originalTree.includes(leftKey.append("three")));
		assertTrue("7", originalTree.includes(rightKey.append("rightOfRight")));
	}

	/**
	 * Init tests
	 */
	protected void setUp() {

		emptyTree = new DeltaDataTree();
		tree = new DeltaDataTree();
		rootKey = Path.ROOT;

		/* Add two children to root */
		try {
			tree.createChild(rootKey, "leftOfRoot");
			tree.createChild(rootKey, "rightOfRoot");
		} catch (ObjectNotFoundException e) {
			throw new Error("(1) Error in setUp");
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
			throw new Error("(2) Error in setUp");
		}

		changedTree = new DeltaDataTree();
		changedTree.createSubtree(rootKey, tree.copyCompleteSubtree(rootKey));
	}

	/**
	 * Run all tests 
	 */

	public static Test suite() {
		TestSuite suite = new TestSuite(DeltaDataTreeTest.class.getName());
		suite.addTest(new DeltaDataTreeTest("testSetup"));

		suite.addTest(new DeltaDataTreeTest("testAddAndRemoveOnSameLayer"));
		suite.addTest(new DeltaDataTreeTest("testAddTwiceAndDelete"));
		suite.addTest(new DeltaDataTreeTest("testAssembleWithIn"));
		suite.addTest(new DeltaDataTreeTest("testCreateChild"));
		suite.addTest(new DeltaDataTreeTest("testDeleteChild"));
		suite.addTest(new DeltaDataTreeTest("testDeltaOnCompletelyDifferentTrees"));
		suite.addTest(new DeltaDataTreeTest("testEmpty"));
		suite.addTest(new DeltaDataTreeTest("testForwardDeltaOnDataDeltaNode"));
		suite.addTest(new DeltaDataTreeTest("testForwardDeltaWith"));
		suite.addTest(new DeltaDataTreeTest("testForwardDeltaWithEquality"));
		suite.addTest(new DeltaDataTreeTest("testGetChild"));
		suite.addTest(new DeltaDataTreeTest("testGetChildCount"));
		suite.addTest(new DeltaDataTreeTest("testGetChildren"));
		suite.addTest(new DeltaDataTreeTest("testGetNameOfChild"));
		suite.addTest(new DeltaDataTreeTest("testGetNamesOfChildren"));
		suite.addTest(new DeltaDataTreeTest("testIncludes"));
		suite.addTest(new DeltaDataTreeTest("testLongDeltaChain"));
		suite.addTest(new DeltaDataTreeTest("testNewEmptyDeltaTree"));
		suite.addTest(new DeltaDataTreeTest("testRegression1FVVP6L"));
		suite.addTest(new DeltaDataTreeTest("testRegression1FVVP6LWithChildren"));
		suite.addTest(new DeltaDataTreeTest("testReroot"));

		return suite;
	}

	/**
	 * Test for problem adding and deleting in same delta layer.
	 */
	public void testAddAndRemoveOnSameLayer() {
		IPath elementA = Path.ROOT.append("A");
		DeltaDataTree tree1 = new DeltaDataTree();

		tree1.createChild(Path.ROOT, "A", "Data for A");

		tree1.immutable();
		DeltaDataTree tree2;
		tree2 = tree1.newEmptyDeltaTree();

		tree2.createChild(elementA, "B", "New B Data");
		tree2.deleteChild(elementA, "B");

		tree2.immutable();

		//do a bunch of operations to ensure the tree isn't corrupt.
		tree1.compareWith(tree2, DefaultElementComparator.getComparator());
		tree2.compareWith(tree1, DefaultElementComparator.getComparator());
		tree1.forwardDeltaWith(tree2, DefaultElementComparator.getComparator());
		tree2.forwardDeltaWith(tree1, DefaultElementComparator.getComparator());
		tree1.copyCompleteSubtree(Path.ROOT);
		tree2.copyCompleteSubtree(Path.ROOT);
		tree1.reroot();
		tree2.reroot();
		tree1.makeComplete();
		tree2.makeComplete();
	}

	/**
	 * Test for problem when two complete nodes exist, and then
	 * the deleting only masks the first one.
	 */
	public void testAddTwiceAndDelete() {
		DeltaDataTree tree1 = new DeltaDataTree();

		tree1.createChild(Path.ROOT, "A", "Data for A");

		tree1.immutable();
		tree1 = tree1.newEmptyDeltaTree();

		tree1.createChild(Path.ROOT, "A", "New A Data");

		tree1.immutable();
		tree1 = tree1.newEmptyDeltaTree();

		tree1.deleteChild(Path.ROOT, "A");
		tree1.immutable();

		assertTrue(tree1.getChildCount(Path.ROOT) == 0);

	}

	public void testAssembleWithIn() {

		/**
		 * Answer the result of assembling @node1 (a node of the receiver)
		 * with @node2 (a node of @tree2).
		 * The @node2 represents a forward delta based on @node1.
		 */

		DeltaDataTree assembledTree;

		/* make a change */
		changedTree.deleteChild(leftKey, "two");

		/* make delta tree */
		deltaTree = tree.forwardDeltaWith(changedTree, DefaultElementComparator.getComparator());

		/* get changedTree from original and forward delta on original */
		assembledTree = tree.assembleWithForwardDelta(deltaTree);

		/* make sure the reconstructed tree is as expected */
		assertTrue(assembledTree.includes(rootKey));
		assertTrue(assembledTree.includes(leftKey));
		assertTrue(assembledTree.includes(leftKey.append("one")));
		assertTrue(!assembledTree.includes(leftKey.append("two")));
		assertTrue(assembledTree.includes(leftKey.append("three")));
		assertTrue(assembledTree.includes(rightKey));

	}

	/**
	 * Create a child of the specified node and give it the specified local name.<p>
	 * If a child with such a name exists, replace it with the new child
	 * @exception ObjectNotFoundException
	 *	parentKey does not exist in the receiver
	 */
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
		assertTrue((tree.getNamesOfChildren(leftKey)).length == size + 1);

		return;
	}

	/**
	 * Delete the child with the specified local name from the specified
	 * node.  Note: this method requires both parentKey and localName,
	 * making it impossible to delete the root node.<p>
	 *
	 * @exception ObjectNotFoundException
	 *	a child of parentKey with name localName does not exist in the receiver
	 */
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

		assertTrue("8", !tree.includes(leftKey));
		assertTrue("9", !tree.includes(leftKey.append("one")));
		assertTrue("10", tree.includes(rootKey));

		/* delete a leaf */
		try {
			tree.deleteChild(rightKey, "rightOfRight");
		} catch (ObjectNotFoundException e) {
		}
		assertTrue("11", !tree.includes(rightKey.append("rightOfRight")));
		assertTrue("12", tree.includes(rightKey));

		return;
	}

	/**
	 * Creates a delta on two unrelated delta trees
	 */
	public void testDeltaOnCompletelyDifferentTrees() {

		DeltaDataTree newTree = new DeltaDataTree();

		/* Create a new tree */

		/* Add two children to root */
		try {
			newTree.createChild(rootKey, "newLeft");
			newTree.createChild(rootKey, "newRight");
		} catch (ObjectNotFoundException e) {
			throw new Error("(1) Error in setUp");
		}

		/* Add three children to left of root and one to right of root */
		try {
			newTree.createChild(rootKey.append("newLeft"), "newOne");
			newTree.createChild(rootKey.append("newLeft"), "newTwo");
			newTree.createChild(rootKey.append("newLeft"), "newThree");

			newTree.createChild(rootKey.append("newRight"), "newRightOfRight");
			newTree.createChild(rootKey.append("newRight").append("newRightOfRight"), "bottom");
		} catch (ObjectNotFoundException e) {
			throw new Error("(2) Error in setUp");
		}

		/* get delta on different trees */
		deltaTree = newTree.forwardDeltaWith(tree, DefaultElementComparator.getComparator());

		/* assert delta has same content as tree */
		assertTree(deltaTree);
		assertTrue(!deltaTree.includes(rootKey.append("newLeft")));
		assertTrue(!deltaTree.includes(rootKey.append("newRight")));

	}

	/**
	 * Initialize the receiver so that it is a complete, empty tree.  It does
	 * not represent a delta on another tree.  An empty tree is defined to 
	 * have a root node with nil data and no children.
	 */
	public void testEmpty() {

		assertTrue("1", emptyTree.includes(rootKey));
		assertTrue("2", TestHelper.getRootNode(emptyTree) != null);
		assertTrue("3", TestHelper.getRootNode(emptyTree).getChildren().length == 0);

		return;
	}

	/**
	 * Tests the forwardDeltaWith function where the delta is calculated
	 * between a data delta node in the old tree and a complete data node
	 * in the new tree.
	 * This is a regression test for a problem with DataDeltaNode.forwardDeltaWith(...).
	 */
	public void testForwardDeltaOnDataDeltaNode() {

		tree.immutable();
		DeltaDataTree tree1 = tree.newEmptyDeltaTree();

		tree1.setData(leftKey, "replaced");
		DeltaDataTree delta = tree1.forwardDeltaWith(changedTree, DefaultElementComparator.getComparator());
		assertTrue(delta.getData(leftKey) == null); // the value in changedTree
	}

	/**
	 * Tests the forwardDeltaWith() function
	 */
	public void testForwardDeltaWith() {

		DeltaDataTree assembledTree;

		/* make several changes */
		changedTree.deleteChild(leftKey, "two");
		changedTree.createChild(leftKey, "four");
		changedTree.createChild(leftKey, "five");
		changedTree.createChild(leftKey, "six");
		changedTree.createChild(rootKey, "NewTopLevel");

		/* make delta tree */
		deltaTree = tree.forwardDeltaWith(changedTree, DefaultElementComparator.getComparator());

		/* get changedTree from original and forward delta on original */
		assembledTree = tree.assembleWithForwardDelta(deltaTree);

		/* make sure the reconstructed tree is as expected */
		assertTrue(assembledTree.includes(rootKey));
		assertTrue(assembledTree.includes(leftKey));
		assertTrue(assembledTree.includes(rightKey));
		assertTrue(assembledTree.includes(rootKey.append("NewTopLevel")));

		assertTrue(assembledTree.includes(leftKey.append("one")));
		assertTrue(!assembledTree.includes(leftKey.append("two")));
		assertTrue(assembledTree.includes(leftKey.append("three")));
		assertTrue(assembledTree.includes(leftKey.append("four")));
		assertTrue(assembledTree.includes(leftKey.append("five")));
		assertTrue(assembledTree.includes(leftKey.append("six")));
		assertTrue(assembledTree.includes(rightKey.append("rightOfRight")));

	}

	/**
	 * Tests the forwardDeltaWith() function using the equality comparer.
	 */
	public void testForwardDeltaWithEquality() {

		DeltaDataTree assembledTree;

		/* make several changes */
		changedTree.deleteChild(leftKey, "two");
		changedTree.createChild(leftKey, "four");
		IPath oneKey = leftKey.append("one");
		changedTree.setData(oneKey, "New");

		/* make delta tree */
		deltaTree = tree.forwardDeltaWith(changedTree, DefaultElementComparator.getComparator());

		/* get changedTree from original and forward delta on original */
		assembledTree = tree.assembleWithForwardDelta(deltaTree);

		/* make sure the reconstructed tree is as expected */
		assertTrue(assembledTree.includes(rootKey));
		assertTrue(assembledTree.includes(leftKey));
		assertTrue(assembledTree.includes(rightKey));

		assertTrue(assembledTree.includes(leftKey.append("one")));
		assertTrue(!assembledTree.includes(leftKey.append("two")));
		assertTrue(assembledTree.includes(leftKey.append("three")));
		assertTrue(assembledTree.includes(leftKey.append("four")));
		Object data = assembledTree.getData(oneKey);
		assertTrue(data != null && data.equals("New"));

	}

	/**
	 * Answer the key of the child with the given index of the
	 * specified node.<p>
	 *
	 * @param: NodeKey, int
	 * @return: NodeKey
	 *
	 * @exception ObjectNotFoundException
	 * 	parentKey does not exist in the receiver
	 * @exception ArrayIndexOutOfBoundsException
	 *	if no child with the given index (runtime exception)
	 */
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
		assertTrue(!caught);

		/* Get non-existant child of root */
		caught = false;
		try {
			tree.getChild(rootKey, 99);
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		} catch (ObjectNotFoundException e) {
			throw new Error();
		}
		assertTrue(caught);

		/* Get non-existant child of interior node */
		caught = false;
		try {
			tree.getChild(leftKey, 99);
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		} catch (ObjectNotFoundException e) {
			throw new Error();
		}
		assertTrue(caught);

		/* Get non-existant child of leaf node */
		caught = false;
		try {
			tree.getChild(leftKey.append("one"), 99);
		} catch (ArrayIndexOutOfBoundsException e) {
			caught = true;
		} catch (ObjectNotFoundException e) {
			throw new Error();
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

		return;
	}

	/**
	 * Answer  the number of children of the specified node.
	 *
	 * @exception ObjectNotFoundException 
	 *	parentKey does not exist in the receiver
	 */
	public void testGetChildCount() {

		boolean caught;

		caught = false;
		try {
			/* empty tree */
			assertTrue("1", emptyTree.getChildCount(rootKey) == 0);

			/* root node */
			assertTrue("2", tree.getChildCount(rootKey) == 2);

			/* interior nodes */
			assertTrue("3", tree.getChildCount(leftKey) == 3);
			assertTrue("4", tree.getChildCount(rightKey) == 1);

			/* leaf nodes */
			assertTrue("5", tree.getChildCount(leftKey.append("one")) == 0);
			assertTrue("6", tree.getChildCount(leftKey.append("three")) == 0);
			assertTrue("7", tree.getChildCount(rightKey.append("rightOfRight")) == 0);
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertTrue(!caught);
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

		return;
	}

	/**
	 * Answer the keys for the children of the specified node.
	 *
	 * @exception ObjectNotFoundException
	 *	parentKey does not exist in the receiver"
	 */
	public void testGetChildren() {

		boolean caught;
		IPath testChildren[], rootChildren[] = {leftKey, rightKey}, leftChildren[] = {leftKey.append("one"), leftKey.append("two"), leftKey.append("three")}, rightChildren[] = {rightKey.append("rightOfRight")};

		caught = false;
		try {
			/* empty tree */
			testChildren = emptyTree.getChildren(rootKey);
			assertTrue("1", testChildren.length == 0);

			/* root node */
			testChildren = tree.getChildren(rootKey);
			assertTrue("2", testChildren.length == 2);
			assertTrue("3", testChildren[0].equals(rootChildren[0]));
			assertTrue("4", testChildren[1].equals(rootChildren[1]));

			/* interior nodes */
			testChildren = tree.getChildren(leftKey);
			assertTrue("5", testChildren.length == 3);
			assertTrue("6", testChildren[0].equals(leftChildren[0]));
			assertTrue("7", testChildren[2].equals(leftChildren[1]));
			assertTrue("8", testChildren[1].equals(leftChildren[2]));

			/* leaf nodes */
			testChildren = tree.getChildren(leftChildren[0]);
			assertTrue("9", testChildren.length == 0);

			testChildren = tree.getChildren(rightChildren[0]);
			assertTrue("10", testChildren.length == 0);
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertTrue("11", !caught);
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

		return;
	}

	/**
	 * Answer the local name of the child with the given index of the
	 * specified node.
	 * @exception ObjectNotFoundException
	 *	parentKey does not exist in the receiver
	 * @exception ArrayIndexOutOfBoundsException
	 *	if no child with the given index
	 */
	public void testGetNameOfChild() {

		/* tested thoroughly in testGetChild() and testGetNamesOfChildren */

		return;
	}

	/**
	 * Returns the local names for the children of the specified node.
	 *
	 * @exception ObjectNotFoundException
	 *	parentKey does not exist in the receiver
	 */
	public void testGetNamesOfChildren() {

		boolean caught;
		String testChildren[], rootChildren[] = {"leftOfRoot", "rightOfRoot"}, leftChildren[] = {"one", "two", "three"}, rightChildren[] = {"rightOfRight"};

		caught = false;
		try {
			/* empty tree */
			testChildren = emptyTree.getNamesOfChildren(rootKey);
			assertTrue("1", testChildren.length == 0);

			/* root node */
			testChildren = tree.getNamesOfChildren(rootKey);
			assertTrue("2", testChildren.length == 2);
			assertTrue("3", testChildren[0].equals(rootChildren[0]));
			assertTrue("4", testChildren[1].equals(rootChildren[1]));

			/* interior nodes */
			testChildren = tree.getNamesOfChildren(leftKey);
			assertTrue("5", testChildren.length == 3);
			assertTrue("6", testChildren[0].equals(leftChildren[0]));
			assertTrue("7", testChildren[2].equals(leftChildren[1]));
			assertTrue("8", testChildren[1].equals(leftChildren[2]));

			testChildren = tree.getNamesOfChildren(rightKey);
			assertTrue("8.1", testChildren.length == 1);
			assertTrue("8.2", testChildren[0].equals(rightChildren[0]));

			/* leaf nodes */
			testChildren = tree.getNamesOfChildren(leftKey.append("one"));
			assertTrue("9", testChildren.length == 0);

			testChildren = tree.getNamesOfChildren(rightKey.append("rightOfRight"));
			assertTrue("10", testChildren.length == 0);
		} catch (ObjectNotFoundException e) {
			caught = true;
		} finally {
			assertTrue("11", !caught);
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

		return;
	}

	/**
	 * Returns true if the receiver includes a node with the given key, false
	 * otherwise.
	 */
	public void testIncludes() {

		/* tested in testCreateChild() and testDeleteChild() */

		assertTrue(emptyTree.includes(rootKey));
		assertTrue(tree.includes(rootKey));
		assertTrue(tree.includes(leftKey));
		assertTrue(tree.includes(rightKey));
		assertTrue(tree.includes(leftKey.append("one")));
		assertTrue(tree.includes(rightKey.append("rightOfRight")));

		assertTrue(!emptyTree.includes(rootKey.append("bogus")));
		assertTrue(!tree.includes(rootKey.append("bogus")));
		assertTrue(!tree.includes(leftKey.append("bogus")));
		assertTrue(!tree.includes(leftKey.append("one").append("bogus")));
		assertTrue(!tree.includes(rightKey.append("bogus")));

		return;
	}

	/**
	 * Tests operations on a chain of deltas
	 */
	public void testLongDeltaChain() {

		final int NUM_DELTAS = 10;

		DeltaDataTree deltas[] = new DeltaDataTree[NUM_DELTAS];

		/* create a delta on the original tree, and make a change */
		tree.immutable();
		deltas[0] = tree.newEmptyDeltaTree();
		deltas[0].createChild(leftKey, "new");
		assertTree(deltas[0]);
		assertTrue(deltas[0].includes(leftKey.append("new")));

		/* create a second delta and make a change to that */
		deltas[0].immutable();
		deltas[1] = deltas[0].newEmptyDeltaTree();
		deltas[1].deleteChild(leftKey, "one");
		assertEquals("parent 0 -> 1", deltas[1].getParent(), deltas[0]);
		assertTrue(!deltas[1].includes(leftKey.append("one")));

		/* create a third delta and make a change to that */
		deltas[1].immutable();
		deltas[2] = deltas[1].newEmptyDeltaTree();
		deltas[2].createChild(rootKey, "newTopLevel");
		assertEquals("parent 1 -> 2", deltas[2].getParent(), deltas[1]);
		assertEquals("parent 0 -> 2", deltas[2].getParent().getParent(), deltas[0]);
		assertTrue(!deltas[2].includes(leftKey.append("one")));
		assertTrue(deltas[2].includes(rootKey.append("newTopLevel")));

	}

	/**
	 * Tests the newEmptyDeltaTree method
	 */
	public void testNewEmptyDeltaTree() {

		tree.immutable();
		DeltaDataTree delta = tree.newEmptyDeltaTree();
		assertEquals("parent", tree, delta.getParent());
		assertTree(delta);

	}

	/**
	 * Test for problem deleting and re-adding in same delta layer.
	 */
	public void testRegression1FVVP6L() {
		IPath elementA = Path.ROOT.append("A");

		DeltaDataTree tree1 = new DeltaDataTree();

		tree1.createChild(Path.ROOT, "A", "Data for A");
		tree1.createChild(elementA, "B", "Data for B");

		tree1.immutable();
		tree1 = tree1.newEmptyDeltaTree();

		tree1.deleteChild(elementA, "B");
		tree1.createChild(elementA, "B", "New B Data");

		tree1.immutable();
		tree1 = tree1.newEmptyDeltaTree();

		tree1.deleteChild(elementA, "B");

		try {
			tree1.copyCompleteSubtree(Path.ROOT);
		} catch (RuntimeException e) {
			assertTrue("Unexpected error copying tree", false);
		}

	}

	/**
	 * Test for problem deleting and re-adding in same delta layer.
	 */
	public void testRegression1FVVP6LWithChildren() {
		IPath elementA = Path.ROOT.append("A");
		IPath elementB = elementA.append("B");
		IPath elementC = elementB.append("C");

		DeltaDataTree tree1 = new DeltaDataTree();

		tree1.createChild(Path.ROOT, "A", "Data for A");
		tree1.createChild(elementA, "B", "Data for B");
		tree1.createChild(elementB, "C", "Data for C");

		tree1.immutable();
		tree1 = tree1.newEmptyDeltaTree();

		tree1.deleteChild(elementA, "B");
		tree1.createChild(elementA, "B", "New B Data");

		tree1.immutable();
		tree1 = tree1.newEmptyDeltaTree();

		assertTrue("Child exists after deletion", !tree1.includes(elementC));

		try {
			tree1.copyCompleteSubtree(Path.ROOT);
		} catch (RuntimeException e) {
			assertTrue("Unexpected error copying tree", false);
		}

	}

	/**
	 * Tests the reroot function
	 */
	public void testReroot() {

		final int NUM_DELTAS = 10;

		DeltaDataTree deltas[] = new DeltaDataTree[NUM_DELTAS];

		/* create a delta on the original tree, and make a change */
		tree.immutable();
		deltas[0] = tree.newEmptyDeltaTree();
		deltas[0].createChild(leftKey, "new");
		assertTree(deltas[0]);
		assertTrue(deltas[0].includes(leftKey.append("new")));

		/* create a second delta and make a change to that */
		deltas[0].immutable();
		deltas[1] = deltas[0].newEmptyDeltaTree();
		deltas[1].deleteChild(leftKey, "one");
		assertEquals("parent 0 -> 1", deltas[1].getParent(), deltas[0]);
		assertTrue(!deltas[1].includes(leftKey.append("one")));

		/* create a third delta and make a change to that */
		deltas[1].immutable();
		deltas[2] = deltas[1].newEmptyDeltaTree();
		deltas[2].createChild(rootKey, "newTopLevel");
		assertEquals("parent 1 -> 2", deltas[2].getParent(), deltas[1]);
		assertEquals("parent 0 -> 2", deltas[2].getParent().getParent(), deltas[0]);
		assertTrue(!deltas[2].includes(leftKey.append("one")));
		assertTrue(deltas[2].includes(rootKey.append("newTopLevel")));

		/* create a fourth delta and reroot at it */
		deltas[2].immutable();
		deltas[3] = deltas[2].newEmptyDeltaTree();
		deltas[3].immutable();
		deltas[3].reroot();
		assertTrue(deltas[3].getParent() == null);
		assertTrue(deltas[2].getParent() == deltas[3]);
		assertTrue(deltas[1].getParent() == deltas[2]);
		assertTrue(deltas[0].getParent() == deltas[1]);

		/* test that all trees have the same representation as before rerooting */
		assertTree(tree);
		assertTrue(!tree.includes(leftKey.append("new")));
		assertTrue(tree.includes(leftKey.append("one")));
		assertTree(deltas[0]);
		assertTrue(deltas[0].includes(leftKey.append("new")));
		assertTrue(deltas[0].includes(leftKey.append("one")));
		assertTrue(deltas[1].includes(leftKey.append("new")));
		assertTrue(!deltas[1].includes(leftKey.append("one")));
		assertDelta(deltas[2]);
		assertDelta(deltas[3]);

	}

	/**
	 * Tests that the setUp() method is doing what it should
	 */
	public void testSetup() {

		assertTree(tree);
		assertTree(changedTree);

		return;
	}
}
