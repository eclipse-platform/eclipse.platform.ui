package org.eclipse.core.tests.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.dtree.*;
import org.eclipse.core.internal.watson.*;
import java.io.*;
import junit.framework.*;

/**
 * Unit tests for <code>ElementTreeWriter</code> and
 * <code>ElementTreeReader</code>.
 */
public class TreeFlatteningTest extends ElementTreeSerializationTest {
public TreeFlatteningTest() {
}
public TreeFlatteningTest(String name) {
	super(name);
}
/**
 * Performs the serialization activity for this test
 */
public Object doRead(ElementTreeReader reader, DataInputStream input) throws IOException {
	return reader.readTree(input);
}
/**
 * Runs a test for this class at a certain depth and path
 */
public void doTest(IPath path, int depth) {
	/* Get an element tree from somewhere. */
	fTree= TestUtil.createTestElementTree();
	fSubtreePath = path;
	fDepth = depth;

	ElementTree newTree = (ElementTree)doPipeTest();

	TestUtil.assertEqualTrees(this.getClass().toString() + "test0", fTree, newTree, fSubtreePath, fDepth);
}
/**
 * Performs the serialization activity for this test
 */
public void doWrite(ElementTreeWriter writer, DataOutputStream output) throws IOException{
	writer.writeTree(fTree, fSubtreePath, fDepth, output);
}
protected void setUp() throws Exception {
	super.setUp();
	fTree = TestUtil.createTestElementTree();
}
public static Test suite() {
	TestSuite suite= new TestSuite(TreeFlatteningTest.class); 
	return suite;
}
public void test0() {
	/* Get an element tree from somewhere. */
	fTree= TestUtil.createTestElementTree();
	ElementTree newTree = (ElementTree)doFileTest();

	TestUtil.assertEqualTrees(this.getClass().toString() + "test0", fTree, newTree);
}
/**
 * Tests the reading and writing of element deltas
 */
public void testExhaustive() {
	doExhaustiveTests();
}
public void testNullData() {
	/* Get an element tree from somewhere. */
	fTree= TestUtil.createTestElementTree();
	fTree = fTree.newEmptyDelta();

	/* set some elements to have null data */
	fTree.setElementData(solution, null);
	fTree.setElementData(folder2, null);
	fTree.immutable();
	
	ElementTree newTree = (ElementTree)doPipeTest();

	TestUtil.assertEqualTrees(this.getClass().toString() + "test0", fTree, newTree);
}
public void testWriteRoot() {
	/* Get an element tree from somewhere. */
	fTree= TestUtil.createTestElementTree();
	fSubtreePath = Path.ROOT;

	ElementTree newTree = (ElementTree)doPipeTest();

	TestUtil.assertEqualTrees(this.getClass().toString() + "test0", fTree, newTree, fSubtreePath);
}
}
