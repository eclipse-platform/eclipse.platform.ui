package org.eclipse.ui.tests.api;

import junit.framework.TestCase;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Tests the PlatformUI class.
 */
public class PlatformUITest extends TestCase {

	public PlatformUITest(String testName) {
		super(testName);
	}

	public void testGetWorkbench() throws Throwable {
		// From Javadoc: "Returns the workbench interface."
		IWorkbench wb = PlatformUI.getWorkbench();
		assertNotNull(wb);
	}
	
	public void testPLUGIN_ID() {
		// From Javadoc: "Identifies the workbench plugin."
		assertNotNull(PlatformUI.PLUGIN_ID);
	}
}