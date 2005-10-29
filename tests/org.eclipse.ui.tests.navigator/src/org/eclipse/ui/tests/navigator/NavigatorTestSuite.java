package org.eclipse.ui.tests.navigator;

import junit.framework.Test;
import junit.framework.TestSuite;

public final class NavigatorTestSuite extends TestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static final Test suite() {
		return new NavigatorTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public NavigatorTestSuite() {
		addTest(new TestSuite(OpenTest.class));
	}

}
