package org.eclipse.ui.tests;

import junit.framework.*;
import junit.textui.TestRunner;
import org.eclipse.swt.SWT;

/**
 * Test all areas of the UI.
 */
public class UiTestSuite extends TestSuite {

	/**
	 * Returns the suite.  This is required to
	 * use the JUnit Launcher.
	 */
	public static Test suite() {
		return new UiTestSuite();
	}
	
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
		addTest(new org.eclipse.ui.tests.propertysheet.PropertySheetTestSuite());		
		addTest(new org.eclipse.ui.tests.internal.InternalTestSuite());
		addTest(new org.eclipse.ui.tests.navigator.NavigatorTestSuite());
		addTest(new org.eclipse.ui.tests.adaptable.AdaptableTestSuite());			
		addTest(new org.eclipse.ui.tests.zoom.ZoomTestSuite());			
	}
}