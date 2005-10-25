package org.eclipse.ui.tests.navigator;

import junit.framework.TestSuite;

public class NavigatorTestSuite extends TestSuite {

	public NavigatorTestSuite() {
		addTest(new TestSuite(OpenTest.class));
	}
	
}
