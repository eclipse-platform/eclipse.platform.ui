package org.eclipse.ui.tests.api;

import org.eclipse.ui.tests.harness.util.EmptyPerspective;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Test cases for the <code>IPageLayout</code> API.
 * 
 * @since 3.2
 */
public class IPageLayoutTest extends UITestCase {

	public IPageLayoutTest(String testName) {
		super(testName);
	}

	public void testGetDescriptor() {
		EmptyPerspective.setLastPerspective(null);
		openTestWindow(EmptyPerspective.PERSP_ID);
		assertEquals(EmptyPerspective.PERSP_ID, EmptyPerspective.getLastPerspective());
	}
}
