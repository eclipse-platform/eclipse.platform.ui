package org.eclipse.core.tests.internal.builders;

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
	suite.addTest(BuilderTest.suite());
	suite.addTest(BuildDeltaVerificationTest.suite());
	suite.addTest(MultiProjectBuildTest.suite());
	suite.addTest(BuilderNatureTest.suite());
	return suite;
}
}

