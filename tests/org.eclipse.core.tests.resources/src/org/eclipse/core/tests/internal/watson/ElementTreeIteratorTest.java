/**********************************************************************
 * Copyright (c) 2000,2003 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at http://www.eclipse.
 * org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.internal.watson;

import java.util.ArrayList;
import java.util.Stack;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/** 
 * Unit tests for <code>ElementTreeIterator</code>.
 */
public class ElementTreeIteratorTest extends WatsonTest {
public ElementTreeIteratorTest() {
	super(null);
}
public ElementTreeIteratorTest(String name) {
	super(name);
}
protected void setUp() throws Exception {
}
static void setupElementTree(ElementTree tree, int num) {
	final IElementTreeData data = new IElementTreeData() {
		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
			}
			return null;
		}
	};
	IPath sol= Path.ROOT.append("sol");
	tree.createElement(sol, data);
	for(int p=0;p<num;p++){
		IPath proj = sol.append("proj"+p);
		tree.createElement(proj, data);
		for(int k=0;k<num;k++){
			IPath folder = proj.append("folder"+k);
			tree.createElement(folder, data);
			for(int c=0;c<num;c++){
				IPath file = folder.append("file"+c);
				tree.createElement(file, data);
			}
		}
	}
}
public static Test suite() {  
	TestSuite suite= new TestSuite(ElementTreeIteratorTest.class); 
	return suite;
}
/**
 * 
 */
protected void tearDown() throws Exception {
	//ElementTree tests don't use the CoreTest infrastructure
}
public void testContentIterator() {
	ElementTree tree = new ElementTree();
	int n= 3;
	setupElementTree(tree, n);
	final ArrayList elts = new ArrayList();
	IElementContentVisitor elementVisitor = new IElementContentVisitor() {
		public boolean visitElement(ElementTree tree, IPathRequestor requestor, Object info) {
			elts.add(requestor.requestPath());
			return true;
		}
	};
	new ElementTreeIterator(tree, Path.ROOT).iterate(elementVisitor);
	assertEquals("1", 2+n+n*n+n*n*n, elts.size());

	elts.clear();
	IPath innerElement = Path.ROOT.append("sol").append("proj1");
	new ElementTreeIterator(tree, innerElement).iterate(elementVisitor);
	assertEquals("2", 1+n+n*n, elts.size());
}
protected void deleteTreeContents(ElementTree tree) {
	IPath[] children = tree.getChildren(Path.ROOT);
	for (int i = 0; i < children.length; i++) {
		tree.deleteElement(children[i]);
	}
}
protected void modifyTree(ElementTree tree) {
	class MyStack extends Stack {
		public void pushAll(Object[] array) {
			for (int i = 0; i < array.length; i++) {
				push(array[i]);
			}
		}
	};
	MyStack toModify = new MyStack();
	IPath[] children = tree.getChildren(Path.ROOT);
	toModify.pushAll(children);
	while (!toModify.isEmpty()) {
		IPath visit = (IPath)toModify.pop();
		tree.openElementData(visit);
		toModify.pushAll(tree.getChildren(visit));
	}
}
}
