package org.eclipse.team.tests.ccvs.ui.benchmark;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests extends TestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(SyncTests.class);
		suite.addTestSuite(WorkflowTests.class);
		//suite.addTestSuite(CommandTests.class);
    	return new BenchmarkTestSetup(suite);
	}	
	
	public AllTests(String name) {
		super(name);
	}
	public AllTests() {
		super();
	}
}
