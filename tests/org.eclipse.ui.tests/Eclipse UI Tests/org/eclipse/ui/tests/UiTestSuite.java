package org.eclipse.ui.tests;

import junit.framework.*;
import junit.textui.TestRunner;
import org.eclipse.swt.SWT;

/**
 * Test all areas of the UI.
 */
public class UiTestSuite extends TestSuite {

	/**
	 * Construct the test suite.
	 */
	public UiTestSuite() {
		addTest(new org.eclipse.ui.tests.api.ApiTestSuite());
		// PR 1GkD5O0 - Fails on linux.
		String platform = SWT.getPlatform();
		if (!platform.equals("motif")) {
			addTest(new org.eclipse.ui.tests.dialogs.UIAutomatedSuite());
		}
	}

}