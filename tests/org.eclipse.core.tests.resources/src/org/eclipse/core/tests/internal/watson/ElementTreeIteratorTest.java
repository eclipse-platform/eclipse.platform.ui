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

import java.util.Vector;

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
	IPath sol= Path.ROOT.append("sol");
	tree.createElement(sol, null);
	for(int p=0;p<num;p++){
		IPath proj = sol.append("proj"+p);
		tree.createElement(proj, null);
		for(int k=0;k<num;k++){
			IPath folder = proj.append("folder"+k);
			tree.createElement(folder, null);
			for(int c=0;c<num;c++){
				IPath file = folder.append("file"+c);
				tree.createElement(file, null);
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
	final Vector elts = new Vector();
	IElementContentVisitor elementContentVisitor = new IElementContentVisitor() {
		public void visitElement(ElementTree tree, IPath elementID, Object info) {
			elts.addElement(elementID);
		}
	};
	new ElementTreeIterator().iterate(tree, elementContentVisitor);
	assertEquals("1", 2+n+n*n+n*n*n, elts.size());

	elts.removeAllElements();
	IPath innerElement = Path.ROOT.append("sol").append("proj1");
	new ElementTreeIterator().iterate(tree, elementContentVisitor, innerElement);
	assertEquals("2", 1+n+n*n, elts.size());
}
}
