package org.eclipse.core.tests.runtime;

import junit.framework.*;
/**
 * Runs the sniff tests for the build. All tests listed here should
 * be automated.
 */
public class AutomatedTests extends TestCase {

public AutomatedTests() {
	super(null);
}

public AutomatedTests(String name) {
	super(name);
}

public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(org.eclipse.core.tests.runtime.AllTests.suite());
	return suite;
}
}