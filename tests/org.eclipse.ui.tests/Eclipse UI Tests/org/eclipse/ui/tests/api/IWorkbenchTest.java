package org.eclipse.ui.tests.api;

import junit.framework.TestCase;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Tests the IWorkbench interface.
 */
public class IWorkbenchTest extends TestCase {

	private IWorkbench wb;
	
	public IWorkbenchTest(String testName) {
		super(testName);
	}

	protected void setUp() {
		wb = PlatformUI.getWorkbench();
	}
	
	public void testGetActiveWorkbenchWindow() throws Throwable {
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		assertNotNull(win);
	}

	public void testGetPrefereneManager() throws Throwable {
		PreferenceManager mgr = wb.getPreferenceManager();
		assertNotNull(mgr);
	}
}