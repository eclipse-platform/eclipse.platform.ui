package org.eclipse.team.tests.ccvs.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends EclipseTest {

	/**
	 * Constructor for CVSClientTest.
	 */
	public AllTests() {
		super();
	}

	/**
	 * Constructor for CVSClientTest.
	 * @param name
	 */
	public AllTests(String name) {
		super(name);
	}

	/*
	 * ORDER IS IMPORTANT: Run compatibility and resource tests before any other!!!
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(org.eclipse.team.tests.ccvs.core.compatible.AllTests.suite());
		suite.addTest(org.eclipse.team.tests.ccvs.core.cvsresources.AllTests.suite());
		suite.addTest(org.eclipse.team.tests.ccvs.core.provider.AllTests.suite());
		return new CVSTestSetup(suite);
	}
}

