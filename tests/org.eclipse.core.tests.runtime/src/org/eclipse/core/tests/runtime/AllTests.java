package org.eclipse.core.tests.runtime;

import junit.framework.*;
import org.eclipse.core.tests.runtime.model.ConfigurationElementModelTest;


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
	suite.addTest(PathTest.suite());
	suite.addTest(PlatformTest.suite());
	suite.addTest(org.eclipse.core.tests.internal.runtime.AllTests.suite());
	suite.addTest(ConfigurationElementModelTest.suite());
	suite.addTest(org.eclipse.core.tests.internal.plugins.AllTests.suite());
	suite.addTest(org.eclipse.core.tests.internal.registrycache.AllTests.suite());
	return suite;
}
}
