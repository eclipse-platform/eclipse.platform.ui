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

import junit.framework.*;

public class AllTests extends TestCase {
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests() {
	super(null);
}
/**
 * AllTests constructor comment.
 * @param name java.lang.String
 */
public AllTests(String name) {
	super(name);
}
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(DeltaChainFlatteningTest.suite());
		suite.addTest(DeltaFlatteningTest.suite());
		suite.addTest(DeltaIteratorTest.suite());
		suite.addTest(ElementDeltaTest.suite());
		suite.addTest(ElementTreeDeltaChainTest.suite());
		suite.addTest(ElementTreeIteratorTest.suite());
		suite.addTest(ElementTreeTest.suite());
		suite.addTest(PluggableDeltaLogicTest.suite());
		suite.addTest(TreeFlatteningTest.suite());
		return suite;
	}
}