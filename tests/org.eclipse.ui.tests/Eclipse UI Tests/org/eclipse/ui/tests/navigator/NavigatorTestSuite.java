package org.eclipse.ui.tests.navigator;

import junit.framework.Test;
import junit.framework.TestSuite;


public class NavigatorTestSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new NavigatorTestSuite();
	}
	
	/**
	 * Construct the test suite.
	 */
	public NavigatorTestSuite() {
		addTest(new TestSuite(ResourceNavigatorTest.class));
		addTest(new TestSuite(NavigatorTest.class));
		addTest(new TestSuite(DecoratorTestCase.class));
		addTest(new TestSuite(DuplicateMenuItemTest.class));
	}

}

