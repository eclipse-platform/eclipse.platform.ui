package org.eclipse.team.tests.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;

public class AllTeamTests extends EclipseWorkspaceTest {

	/**
	 * Constructor for CVSClientTest.
	 */
	public AllTeamTests() {
		super();
	}

	/**
	 * Constructor for CVSClientTest.
	 * @param name
	 */
	public AllTeamTests(String name) {
		super(name);
	}

	/*
	 * ORDER IS IMPORTANT: Run compatibility and resource tests before any other!!!
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(RepositoryProviderTests.suite());
		suite.addTest(StreamTests.suite());
		return new TargetTestSetup(suite);
	}
}

