package org.eclipse.core.tests.internal.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
		suite.addTest(ModelObjectWriterTest.suite());
		suite.addTest(ResourceInfoTest.suite());
		return suite;
	}
}
