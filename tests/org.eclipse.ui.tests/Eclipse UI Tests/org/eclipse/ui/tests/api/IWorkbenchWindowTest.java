package org.eclipse.ui.tests.api;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

public class IWorkbenchWindowTest extends AbstractTestCase {

	private IWorkbenchWindow fWin;

	public IWorkbenchWindowTest(String testName) {
		super(testName);
	}

	public void setUp() {
		try {
			fWin = openTestWindow();
		} catch (WorkbenchException e) {
			fail();
		}
	}

	public void testClose() throws Throwable {
		IWorkbenchWindow win = openTestWindow();
		assert(win.close() == true);
	}

	public void testGetActivePage() throws Throwable {
		IWorkbenchPage page = openTestPage(fWin);
		assertNotNull(fWin.getActivePage());
		assertEquals(fWin.getActivePage(), page);

		openTestPage(fWin);
		assert(page != fWin.getActivePage());

		//close all pages of the window
		IWorkbenchPage[] pages = fWin.getPages();
		for (int i = 0; i < pages.length; i++)
			pages[i].close();
		page = fWin.getActivePage();
		assertNull(page);
	}

	public void testSetActivePage() throws Throwable {
		openTestPage(fWin, DEF_PAGETOTAL);
		IWorkbenchPage[] pages = fWin.getPages();

		for (int i = 0; i < pages.length; i++) {
			fWin.setActivePage(pages[i]);
			assertEquals(pages[i], fWin.getActivePage());
		}
	}

	public void testGetPages() throws Throwable {
		int totalBefore;
		IWorkbenchPage[] pages, domainPages;

		totalBefore = fWin.getPages().length;
		int num = 5;
		pages = openTestPage(fWin, num);
		assert(fWin.getPages().length == totalBefore + num);

		domainPages = fWin.getPages();
		for (int i = 0; i < pages.length; i++)
			assert(Tool.arrayHas(domainPages, pages[i]));
	
		closeAllTestPages();
		assert(fWin.getPages().length == totalBefore );
	}

	public void testGetShell() {
		Shell sh = fWin.getShell();
		assertNotNull(sh);
	}

	public void testGetWorkbench() {
		IWorkbenchWindow win = fWorkbench.getActiveWorkbenchWindow();

		assertEquals(win.getWorkbench(), fWorkbench);
	}

	/**
	 * openPage(String)
	 */
	public void testOpenPage() throws Throwable {
		IWorkbenchPage page = null;
		try {
			page = fWin.openPage(ResourcesPlugin.getWorkspace());
			assertNotNull(page);
			assertEquals(fWin.getActivePage(), page);
		} finally {
			if (page != null)
				page.close();
		}
	}

	/**
	 * openPage(String, IAdaptable)
	 */
	public void testOpenPage2() throws Throwable {
		IWorkbenchPage page = null;
		try {
			page = fWin.openPage(EmptyPerspective.PERSP_ID, ResourcesPlugin.getWorkspace());
			assertNotNull(page);
			assertEquals(fWin.getActivePage(), page);
			assertEquals(
				fWin.getActivePage().getPerspective().getId(),
				EmptyPerspective.PERSP_ID);
		} finally {
			if (page != null)
				page.close();
		}

		//test openPage() fails
		try {
			page = fWin.openPage("*************", ResourcesPlugin.getWorkspace());
			fail(Tool.notCaught(Tool.WBE));
		} catch (WorkbenchException ex) {
		}

		page.close();
	}

	public void testIsApplicationMenu() throws Throwable {
		assert(fWin.isApplicationMenu(Tool.FakeID) == false);
		/*
				somemagic
				assert( fWin.isApplicationMenu( goodID ) == true );
		*/
	}
}