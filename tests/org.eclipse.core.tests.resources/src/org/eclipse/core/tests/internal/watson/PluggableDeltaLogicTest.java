package org.eclipse.core.tests.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.watson.*;
/**
 * This class tests the ElementTreeDelta functionality of allowing
 * the Elmeentree client to specify how deltas appear.  For example,
 * the tree can have phantom elements that are ignored when deleted
 * or changed.
 */
public class PluggableDeltaLogicTest extends WatsonTest implements IPathConstants {
	protected static final IElementComparator fComparator = PhantomComparator.getComparator();
	protected static final IDeltaFilter fFilter = new TestDeltaFilter();
	protected ElementTree fTree;
public PluggableDeltaLogicTest() {
	super(null);
}
public PluggableDeltaLogicTest(String name) {
	super(name);
}
/**
 * Creates an ElementTree identical in structure to the superclass'
 * TestElementTree, but with PhantomElementData instead of strings.
 * Initially none of the elements are phantoms.
 */
static ElementTree createPhantomElementTree() {

	ElementTree spooky = new ElementTree();

	/* build the phantom tree */
	spooky.createElement(solution, new PhantomElementData("solution", false));
	spooky.createElement(project1, new PhantomElementData("project1", false));
	spooky.createElement(project2, new PhantomElementData("project2", false));
	spooky.createElement(file1, new PhantomElementData("file1", false));
	spooky.createElement(folder1, new PhantomElementData("folder1", false));
	spooky.createElement(folder2, new PhantomElementData("folder2", false));
	spooky.createElement(file2, new PhantomElementData("file2", false));
	spooky.createElement(folder3, new PhantomElementData("folder3", false));
	spooky.createElement(folder4, new PhantomElementData("folder4", false));
	spooky.createElement(file3, new PhantomElementData("file3", false));

	return spooky;
}
protected void setUp() throws Exception {
	fTree = createPhantomElementTree();
}
public static Test suite() { 
	TestSuite suite= new TestSuite(PluggableDeltaLogicTest.class);
	return suite;
}
/**
 * 
 */
protected void tearDown() throws Exception {
	//ElementTree tests don't use the CoreTest infrastructure
}
/**
 * Tests adding a phantom -- should not show up in delta
 */
public void testAddPhantom() {
	ElementTree deltaTree = fTree.newEmptyDelta();
	PhantomElementData phantomData = new PhantomElementData("data", true);
	IPath project3 = solution.append("project3");
	IPath file4 = folder2.append("file4");
	deltaTree.createElement(project3, phantomData);
	deltaTree.createElement(file4, phantomData);

	/* make sure the delta is empty (root has no children) */
	ElementTreeDelta delta = deltaTree.computeDeltaWith(fTree, fComparator);
	ElementDelta rootDelta = delta.getElementDelta(Path.ROOT);
	assertTrue(rootDelta.getAffectedChildren(fFilter).length == 0);
}
/**
 * Tests changing a phantom element into a real element.
 * This should appear as an addition in the delta
 */
public void testConvertFromPhantom() {
	ElementTree oldTree = fTree.newEmptyDelta();

	/* convert project1 and folder3 to phantom */
	Object data = new PhantomElementData("CasperTheFriendlyPhantom", true);
	oldTree.setElementData(project1, data);
	oldTree.setElementData(folder3, data);

	/* now create a new delta */
	ElementTree newTree = oldTree.newEmptyDelta();

	/* set the phantoms to be real */
	data = new PhantomElementData("NotAPhantom", false);
	newTree.setElementData(project1, data);
	newTree.setElementData(folder3, data);

	/* analyze the delta */
	ElementTreeDelta delta = newTree.computeDeltaWith(oldTree, fComparator);

	ElementDelta rootDelta = delta.getElementDelta(Path.ROOT);
	ElementDelta[] children = rootDelta.getAffectedChildren(fFilter);

	/* one solution */
	assertTrue(children.length == 1);
	assertEquals(children[0].getPath(), solution);
	TestUtil.assertNoChange(children[0].getComparison());

	/* solution has two children */
	children = children[0].getAffectedChildren(fFilter);
	assertTrue(children.length == 2);
	assertEquals(children[0].getPath(), project1);
	TestUtil.assertAdded(children[0].getComparison());
	assertEquals(children[1].getPath(), project2);
	TestUtil.assertNoChange(children[1].getComparison());

	/* project2 has one changed child */
	children = children[1].getAffectedChildren(fFilter);
	assertTrue(children.length == 1);
	assertEquals(children[0].getPath(), folder1);
	TestUtil.assertNoChange(children[0].getComparison());

	/* folder1 has one removed child */
	children = children[0].getAffectedChildren(fFilter);
	assertTrue(children.length == 1);
	assertEquals(children[0].getPath(), folder3);
	TestUtil.assertAdded(children[0].getComparison());

	/* folder3 has no affected children (phantoms don't have recursive effects) */
	children = children[0].getAffectedChildren(fFilter);
	assertTrue(children.length == 0);
}
/**
 * Tests changing a real element into a phantom element.
 * This should appear as a deletion in the delta
 */
public void testConvertToPhantom() {
	ElementTree newTree = fTree.newEmptyDelta();

	/* convert project1 and folder3 to phantom */
	Object data = new PhantomElementData("CasperTheFriendlyPhantom", true);
	newTree.setElementData(project1, data);
	newTree.setElementData(folder3, data);

	/* analyze the delta */
	ElementTreeDelta delta = newTree.computeDeltaWith(fTree, fComparator);

	ElementDelta rootDelta = delta.getElementDelta(Path.ROOT);
	ElementDelta[] children = rootDelta.getAffectedChildren(fFilter);

	/* one solution */
	assertTrue(children.length == 1);
	assertEquals(children[0].getPath(), solution);
	TestUtil.assertNoChange(children[0].getComparison());

	/* solution has two children */
	children = children[0].getAffectedChildren(fFilter);
	assertTrue(children.length == 2);
	assertEquals(children[0].getPath(), project1);
	TestUtil.assertRemoved(children[0].getComparison());
	assertEquals(children[1].getPath(), project2);
	TestUtil.assertNoChange(children[1].getComparison());

	/* project2 has one changed child */
	children = children[1].getAffectedChildren(fFilter);
	assertTrue(children.length == 1);
	assertEquals(children[0].getPath(), folder1);
	TestUtil.assertNoChange(children[0].getComparison());

	/* folder1 has one removed child */
	children = children[0].getAffectedChildren(fFilter);
	assertTrue(children.length == 1);
	assertEquals(children[0].getPath(), folder3);
	TestUtil.assertRemoved(children[0].getComparison());

	/* folder3 has no affected children */
	children = children[0].getAffectedChildren(fFilter);
	assertTrue(children.length == 0);
}
/**
 * Tests removing a phantom -- should not show up in delta
 */
public void testRemovePhantom() {

	/* first add phantoms to a tree */
	ElementTree oldTree = fTree.newEmptyDelta();
	PhantomElementData phantomData = new PhantomElementData("data", true);
	IPath project3 = solution.append("project3");
	IPath file4 = folder2.append("file4");
	oldTree.createElement(project3, phantomData);
	oldTree.createElement(file4, phantomData);

	/* now delete the phantoms */
	ElementTree newTree = oldTree.newEmptyDelta();
	newTree.deleteElement(project3);
	newTree.deleteElement(file4);
	
	/* make sure the delta is empty */
	ElementTreeDelta delta = newTree.computeDeltaWith(oldTree, fComparator);
	ElementDelta rootDelta = delta.getElementDelta(Path.ROOT);
	assertTrue(rootDelta.getAffectedChildren(fFilter).length == 0);
}
}
