package org.eclipse.core.tests.internal.runtime;

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
		suite.addTest(AuthorizationDatabaseTest.suite());
		suite.addTest(CipherStreamsTest.suite());
		suite.addTest(CipherTest.suite());
		suite.addTest(LogSerializationTest.suite());
		return suite;
	}
}
