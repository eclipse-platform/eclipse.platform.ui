package org.eclipse.core.tests.plugintests;

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
	suite.addTest(ApiTest.suite());
	return suite;
}
}
