package org.eclipse.ui.tests.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import org.eclipse.core.resources.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IWorkbenchPage;

public abstract class AbstractTestCase extends TestCase {
	protected IWorkbench fWorkbench;
	private List testWindows, testPages;
	protected static int DEF_PAGETOTAL = 3, DEF_WINTOTAL = 3;

	public AbstractTestCase(String testName) {
		super(testName);
		fWorkbench = PlatformUI.getWorkbench();
		testWindows = new ArrayList(DEF_WINTOTAL);
		testPages = new ArrayList(DEF_PAGETOTAL);
	}

	/**
	 * Tear down.  May be overridden.
	 */
	public void tearDown() {
		cleanUp();
	}

	/**
	 * Cleanup after the test.
	 */
	public void cleanUp() {
		closeAllTestWindows();

		// ^_^?
	}

	/** 
	 * Open a test window.
	 */
	public IWorkbenchWindow openTestWindow() throws WorkbenchException {
		IWorkbenchWindow win =
			fWorkbench.openWorkbenchWindow(
				EmptyPerspective.PERSP_ID,
				ResourcesPlugin.getWorkspace());
		testWindows.add(win);
		return win;
	}

	/**
	 * Close all test windows.
	 */
	public void closeAllTestWindows() {
		Iterator iter = testWindows.iterator();
		IWorkbenchWindow win;
		while (iter.hasNext()) {
			win = (IWorkbenchWindow) iter.next();
			win.close();
		}
		testWindows.clear();
	}

	public IWorkbenchPage openTestPage(IWorkbenchWindow win)
		throws WorkbenchException {
		IWorkbenchPage[] pages = openTestPage(win, 1);
		return pages[0];
	}

	public IWorkbenchPage[] openTestPage(IWorkbenchWindow win, int pageTotal)
		throws WorkbenchException {
		IWorkbenchPage[] pages = new IWorkbenchPage[pageTotal];
		IWorkspace work = ResourcesPlugin.getWorkspace();

		for (int i = 0; i < pageTotal; i++) {
			pages[i] = win.openPage(EmptyPerspective.PERSP_ID, work);
			testPages.add(pages[i]);
		}

		return pages;
	}

	public void closeAllTestPages() {
		IWorkbenchPage page;
		Iterator iter = testPages.iterator();
		while (iter.hasNext()) {
			page = (IWorkbenchPage) iter.next();
			page.close();
		}
		testPages.clear();
	}

}