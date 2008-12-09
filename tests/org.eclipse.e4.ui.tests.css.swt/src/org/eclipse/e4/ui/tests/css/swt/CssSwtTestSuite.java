package org.eclipse.e4.ui.tests.css.swt;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CssSwtTestSuite extends TestSuite {
	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static final Test suite() {
		return new CssSwtTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public CssSwtTestSuite() {
		addTest(new TestSuite(WidgetClassAttributeTest.class));
	}
}
