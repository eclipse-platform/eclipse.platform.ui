package org.eclipse.core.tests.internal.dtree;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.*;

public class AllTests extends TestCase {
public AllTests() {
	super(null);
}
public AllTests(String name) {
	super(name);
}
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(DataTreeTest.suite());
		suite.addTest(DeltaDataTreeTest.suite());
		return suite;
	}
}
