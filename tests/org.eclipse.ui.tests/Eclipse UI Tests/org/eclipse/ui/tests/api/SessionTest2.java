package org.eclipse.ui.tests.api;
import org.eclipse.ui.*;

/**
 * SessionTest2 runs the second half of our session
 * presistance tests.
 * 
*/
public class SessionTest2 extends AbstractTestCase {

	/** 
	 * Construct an instance.
	 */
	public SessionTest2() {
		super("testRestore");
	}
		
	/**
	 * Generates a session state in the workbench.
	 */
	public void testRestore() throws Throwable {
		// Get the session test page.
		IWorkbenchPage page = getSessionPage();
		assertNotNull(page);
		
		// Get the session view.
		IViewPart view = page.findView(SessionView.VIEW_ID);
		assertNotNull(view);
		assert(view instanceof SessionView);
		
		// Test state of session view.
		SessionView sessionView = (SessionView)view;
		sessionView.testMementoState(this);
	}
	
	/**
	 * Returns the first page with a SessionPerspective.
	 */
	private IWorkbenchPage getSessionPage() {
		IWorkbenchWindow [] windows = fWorkbench.getWorkbenchWindows();
		for (int nX = 0; nX < windows.length; nX ++) {
			IWorkbenchPage [] pages = windows[nX].getPages();
			for (int nY = 0; nY < pages.length; nY ++) {
				IPerspectiveDescriptor desc = pages[nY].getPerspective();
				if (desc.getId().equals(SessionTest1.SESSION_PERSPID_ID))
					return pages[nY];
			}
		}
		return null;
	}

}

