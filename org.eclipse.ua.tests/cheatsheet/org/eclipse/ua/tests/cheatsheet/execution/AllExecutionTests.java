package org.eclipse.ua.tests.cheatsheet.execution;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for the classes which execute commands and actions from a cheatsheet
 */
public class AllExecutionTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"org.eclipse.ua.tests.cheatsheet.AllExecutionTests");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestActionExecution.class);
		suite.addTestSuite(TestCommandExecution.class);
		//$JUnit-END$
		return suite;
	}

}
