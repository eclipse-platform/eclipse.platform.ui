/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.watson;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Unit tests for <code>ElementTree</code>.
 */
public class ElementTreeTest extends WatsonTest implements IPathConstants {
	protected ElementTree fTree;
	protected ElementTree fEmptyTree = new ElementTree();
public ElementTreeTest() {
	super(null);
}
public ElementTreeTest(String name) {
	super(name);
}
/**
 * Creates a chain of deltas such that the final tree has the same
 * content as the base tree.  Returns the final tree.
 */
protected ElementTree buildDeltaChain(ElementTree baseTree) {
	/* do a bunch of operations to build a chain of deltas */
	ElementTree tree1 = baseTree.newEmptyDelta();

	IPath project3 = solution.append("project3");
	tree1.createElement(project3, null);
	tree1.immutable();
	assertTrue(tree1.hasAncestor(baseTree));

	ElementTree tree2 = tree1.newEmptyDelta();
	tree2.setElementData(project3, "Hello");
	tree2.immutable();
	assertTrue(tree2.hasAncestor(baseTree));

	ElementTree tree3 = tree2.newEmptyDelta();
	tree3.setElementData(project3, null);
	tree3.immutable();
	assertTrue(tree3.hasAncestor(baseTree));

	ElementTree tree4 = tree3.newEmptyDelta();
	tree4.deleteElement(project3);

	/* tree4 should be same as original tree */
	TestUtil.assertTreeStructure(tree4);

	IPath file33 = folder2.append("file33");
	tree4.createElement(file33, null);
	tree4.immutable();
	assertTrue(tree4.hasAncestor(baseTree));
	
	ElementTree tree5 = tree4.newEmptyDelta();
	tree5.setElementData(file33, "Hello");
	tree5.immutable();
	assertTrue(tree5.hasAncestor(baseTree));
	
	ElementTree tree6 = tree5.newEmptyDelta();
	tree6.setElementData(file33, null);
	tree6.immutable();
	assertTrue(tree6.hasAncestor(baseTree));
	
	ElementTree tree7 = tree6.newEmptyDelta();
	tree7.deleteElement(file33);
	tree7.immutable();
	assertTrue(tree7.hasAncestor(baseTree));
	
	/* final tree should be same as original tree */
	TestUtil.assertTreeStructure(tree7);

	return tree7;
}
/**
 * Tests the getSubtree and createSubtree API calls.
 */
public void copySubtree(ElementTree oldTree, ElementTree newTree, IPath key) {
	try {
		newTree.createSubtree(key, oldTree.getSubtree(key));
	} catch (IllegalArgumentException e) {
		assertTrue(key.equals(Path.ROOT));
	}

	/* recurse on children (redundant, but we're testing) */
	IPath[] children = oldTree.getChildren(key);
	for (int i = 0; i < children.length; i++) {
		copySubtree(oldTree, newTree, children[i]);
	}
}
protected void setUp() throws Exception {
	fTree = TestUtil.createTestElementTree();
}
public static Test suite() { 
	TestSuite suite= new TestSuite(ElementTreeTest.class);
//	suite.addTest(new ElementTreeTest("testRegression1FVVP6L"));
	return suite;
}
/**
 * 
 */
protected void tearDown() throws Exception {
	//ElementTree tests don't use the CoreTest infrastructure
}
public void testBottomUpCreation() {

	/* Create a root with no children. */
	ElementTree tree = new ElementTree();
	IPath rootID = (IPath)Path.ROOT.append("sol1");
	tree.createElement(rootID, "ROOTS");

	/* Create two grandchildren */
	ElementTree f1 = new ElementTree("TestFile1", "INFO 1");
	ElementTree f2 = new ElementTree("TestFile2", "INFO 2");
	ElementTree[] filez = {f1, f2};

	/* Create a child type containing the grandchildren. */
	ElementTree subtree = new ElementTree("TestProject", "INFO 3", filez);

	IPath projectID= rootID.append("TestProject");
	IPath file1ID = projectID.append("TestFile1");
	IPath file2ID = projectID.append("TestFile2");
	
	/* Add the child to the root. */
	tree.createSubtree(projectID, subtree);
	TestUtil.assertHasPaths(tree, new IPath[] {projectID, file1ID, file2ID});
}
public void testChildCreation() {
	/* basic child creation is tested during setup... */
	TestUtil.assertTreeStructure(fTree);

	/* add to empty tree */
	IPath key = Path.ROOT.append("only");	assertTrue("not includes", !fEmptyTree.includes(key));
	fEmptyTree.createElement(key, new Integer(4));
	assertTrue("includes", fEmptyTree.includes(key));

	/* add child of non-existent parent */
	key = Path.ROOT.append("bogus").append("figment");
	assertTrue("not1", !fEmptyTree.includes(key));
	boolean caught = false;
	try {
		fEmptyTree.createElement(key, null);
	} catch (IllegalArgumentException e) {
		caught = true;
	} finally {
		assertTrue("caught1", caught);
	}

	/* test creation on an immutable tree */
	key = Path.ROOT.append("only");
	fTree.immutable();

	caught = false;
	try {
		fTree.createElement(key, new Integer(4));
	} catch (RuntimeException e) {
		caught = true;
	} finally {
		assertTrue("caught2", caught);
	}
	assertTrue("not includes2", !fTree.includes(key));
	
	/* test creation on delta */
	ElementTree delta = fTree.newEmptyDelta();
	assertTrue("not includes3", !delta.includes(key));
	delta.createElement(key, new Integer(4));
	assertTrue("includes2", delta.includes(key));
	assertTrue("not includes4", !fTree.includes(key));
	
}
public void testChildDeletion() {

	/* Delete the tree bottom-up */
	IPath[] paths = TestUtil.getTreePaths();
	int numPaths = paths.length;
	
	for (int i = numPaths; --i >=0;) {
		assertTrue("includes"+i, fTree.includes(paths[i]));
		fTree.deleteElement(paths[i]);
		assertTrue("not includes"+i, !fTree.includes(paths[i]));
	}

	/* rebuild the tree */
	fTree = TestUtil.createTestElementTree();

	/* delete the tree top-down */
	fTree.deleteElement(solution)	;

	/* make sure there's nothing left */
	for (int i = 0; i < numPaths; i++) {
		assertTrue("top-down", !fTree.includes(paths[i]));
	}

	/* rebuild the fTree */
	fTree= TestUtil.createTestElementTree();
	
	/* delete a folder with children */
	assertTrue("f1", fTree.includes(folder1));
	fTree.deleteElement(folder1);
	assertTrue("!f1", !fTree.includes(folder1));
	assertTrue("!f3", !fTree.includes(folder3));
	assertTrue("!file3", !fTree.includes(file3));

}
public void testChildReplacement() {

	/* "create" an existing element */
 	Object newInfo = "New Info";
	fTree.createElement(folder1, newInfo);

	/* make sure there isn't an extra child */
	assertEquals("1", 3, fTree.getChildCount(project2));

	/* child's info has been replaced */
	assertEquals("2", newInfo, fTree.getElementData(folder1));

	/* make sure grandchildren are gone */
	TestUtil.assertNoPaths(fTree, new IPath[] {file2, folder3, folder4});

	/* do it all again with null info */
 	newInfo = null;
	fTree.createElement(folder1, newInfo);

	assertEquals("1", 3, fTree.getChildCount(project2));
	assertEquals("2", null, fTree.getElementData(folder1));
	TestUtil.assertNoPaths(fTree, new IPath[] {file2, folder3, folder4});

	/* one more time going from null info to real info */
 	newInfo = "New Info";
	fTree.createElement(folder1, newInfo);

	assertEquals("1", 3, fTree.getChildCount(project2));
	assertEquals("2", newInfo, fTree.getElementData(folder1));
	TestUtil.assertNoPaths(fTree, new IPath[] {file2, folder3, folder4});
}
/**
 * Tests the collapsing API method
 */
public void testCollapsing() {
	if (true)
		return;
	
	///* do a bunch of operations to build a chain of deltas */
	//ElementTree deepTree = buildDeltaChain(fTree);

	///* collapse the deep tree one chain at a time */
	//while (deepTree.deltaDepth() > 0) {
		//assert(deepTree.hasAncestor(fTree));
		//deepTree = deepTree.collapsing(1);
	//}
	//TestUtil.assertTreeStructure(deepTree);

	///* build another delta chain */
	//ElementTree deeperTree = buildDeltaChain(deepTree);

	///* collapse by twos */
	//while (deeperTree.deltaDepth() > 0) {
		//assert(deeperTree.hasAncestor(deepTree));
		//if (deeperTree.deltaDepth() > 1) {
			//deeperTree = deeperTree.collapsing(2);
		//} else {
			//deeperTree = deeperTree.collapsing(1);
		//}
	//}

	//TestUtil.assertTreeStructure(deeperTree);

	///* build another delta chain */
	//ElementTree deepestTree = buildDeltaChain(deeperTree);

	///* collapse all but one */
	//deepestTree = deepestTree.collapsing(deepestTree.deltaDepth()-1);
	//assert(deepestTree.hasAncestor(deeperTree));

	///* collapse into a single tree with no ancestors */
	//deepestTree = buildDeltaChain(deeperTree);
	//deepestTree = deepestTree.collapsing(deepestTree.deltaDepth());

	//TestUtil.assertTreeStructure(deepestTree);
}
public void testComputeDeltaWith() {
	/**
	 * The following changes will be made to the base tree:
	 *	- add project 3
	 *  - delete file1
	 *  - change data of folder2
	 *	- add file4 below project2
	 *  - add file5 below folder1
	 *  - delete file3
	 */

	ElementTree newTree = fTree.newEmptyDelta();
	IDeltaFilter filter = TestUtil.getFilter();

	IPath project3 = solution.append("project3");
	IPath file4 = project2.append("file4");
	IPath file5 = folder1.append("file5");
	 
	newTree.createElement(project3, "project3");
	newTree.deleteElement(file1);
	newTree.createElement(folder2, "ChangedData");
	newTree.createElement(file4, "file4");
	newTree.createElement(file5, "file5");
	newTree.deleteElement(file3);

	/* assert the new structure */
	TestUtil.assertHasPaths(newTree, new IPath[] {solution, project1, project2, project3,
		file2, file4, file5, folder1, folder2, folder3, folder4});
	TestUtil.assertNoPaths(newTree, new IPath[] {file1, file3});

	/* compute the delta */
	ElementTreeDelta delta = fTree.computeDeltaWith(newTree, TestUtil.getComparator());

	/* try asking the delta for bogus elements */
	assertTrue(delta.getElementDelta(Path.ROOT.append("bogosity")) == null);
	assertTrue(delta.getElementDelta(project2.append("bogosity")) == null);
	
	/* assert the delta structure (changes to get from newTree -> fTree) */
	ElementDelta root = delta.getElementDelta(Path.ROOT);
	ElementDelta[] children = root.getAffectedChildren(filter);
	assertTrue(children.length == 1);

	children = children[0].getAffectedChildren(filter);
	assertTrue(children.length == 2);

	assertTrue(children[0].getPath().equals(project2));
	TestUtil.assertNoChange(children[0].getComparison());
	assertTrue(children[1].getPath().equals(project3));
	TestUtil.assertRemoved(children[1].getComparison());
	assertTrue(children[1].getAffectedChildren(filter).length == 0);

	/* children of project2 */
	children = children[0].getAffectedChildren(filter);
	assertTrue(children.length == 4);

	assertTrue(children[0].getPath().equals(file1));
	TestUtil.assertAdded(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(file4));
	TestUtil.assertRemoved(children[1].getComparison());
	assertTrue(children[1].getAffectedChildren(filter).length == 0);
	assertTrue(children[2].getPath().equals(folder1));
	TestUtil.assertNoChange(children[2].getComparison());
	assertTrue(children[3].getPath().equals(folder2));
	TestUtil.assertChanged(children[3].getComparison());
	assertTrue(children[3].getAffectedChildren(filter).length == 0);

	/* children of folder1 */
	children = children[2].getAffectedChildren(filter);
	assertTrue(children.length == 2);

	assertTrue(children[0].getPath().equals(file5));
	TestUtil.assertRemoved(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(folder3));
	TestUtil.assertNoChange(children[1].getComparison());

	/* children of folder3 */
	children = children[1].getAffectedChildren(filter);
	assertTrue(children.length == 1);

	assertTrue(children[0].getPath().equals(file3));
	TestUtil.assertAdded(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
}
/**
 * Same as testComputeDeltaWith, except this time we look at the
 * reverse delta.
 */
public void testComputeDeltaWith2() {
	/**
	 * The following changes will be made to the base tree:
	 *	- add project 3
	 *  - delete file1
	 *  - change data of folder2
	 *	- add file4 below project2
	 *  - add file5 below folder1
	 *  - delete file3
	 */

	ElementTree newTree = fTree.newEmptyDelta();
	IDeltaFilter filter = TestUtil.getFilter();

	IPath project3 = solution.append("project3");
	IPath file4 = project2.append("file4");
	IPath file5 = folder1.append("file5");
	 
	newTree.createElement(project3, "project3");
	newTree.deleteElement(file1);
	newTree.createElement(folder2, "ChangedData");
	newTree.createElement(file4, "file4");
	newTree.createElement(file5, "file5");
	newTree.deleteElement(file3);

	/* assert the new structure */
	TestUtil.assertHasPaths(newTree, new IPath[] {solution, project1, project2, project3,
		file2, file4, file5, folder1, folder2, folder3, folder4});
	TestUtil.assertNoPaths(newTree, new IPath[] {file1, file3});

	/* compute the delta */
	ElementTreeDelta delta = newTree.computeDeltaWith(fTree, TestUtil.getComparator());

	/* assert the delta structure (changes to get from fTree -> newTree) */
	ElementDelta root = delta.getElementDelta(Path.ROOT);
	ElementDelta[] children = root.getAffectedChildren(filter);
	assertTrue(children.length == 1);

	children = children[0].getAffectedChildren(filter);
	assertTrue(children.length == 2);

	assertTrue(children[0].getPath().equals(project2));
	TestUtil.assertNoChange(children[0].getComparison());
	assertTrue(children[1].getPath().equals(project3));
	TestUtil.assertAdded(children[1].getComparison());
	assertTrue(children[1].getAffectedChildren(filter).length == 0);

	/* children of project2 */
	children = children[0].getAffectedChildren(filter);
	assertTrue(children.length == 4);

	assertTrue(children[0].getPath().equals(file1));
	TestUtil.assertRemoved(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(file4));
	TestUtil.assertAdded(children[1].getComparison());
	assertTrue(children[1].getAffectedChildren(filter).length == 0);
	assertTrue(children[2].getPath().equals(folder1));
	TestUtil.assertNoChange(children[2].getComparison());
	assertTrue(children[3].getPath().equals(folder2));
	TestUtil.assertChanged(children[3].getComparison());
	assertTrue(children[3].getAffectedChildren(filter).length == 0);

	/* children of folder1 */
	children = children[2].getAffectedChildren(filter);
	assertTrue(children.length == 2);

	assertTrue(children[0].getPath().equals(file5));
	TestUtil.assertAdded(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(folder3));
	TestUtil.assertNoChange(children[1].getComparison());

	/* children of folder3 */
	children = children[1].getAffectedChildren(filter);
	assertTrue(children.length == 1);

	assertTrue(children[0].getPath().equals(file3));
	TestUtil.assertRemoved(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
}
/**
 * Same as testComputeDeltaWith, except this time we perform
 * the delta on an unrelated tree.
 */
public void testComputeDeltaWith3() {
	/**
	 * The following changes will be made to the base tree:
	 *	- add project 3
	 *  - delete file1
	 *  - change data of folder2
	 *	- add file4 below project2
	 *  - add file5 below folder1
	 *  - delete file3
	 */

	ElementTree newTree = fTree.newEmptyDelta();
	ElementTree copyTree = TestUtil.createTestElementTree();
	IDeltaFilter filter = TestUtil.getFilter();

	IPath project3 = solution.append("project3");
	IPath file4 = project2.append("file4");
	IPath file5 = folder1.append("file5");
	 
	newTree.createElement(project3, "project3");
	newTree.deleteElement(file1);
	newTree.createElement(folder2, "ChangedData");
	newTree.createElement(file4, "file4");
	newTree.createElement(file5, "file5");
	newTree.deleteElement(file3);

	/* assert the new structure */
	TestUtil.assertHasPaths(newTree, new IPath[] {solution, project1, project2, project3,
		file2, file4, file5, folder1, folder2, folder3, folder4});
	TestUtil.assertNoPaths(newTree, new IPath[] {file1, file3});

	/* compute the delta */
	ElementTreeDelta delta = newTree.computeDeltaWith(copyTree, TestUtil.getComparator());

	/* assert the delta structure (changes to get from fTree -> newTree) */
	ElementDelta root = delta.getElementDelta(Path.ROOT);
	ElementDelta[] children = root.getAffectedChildren(filter);
	assertTrue(children.length == 1);

	children = children[0].getAffectedChildren(filter);
	assertTrue(children.length == 2);

	assertTrue(children[0].getPath().equals(project2));
	TestUtil.assertNoChange(children[0].getComparison());
	assertTrue(children[1].getPath().equals(project3));
	TestUtil.assertAdded(children[1].getComparison());
	assertTrue(children[1].getAffectedChildren(filter).length == 0);

	/* children of project2 */
	children = children[0].getAffectedChildren(filter);
	assertTrue(children.length == 4);

	assertTrue(children[0].getPath().equals(file1));
	TestUtil.assertRemoved(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(file4));
	TestUtil.assertAdded(children[1].getComparison());
	assertTrue(children[1].getAffectedChildren(filter).length == 0);
	assertTrue(children[2].getPath().equals(folder1));
	TestUtil.assertNoChange(children[2].getComparison());
	assertTrue(children[3].getPath().equals(folder2));
	TestUtil.assertChanged(children[3].getComparison());
	assertTrue(children[3].getAffectedChildren(filter).length == 0);

	/* children of folder1 */
	children = children[2].getAffectedChildren(filter);
	assertTrue(children.length == 2);

	assertTrue(children[0].getPath().equals(file5));
	TestUtil.assertAdded(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(folder3));
	TestUtil.assertNoChange(children[1].getComparison());

	/* children of folder3 */
	children = children[1].getAffectedChildren(filter);
	assertTrue(children.length == 1);

	assertTrue(children[0].getPath().equals(file3));
	TestUtil.assertRemoved(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
}
/**
 * Tests the computeDeltaWith(ElementTree, IElementComparator, IPath) method, where
 * the given path is a leaf node.
 */
public void testComputeLeafDelta() {
	/**
	 * The following changes will be made to the base tree:
	 *	- add project 3
	 *  - delete file1
	 *  - change data of folder2
	 *	- add file4 below project2
	 *  - add file5 below folder1
	 *  - delete file3
	 */

	ElementTree newTree = fTree.newEmptyDelta();

	IPath project3 = solution.append("project3");
	IPath file4 = project2.append("file4");
	IPath file5 = folder1.append("file5");
	 
	newTree.createElement(project3, "project3");
	newTree.deleteElement(file1);
	newTree.createElement(folder2, "ChangedData");
	newTree.createElement(file4, "file4");
	newTree.createElement(file5, "file5");
	newTree.deleteElement(file3);

	/* assert the new structure */
	TestUtil.assertHasPaths(newTree, new IPath[] {solution, project1, project2, project3,
		file2, file4, file5, folder1, folder2, folder3, folder4});
	TestUtil.assertNoPaths(newTree, new IPath[] {file1, file3});

	/* compute the delta */
	ElementTreeDelta delta = fTree.computeDeltaWith(newTree, TestUtil.getComparator(), file3);

	/* try asking the delta for bogus elements */
	assertTrue(delta.getElementDelta(Path.ROOT.append("bogosity")) == null);
	assertTrue(delta.getElementDelta(project2.append("bogosity")) == null);
	
	/* assert the delta structure (changes to get from newTree -> fTree) */
	ElementDelta root = delta.getElementDelta(Path.ROOT);
	assertTrue(root.getPath().equals(file3));
	TestUtil.assertAdded(root.getComparison());
	assertTrue(root.getAffectedChildren(TestUtil.getFilter()).length == 0);
}
/**
 * Tests the computeDeltaWith(ElementTree, IElementComparator, IPath) method, where
 * the given path is project2.
 */
public void testComputeProjectDelta() {
	/**
	 * The following changes will be made to the base tree:
	 *	- add project 3
	 *  - delete file1
	 *  - change data of folder2
	 *	- add file4 below project2
	 *  - add file5 below folder1
	 *  - delete file3
	 */

	ElementTree newTree = fTree.newEmptyDelta();
	IDeltaFilter filter = TestUtil.getFilter();

	IPath project3 = solution.append("project3");
	IPath file4 = project2.append("file4");
	IPath file5 = folder1.append("file5");
	 
	newTree.createElement(project3, "project3");
	newTree.deleteElement(file1);
	newTree.createElement(folder2, "ChangedData");
	newTree.createElement(file4, "file4");
	newTree.createElement(file5, "file5");
	newTree.deleteElement(file3);

	/* assert the new structure */
	TestUtil.assertHasPaths(newTree, new IPath[] {solution, project1, project2, project3,
		file2, file4, file5, folder1, folder2, folder3, folder4});
	TestUtil.assertNoPaths(newTree, new IPath[] {file1, file3});

	/* compute the delta */
	ElementTreeDelta delta = fTree.computeDeltaWith(newTree, TestUtil.getComparator(), project2);

	/* try asking the delta for bogus elements */
	assertTrue(delta.getElementDelta(Path.ROOT.append("bogosity")) == null);
	assertTrue(delta.getElementDelta(project2.append("bogosity")) == null);
	
	/* assert the delta structure (changes to get from newTree -> fTree) */

	/* the root node should be the project node */
	ElementDelta root = delta.getElementDelta(Path.ROOT);
	TestUtil.assertNoChange(root.getComparison());
	assertTrue(root.getPath().equals(project2));

	/* children of project2 */
	ElementDelta[] children = root.getAffectedChildren(filter);
	assertTrue(children.length == 4);

	assertTrue(children[0].getPath().equals(file1));
	TestUtil.assertAdded(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(file4));
	TestUtil.assertRemoved(children[1].getComparison());
	assertTrue(children[1].getAffectedChildren(filter).length == 0);
	assertTrue(children[2].getPath().equals(folder1));
	TestUtil.assertNoChange(children[2].getComparison());
	assertTrue(children[3].getPath().equals(folder2));
	TestUtil.assertChanged(children[3].getComparison());
	assertTrue(children[3].getAffectedChildren(filter).length == 0);

	/* children of folder1 */
	children = children[2].getAffectedChildren(filter);
	assertTrue(children.length == 2);

	assertTrue(children[0].getPath().equals(file5));
	TestUtil.assertRemoved(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(folder3));
	TestUtil.assertNoChange(children[1].getComparison());

	/* children of folder3 */
	children = children[1].getAffectedChildren(filter);
	assertTrue(children.length == 1);

	assertTrue(children[0].getPath().equals(file3));
	TestUtil.assertAdded(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
}
/**
 * Tests the computeDeltaWith(ElementTree, IElementComparator, IPath) method, where
 * the given path is a solution path
 */
public void testComputeSolutionDelta() {
	/**
	 * The following changes will be made to the base tree:
	 *	- add project 3
	 *  - delete file1
	 *  - change data of folder2
	 *	- add file4 below project2
	 *  - add file5 below folder1
	 *  - delete file3
	 */

	ElementTree newTree = fTree.newEmptyDelta();
	IDeltaFilter filter = TestUtil.getFilter();

	IPath project3 = solution.append("project3");
	IPath file4 = project2.append("file4");
	IPath file5 = folder1.append("file5");
	 
	newTree.createElement(project3, "project3");
	newTree.deleteElement(file1);
	newTree.createElement(folder2, "ChangedData");
	newTree.createElement(file4, "file4");
	newTree.createElement(file5, "file5");
	newTree.deleteElement(file3);

	/* assert the new structure */
	TestUtil.assertHasPaths(newTree, new IPath[] {solution, project1, project2, project3,
		file2, file4, file5, folder1, folder2, folder3, folder4});
	TestUtil.assertNoPaths(newTree, new IPath[] {file1, file3});

	/* compute the delta */
	ElementTreeDelta delta = fTree.computeDeltaWith(newTree, TestUtil.getComparator(), solution);

	/* try asking the delta for bogus elements */
	assertTrue(delta.getElementDelta(Path.ROOT.append("bogosity")) == null);
	assertTrue(delta.getElementDelta(project2.append("bogosity")) == null);
	
	/* assert the delta structure (changes to get from newTree -> fTree) */

	/* the root node should be the solution node */
	ElementDelta root = delta.getElementDelta(Path.ROOT);
	assertEquals(root.getPath(), solution);
	TestUtil.assertNoChange(root.getComparison());
	ElementDelta[] children = root.getAffectedChildren(filter);
	assertTrue(children.length == 2);

	assertTrue(children[0].getPath().equals(project2));
	TestUtil.assertNoChange(children[0].getComparison());
	assertTrue(children[1].getPath().equals(project3));
	TestUtil.assertRemoved(children[1].getComparison());
	assertTrue(children[1].getAffectedChildren(filter).length == 0);

	/* children of project2 */
	children = children[0].getAffectedChildren(filter);
	assertTrue(children.length == 4);

	assertTrue(children[0].getPath().equals(file1));
	TestUtil.assertAdded(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(file4));
	TestUtil.assertRemoved(children[1].getComparison());
	assertTrue(children[1].getAffectedChildren(filter).length == 0);
	assertTrue(children[2].getPath().equals(folder1));
	TestUtil.assertNoChange(children[2].getComparison());
	assertTrue(children[3].getPath().equals(folder2));
	TestUtil.assertChanged(children[3].getComparison());
	assertTrue(children[3].getAffectedChildren(filter).length == 0);

	/* children of folder1 */
	children = children[2].getAffectedChildren(filter);
	assertTrue(children.length == 2);

	assertTrue(children[0].getPath().equals(file5));
	TestUtil.assertRemoved(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
	assertTrue(children[1].getPath().equals(folder3));
	TestUtil.assertNoChange(children[1].getComparison());

	/* children of folder3 */
	children = children[1].getAffectedChildren(filter);
	assertTrue(children.length == 1);

	assertTrue(children[0].getPath().equals(file3));
	TestUtil.assertAdded(children[0].getComparison());
	assertTrue(children[0].getAffectedChildren(filter).length == 0);
}
/**
 * Tests the getSubtree and createSubtree API calls.
 */
public void testCopySubtrees() {
	ElementTree copy = new ElementTree();

	copySubtree(fTree, copy, Path.ROOT);

	TestUtil.assertTreeStructure(copy);
	TestUtil.assertTreeStructure(fTree);
}
/**
 * Tests the getSubtree and createSubtree API calls.
 */
public void testCopySubtreesInDelta() {
	ElementTree copy = fTree.newEmptyDelta();

	copySubtree(fTree, copy, Path.ROOT);

	assertTrue(copy.hasAncestor(fTree));
	TestUtil.assertTreeStructure(copy);
	TestUtil.assertTreeStructure(fTree);
}
public void testGetSubtree() {
	/* make sure subtree has same structure as original */
	ElementTree sub = fTree.getSubtree(Path.ROOT);
	TestUtil.assertTreeStructure(sub);

}
/**
 * Tests the makeComplete API method
 */
public void testMakeComplete() {
	/* do a bunch of operations to build a chain of deltas */
	ElementTree deepTree = buildDeltaChain(fTree);

	assertTrue(deepTree.hasAncestor(fTree));

	deepTree.makeComplete();
	assertTrue(deepTree.deltaDepth() == 0);
	assertTrue(deepTree.getParent() == null);
//	assert(!deepTree.hasAncestor(fTree));
	assertTrue(!fTree.hasAncestor(deepTree));
	TestUtil.assertTreeStructure(deepTree);
}
/**
 * Regression test for PR:
 * 1EVK1G0: RQSM:ALL - ElementTree.getChildrenOfType() returns deleted elements
 */
 public void testRegression1EVK1G0() {
	
	ElementTree t1 = new ElementTree();
	IPath elementA = Path.ROOT.append("A");
	IPath elementAX = elementA.append("AX");
	IPath elementB = Path.ROOT.append("B");

	t1.createElement(elementA, new Object());
	t1.createElement(elementAX, new Object());
	t1.createElement(elementB, new Object());

	t1.immutable();

	IPath[] roots1 = t1.getChildren(t1.getRoot());
	assertTrue("A included?", t1.includes(elementA));
	assertTrue("AX included?", t1.includes(elementAX));
	assertTrue("B included?", t1.includes(elementB));
	assertTrue("2 roots?", roots1.length == 2);

	ElementTree t2 = t1.newEmptyDelta();

	t2.deleteElement(elementB);
	IPath[] roots2 = t2.getChildren(t2.getRoot());

	t2.deleteElement(elementAX);
	IPath[] kids2 = t2.getChildren(elementA);

	assertTrue("A included?", t2.includes(elementA));
	assertTrue("AX deleted?", !t2.includes(elementAX));
	assertTrue("B deleted?", !t2.includes(elementB));
	assertTrue("A has no kids?", kids2.length == 0);
	assertTrue("Only one root?", roots2.length == 1);
}
/**
 * Regression test for PR:
 * 1EVWNON: RQSM:ALL - ElementTree.getSubtree() sometimes returns bogus root element ids
 */
public void testRegression1EVWNON() {
	
	ElementTree t0 = new ElementTree();
	IPath e0 = Path.ROOT.append("ZZProject");
	t0.createElement(e0, new Object());
	t0.immutable();

	ElementTree t2 = t0.newEmptyDelta();
	IPath e02 = e0.append("posterchild");
	t2.createElement(e02, new Object());
	ElementTree t2a = t2.getSubtree(e0);
	IPath bogus = t2a.getChildren(t2a.getRoot())[0];
	assertTrue(bogus.lastSegment() != null);
}
/**
 * Regression test for PR:
 * 1FVVP6L: ITPCORE:ALL - ElementTree corruption
 */
 public void testRegression1FVVP6L() {
	
	ElementTree t1 = new ElementTree();
	IPath elementA = Path.ROOT.append("A");
	IPath elementB = elementA.append("B");

	t1.createElement(elementA, "Element A");

	t1.immutable();
	ElementTree t2 = t1.newEmptyDelta();
	
	t2.createElement(elementB, "Element B");

	t2.immutable();
	t2 = t2.newEmptyDelta();
	
	t2.deleteElement(elementB);
	t2.createElement(elementB, "New Content");

	t2.immutable();
	try {
		//bug should cause runtime exception here
		t1.toDebugString();
	} catch (RuntimeException e) {
		assertTrue("Unexpected runtime exception in test", false);
	}
}
public void testSetInfo() {

	/* set new info */
	Object solutionInfo = "New Info";
	Object file2Info = Class.class;
	fTree.setElementData(solution, solutionInfo);
	fTree.setElementData(file2, file2Info);
	assertEquals(fTree.getElementData(solution), solutionInfo);
	assertEquals(fTree.getElementData(file2), file2Info);

	/* set to null */
	fTree.setElementData(solution, null);
	fTree.setElementData(file2, null);
	assertEquals(fTree.getElementData(solution), null);
	assertEquals(fTree.getElementData(file2), null);

	/* reset info */
	fTree.setElementData(solution, solutionInfo);
	fTree.setElementData(file2, file2Info);
	assertEquals(fTree.getElementData(solution), solutionInfo);
	assertEquals(fTree.getElementData(file2), file2Info);
}
public void testSetInfoInDelta() {

	/* set new info */
	fTree.immutable();
	ElementTree delta = fTree.newEmptyDelta();
	
	Object solutionInfo = "New Info";
	Object file2Info = Class.class;
	delta.setElementData(solution, solutionInfo);
	delta.setElementData(file2, file2Info);
	assertEquals(delta.getElementData(solution), solutionInfo);
	assertEquals(delta.getElementData(file2), file2Info);

	/* set to null */
	delta.setElementData(solution, null);
	delta.setElementData(file2, null);
	assertEquals(delta.getElementData(solution), null);
	assertEquals(delta.getElementData(file2), null);

	/* reset info */
	delta.setElementData(solution, solutionInfo);
	delta.setElementData(file2, file2Info);
	assertEquals(delta.getElementData(solution), solutionInfo);
	assertEquals(delta.getElementData(file2), file2Info);
}
public void testSetInfoInDelta2() {

	/* set new info */
	Object solutionInfo = "New Info";
	Object file2Info = Class.class;
	fTree.setElementData(solution, solutionInfo);
	fTree.setElementData(file2, file2Info);
	assertEquals(fTree.getElementData(solution), solutionInfo);
	assertEquals(fTree.getElementData(file2), file2Info);

	/* set to null in delta*/
	ElementTree delta = fTree.newEmptyDelta();
	delta.setElementData(solution, null);
	delta.setElementData(file2, null);
	assertEquals(delta.getElementData(solution), null);
	assertEquals(delta.getElementData(file2), null);
	assertEquals(fTree.getElementData(solution), solutionInfo);
	assertEquals(fTree.getElementData(file2), file2Info);
}
}
