package org.eclipse.ui.tests.api;
import junit.framework.*;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.*;

/**
 * SessionTest1 runs the first half of our session
 * presistance tests.
 * 
*/
public class SessionTest1 extends AbstractTestCase {
	
	public static String SESSION_PERSPID_ID = "org.eclipse.ui.tests.api.SessionPerspective";

	/** 
	 * Construct an instance.
	 */
	public SessionTest1() {
		super("testCreate");
	}
		/**
	 * Generates a session state in the workbench.
	 */
	public void testCreate() throws Throwable {
		// Get workbench.
		IWorkbench wb = PlatformUI.getWorkbench();
		
		// Open the session test page.
		IWorkbenchWindow window = wb.openWorkbenchWindow(
			SESSION_PERSPID_ID,
			ResourcesPlugin.getWorkspace());
		IWorkbenchPage page = window.getActivePage();
			
		// Open the session test view.
		page.showView(SessionView.VIEW_ID);
	}

}

