package org.eclipse.team.tests.ccvs.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.team.tests.core.AllTeamTests;

public class AllTeamAndCVSTests extends EclipseTest {

	/**
	 * Constructor for CVSClientTest.
	 */
	public AllTeamAndCVSTests() {
		super();
	}

	/**
	 * Constructor for CVSClientTest.
	 * @param name
	 */
	public AllTeamAndCVSTests(String name) {
		super(name);
	}

	/*
	 * ORDER IS IMPORTANT: Run compatibility and resource tests before any other!!!
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestSetup(AllTeamTests.suite()));
		suite.addTest(new CVSTestSetup(AllTests.suite()));
		return suite;
	}
}

