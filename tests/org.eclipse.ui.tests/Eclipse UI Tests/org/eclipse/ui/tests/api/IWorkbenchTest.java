package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.jdt.junit.util.*;

/**
 * Tests the IWorkbench interface.
 */
public class IWorkbenchTest extends UITestCase {

	public IWorkbenchTest(String testName) {
		super(testName);
	}

	/**
	 * Tests the activation of two windows.
	 */
	public void testGetActiveWorkbenchWindow() throws Throwable {
		IWorkbenchWindow win1, win2;

		// PR 1GkD5O0 - Fails on linux.
		String platform = SWT.getPlatform();
		if (platform.equals("motif"))
			return;
		
		// Test initial window.
		win1 = fWorkbench.getActiveWorkbenchWindow();
		assertNotNull(win1);

		// Test open window.
		win1 = openTestWindow();
		assertEquals(win1, fWorkbench.getActiveWorkbenchWindow());

		// Test open second window.
		win2 = openTestWindow();
		assertEquals(win2, fWorkbench.getActiveWorkbenchWindow());

		// Test set focus.
		win1.getShell().forceFocus();
		assertEquals(win1, fWorkbench.getActiveWorkbenchWindow());

		// Test set focus.
		win2.getShell().forceFocus();
		assertEquals(win2, fWorkbench.getActiveWorkbenchWindow());

		// Cleanup in tearDown.
	}

	public void testGetEditorRegistry() throws Throwable {
		IEditorRegistry reg = fWorkbench.getEditorRegistry();
		assertNotNull(reg);
	}

	public void testGetPerspectiveRegistry() throws Throwable {
		IPerspectiveRegistry reg = fWorkbench.getPerspectiveRegistry();
		assertNotNull(reg);
	}
	
	public void testGetPrefereneManager() throws Throwable {
		PreferenceManager mgr = fWorkbench.getPreferenceManager();
		assertNotNull(mgr);
	}

	public void testGetSharedImages() throws Throwable {
		ISharedImages img = fWorkbench.getSharedImages();
		assertNotNull(img);
	}

	public void testGetWorkbenchWindows() throws Throwable {
		IWorkbenchWindow[] wins = fWorkbench.getWorkbenchWindows();
		assertEquals(ArrayUtil.checkNotNull(wins), true);
		int oldTotal = wins.length;
		int num = 3;

		IWorkbenchWindow[] newWins = new IWorkbenchWindow[num];
		for (int i = 0; i < num; i++)
			newWins[i] = openTestWindow();

		wins = fWorkbench.getWorkbenchWindows();
		for (int i = 0; i < num; i++)
			assertTrue(ArrayUtil.contains(wins, newWins[i]));

		assertEquals(wins.length, oldTotal + num);

		closeAllTestWindows();
		wins = fWorkbench.getWorkbenchWindows();
		assertEquals(wins.length, oldTotal);
	}

	/**
	 * openWorkbenchWindow(String, IAdaptable)
	 */
	public void testOpenWorkbenchWindow() throws Throwable {
		// open a window with valid perspective 
		IWorkbenchWindow win = null;
		try {
			win =
				fWorkbench.openWorkbenchWindow(
					EmptyPerspective.PERSP_ID,
					ResourcesPlugin.getWorkspace());
			assertNotNull(win);
			// PR 1GkD5O0 - Fails on linux.
			String platform = SWT.getPlatform();
			if (!platform.equals("motif")) {
				assertEquals(win, fWorkbench.getActiveWorkbenchWindow());
			}
			assertEquals(
				EmptyPerspective.PERSP_ID,
				win.getActivePage().getPerspective().getId());
		} finally {
			if (win != null)
				win.close();
		}

		// open a window with invalid perspective. WorkbenchException is expected.
		boolean exceptionOccured = false;
		try {
			win =
				fWorkbench.openWorkbenchWindow("afdasfdasf", ResourcesPlugin.getWorkspace());
		} catch (WorkbenchException ex) {
			exceptionOccured = true;
		}

		assertEquals(exceptionOccured, true);
	}

	/**
	 * openWorkbenchWindow(IAdaptable)
	 */
	public void testOpenWorkbenchWindow2() throws Throwable {
		// open a window with valid perspective 
		IWorkbenchWindow win = null;

		try {
			win = fWorkbench.openWorkbenchWindow(ResourcesPlugin.getWorkspace());
			assertNotNull(win);

			assertEquals(win, fWorkbench.getActiveWorkbenchWindow());
			String defaultID = fWorkbench.getPerspectiveRegistry().getDefaultPerspective();
			assertEquals(win.getActivePage().getPerspective().getId(), defaultID);

		} finally {
			if (win != null)
				win.close();
		}
	}

	/**
	 * close() couldn't be tested because calling close() would lead to early termination 
	 * to entire test suites		
	 */
	public void testClose() {
	}
}