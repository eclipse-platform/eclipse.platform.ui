package org.eclipse.e4.ui.menu.tests;

import junit.framework.TestSuite;

public class MenuTestSuite extends TestSuite {
	public static TestSuite suite() {
		return new MenuTestSuite();
	}

	public MenuTestSuite() {
		addTestSuite(MMenuItemTest.class);
		addTestSuite(MToolItemTest.class);
	}

}
