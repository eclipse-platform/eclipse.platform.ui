package org.eclipse.ui.tests.propertysheet;

import junit.framework.*;
import junit.textui.TestRunner;
import org.eclipse.swt.SWT;

/**
 * Test areas of the Property Sheet API.
 */
public class PropertySheetTestSuite extends TestSuite {
	/**
	 * Construct the test suite.
	 */
	public PropertySheetTestSuite() {
		addTest(new TestSuite(PropertySheetAuto.class));
	}
}