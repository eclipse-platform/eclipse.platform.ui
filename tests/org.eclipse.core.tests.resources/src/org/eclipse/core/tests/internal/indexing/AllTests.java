package org.eclipse.core.tests.internal.indexing;

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
		suite.addTest(IntegratedFieldTest.suite());
		suite.addTest(IntegratedIndexedStoreTest.suite());
		suite.addTest(IntegratedObjectStoreTest.suite());
		suite.addTest(IntegratedPageStoreTest.suite());
		return suite;
	}

}