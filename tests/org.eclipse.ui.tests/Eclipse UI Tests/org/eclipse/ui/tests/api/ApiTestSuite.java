package org.eclipse.ui.tests.api;

import junit.framework.*;
import junit.textui.TestRunner;

/**
 * Test all areas of the UI API.
 */
public class ApiTestSuite extends TestSuite {

	/**
	 * Construct the test suite.
	 */
	public ApiTestSuite() {
		addTest(new TestSuite(PlatformUITest.class));
		addTest(new TestSuite(IWorkbenchTest.class));
	}

}