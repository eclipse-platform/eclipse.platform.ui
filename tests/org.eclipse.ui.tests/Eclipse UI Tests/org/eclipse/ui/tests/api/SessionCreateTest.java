package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.ResourcesPlugin;


/**
 * This test generates a session state in the workbench.
 * The SessionRestoreTest is used to verify the state after
 * the workbench is shutdown and run again.
 */
public class SessionCreateTest extends AbstractTestCase {
	
	public static String SESSION_PERSPID_ID = "org.eclipse.ui.tests.api.SessionPerspective";

	public SessionCreateTest(String testName) {
		super(testName);
	}
	
	/**
	 * Generates a session state in the workbench.
	 */
	public void testCreate() throws Throwable {
		// Open the session test page.
		fWorkbench.openWorkbenchWindow(
			SESSION_PERSPID_ID,
			ResourcesPlugin.getWorkspace());
	}

}

