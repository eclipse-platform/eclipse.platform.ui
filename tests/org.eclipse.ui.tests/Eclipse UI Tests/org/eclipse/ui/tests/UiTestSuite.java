package org.eclipse.ui.tests;

import junit.framework.*;
import junit.textui.TestRunner;

/**
 * Test all areas of the UI.
 */
public class UiTestSuite extends TestSuite {

	/**
	 * Construct the test suite.
	 */
	public UiTestSuite() {
		addTest(new org.eclipse.ui.tests.api.ApiTestSuite());
		addTest(new org.eclipse.ui.tests.dialogs.UIAutomatedSuite());
	}

}